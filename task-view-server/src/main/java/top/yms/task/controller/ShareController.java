package top.yms.task.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import top.yms.storage.client.StorageClient;
import top.yms.task.common.R;
import top.yms.task.entity.ShareEntity;
import top.yms.task.entity.TaskEntity;
import top.yms.task.entity.TaskFileEntity;
import top.yms.task.exception.BusinessException;
import top.yms.task.mapper.ShareMapper;
import top.yms.task.mapper.TaskFileMapper;
import top.yms.task.mapper.TaskMapper;
import top.yms.task.util.IdWorker;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URLEncoder;
import java.util.*;

@RestController
@RequestMapping("/api/share")
public class ShareController {

    private static final Logger log = LoggerFactory.getLogger(ShareController.class);

    @Resource
    private TaskMapper taskMapper;

    @Resource
    private TaskFileMapper taskFileMapper;

    @Resource
    private ShareMapper shareMapper;

    @Resource
    private IdWorker idWorker;

    @Resource
    private StorageClient storageClient;

    /** 前端端口 */
    @Value("${share.frontend-port:5173}")
    private int frontendPort;

    /** 后端端口 */
    @Value("${server.port:8080}")
    private int serverPort;

    /** token 过期天数 */
    @Value("${share.token-expire-days:7}")
    private int tokenExpireDays;

    /**
     * 创建分享链接
     */
    @PostMapping("/create/{taskId}")
    public R<Map<String, String>> createShare(@PathVariable String taskId) throws Exception {
        TaskEntity task = taskMapper.selectById(taskId);
        if (task == null) {
            return R.fail("任务不存在");
        }

        // 生成 token
        String token = UUID.randomUUID().toString().replace("-", "");

        // 计算过期时间
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, tokenExpireDays);

        ShareEntity share = new ShareEntity();
        share.setId(String.valueOf(idWorker.nextId()));
        share.setTaskId(taskId);
        share.setToken(token);
        share.setExpireTime(cal.getTime());
        share.setCreateTime(new Date());
        share.setUpdateTime(new Date());
        shareMapper.insert(share);

        // 获取本机 LAN IP
        String lanIp = getLanIp();

        String apiHost = String.format("%s:%d", lanIp, serverPort);
        String shareUrl = String.format("http://%s:%d/share/%s?token=%s&api=%s",
                lanIp, frontendPort, taskId, token, apiHost);
        String apiUrl = "http://" + apiHost;

        Map<String, String> result = new LinkedHashMap<>();
        result.put("shareUrl", shareUrl);
        result.put("apiUrl", apiUrl);
        result.put("token", token);

        log.info("创建分享链接: {}", shareUrl);
        return R.ok(result);
    }

    /**
     * 分享页获取任务详情（通过 token 校验）
     */
    @GetMapping("/tasks/{taskId}")
    public R<Map<String, Object>> getTask(@PathVariable String taskId,
                                          @RequestParam("token") String token) {
        log.info("share getTask taskId={}, token={}", taskId, token);
        // 校验 token
        ShareEntity share = shareMapper.selectOne(
                new LambdaQueryWrapper<ShareEntity>()
                        .eq(ShareEntity::getTaskId, taskId)
                        .eq(ShareEntity::getToken, token)
        );

        if (share == null) {
            return R.fail(403, "无效的访问链接");
        }

        if (share.getExpireTime() != null && share.getExpireTime().before(new Date())) {
            return R.fail(403, "分享链接已过期");
        }

        // 查询任务
        TaskEntity task = taskMapper.selectById(taskId);
        if (task == null) {
            return R.fail("任务不存在");
        }

        // 查询附件
        List<TaskFileEntity> files = taskFileMapper.selectList(
                new LambdaQueryWrapper<TaskFileEntity>()
                        .eq(TaskFileEntity::getTaskId, taskId)
                        .orderByAsc(TaskFileEntity::getCreateTime)
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("task", task);
        result.put("files", files);

        return R.ok(result);
    }

    /**
     * 分享页下载附件（通过 token 校验）
     */
    @GetMapping("/files/{fileId}/download")
    public void downloadFile(@PathVariable String fileId,
                              @RequestParam("token") String token,
                              HttpServletResponse response) {
        TaskFileEntity fileEntity = taskFileMapper.selectById(fileId);
        if (fileEntity == null) {
            response.setStatus(404);
            return;
        }

        // 校验 token
        ShareEntity share = shareMapper.selectOne(
                new LambdaQueryWrapper<ShareEntity>()
                        .eq(ShareEntity::getTaskId, fileEntity.getTaskId())
                        .eq(ShareEntity::getToken, token)
        );

        if (share == null) {
            response.setStatus(403);
            return;
        }

        if (share.getExpireTime() != null && share.getExpireTime().before(new Date())) {
            response.setStatus(403);
            return;
        }

        String storageFileId = fileEntity.getFilePath();
        try {
            String encodedName = URLEncoder.encode(fileEntity.getFileName(), "UTF-8")
                    .replaceAll("\\+", "%20");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition",
                    "attachment; filename*=UTF-8''" + encodedName);
            response.setHeader("Content-Length", String.valueOf(fileEntity.getFileSize()));

            try (InputStream in = storageClient.getFileStream(storageFileId);
                 OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                out.flush();
            }
        } catch (Exception e) {
            log.error("下载文件失败: {}", e.getMessage(), e);
            response.setStatus(500);
        }
    }

    /**
     * 获取本机 LAN IP
     */
    private String getLanIp() {
        try {
            // 遍历所有网络接口
            /*
            for (NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (iface.isLoopback() || !iface.isUp()) continue;
                for (InetAddress addr : Collections.list(iface.getInetAddresses())) {
                    if (addr.isLoopbackAddress() || addr.isLinkLocalAddress()) continue;
                    if (addr.getHostAddress().contains(":")) continue; // 跳过 IPv6
                    String ip = addr.getHostAddress();
                    // 优先返回 10.x.x.x 或 192.168.x.x 或 172.16-31.x.x
                    if (ip.startsWith("10.") || ip.startsWith("192.168.") ||
                            (ip.startsWith("172.") && isPrivate172(ip))) {
                        return ip;
                    }
                }
            }*/
            // 兜底
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            log.error("获取本机IP失败", e);
            return "127.0.0.1";
        }
    }

    private boolean isPrivate172(String ip) {
        try {
            String[] parts = ip.split("\\.");
            int second = Integer.parseInt(parts[1]);
            return second >= 16 && second <= 31;
        } catch (Exception e) {
            return false;
        }
    }
}

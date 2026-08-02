package top.yms.task.controller;

import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.yms.storage.client.StorageClient;
import top.yms.storage.entity.UploadResp;
import top.yms.task.common.R;
import top.yms.task.util.IdWorker;

import javax.annotation.Resource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Markdown 图片上传与访问
 */
@RestController
@RequestMapping("/api")
public class MarkdownImageController {

    private static final Logger log = LoggerFactory.getLogger(MarkdownImageController.class);

//    @Value("${markdown-image.upload-dir:./uploads/markdown-images}")
//    private String uploadDir;

    /**
     * 文件存储服务
     */
    @Resource
    private StorageClient storageClient;

    /**
     * id生成器（雪花算法）
     */
    @Resource
    private IdWorker idWorker;

    /** 允许的图片类型 */
    private static final Set<String> ALLOWED_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "image/png", "image/jpeg", "image/gif",
            "image/webp", "image/bmp", "image/svg+xml"
    )));

    /** 最大 10MB */
    private static final long MAX_SIZE = 10 * 1024 * 1024;

    /**
     * 上传 Markdown 图片（粘贴 Ctrl+V 或拖拽时调用）
     * 返回图片访问 URL
     */
    @PostMapping("/markdown/images/upload")
    public R<Map<String, String>> upload(@RequestParam("image") MultipartFile file) {
        if (file.isEmpty()) {
            return R.fail(400, "图片为空");
        }
        if (file.getSize() > MAX_SIZE) {
            return R.fail(400, "图片大小不能超过 10MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            return R.fail(400, "不支持的图片类型：" + contentType);
        }

        try {
            // 确保上传目录存在
            /*
            Path dir = Paths.get(uploadDir);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }*/

            // 生成唯一文件名：UUID + 原始扩展名
            String originalName = file.getOriginalFilename();
            String ext = ".png";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
            } else if (contentType.equals("image/jpeg")) {
                ext = ".jpg";
            } else if (contentType.equals("image/png")) {
                ext = ".png";
            } else if (contentType.equals("image/gif")) {
                ext = ".gif";
            } else if (contentType.equals("image/webp")) {
                ext = ".webp";
            } else if (contentType.equals("image/bmp")) {
                ext = ".bmp";
            } else if (contentType.equals("image/svg+xml")) {
                ext = ".svg";
            }
            // new id
            long newId = idWorker.nextId();
            String storedName = newId + ext;
            //调用storage sdk进行文件上传
            UploadResp uploadResp = storageClient.upload(file.getInputStream(), storedName);
            log.info("uploadResp={}", JSONObject.toJSONString(uploadResp));
            String viewUrl = uploadResp.getViewUrl(); //返回浏览url
           /*
            Path targetPath = dir.resolve(storedName);
            // 保存文件
            file.transferTo(targetPath.toFile());
            String url = "/api/markdown/images/" + storedName;*/

            Map<String, String> result = new LinkedHashMap<>();
            result.put("url", viewUrl);
            result.put("name", originalName != null ? originalName : storedName);
            return R.ok(result);

        } catch (IOException e) {
            return R.fail(500, "图片保存失败: " + e.getMessage());
        }
    }


    /**
     * 访问 Markdown 图片（在详情页预览时通过 <img> 加载）
     */
    /*@GetMapping("/markdown/images/{filename}")
    public void getImage(@PathVariable String filename, javax.servlet.http.HttpServletResponse response) {
        Path filePath = Paths.get(uploadDir, filename);

        // 安全检查：防止路径穿越
        if (!filePath.normalize().startsWith(Paths.get(uploadDir).normalize())) {
            response.setStatus(403);
            return;
        }

        if (!Files.exists(filePath)) {
            response.setStatus(404);
            return;
        }

        try {
            // 根据扩展名设置 Content-Type
            String lowerName = filename.toLowerCase();
            if (lowerName.endsWith(".png")) {
                response.setContentType("image/png");
            } else if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
                response.setContentType("image/jpeg");
            } else if (lowerName.endsWith(".gif")) {
                response.setContentType("image/gif");
            } else if (lowerName.endsWith(".webp")) {
                response.setContentType("image/webp");
            } else if (lowerName.endsWith(".bmp")) {
                response.setContentType("image/bmp");
            } else if (lowerName.endsWith(".svg")) {
                response.setContentType("image/svg+xml");
            } else {
                response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            }

            try (InputStream in = new FileInputStream(filePath.toFile());
                 OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                out.flush();
            }

        } catch (IOException e) {
            response.setStatus(500);
        }
    }*/
}

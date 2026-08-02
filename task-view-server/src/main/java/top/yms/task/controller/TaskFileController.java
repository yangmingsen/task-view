package top.yms.task.controller;

import com.alibaba.fastjson2.JSONObject;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.yms.storage.client.StorageClient;
import top.yms.task.common.R;
import top.yms.task.entity.TaskFileEntity;
import top.yms.task.exception.BusinessException;
import top.yms.task.service.TaskFileService;
import top.yms.task.util.JwtUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api")
public class TaskFileController {

    @Autowired
    private TaskFileService fileService;

//    @Value("${task-file.upload-dir:./uploads/task-files}")
//    private String uploadDir;

    @Resource
    private StorageClient storageClient;

    /**
     * 获取任务的附件列表
     */
    @GetMapping("/tasks/{taskId}/files")
    public R<List<Map<String, Object>>> list(@PathVariable String taskId) {
        List<TaskFileEntity> files = fileService.listByTaskId(taskId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (TaskFileEntity f : files) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", f.getId());
            item.put("fileName", f.getFileName());
            item.put("fileSize", f.getFileSize());
            item.put("fileType", f.getFileType());
            item.put("createdBy", f.getCreatedBy());
            item.put("createTime", f.getCreateTime());
            result.add(item);
        }
        return R.ok(result);
    }

    /**
     * 上传附件
     */
    @PostMapping("/tasks/{taskId}/files")
    public R<List<Map<String, Object>>> upload(
            @PathVariable String taskId,
            @RequestParam("files") MultipartFile[] files,
            @RequestHeader(value = "Authorization", required = false) String auth) {

        String uploadBy = "unknown";
        if (auth != null && auth.startsWith("Bearer ")) {
            try {
                Claims claims = JwtUtil.parseToken(auth.replace("Bearer ", ""));
                uploadBy = String.valueOf(claims.getOrDefault("username", "unknown"));
            } catch (Exception ignored) {}
        }

        List<Map<String, Object>> uploaded = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            TaskFileEntity saved = fileService.upload(taskId, file, uploadBy);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", saved.getId());
            item.put("fileName", saved.getFileName());
            item.put("fileSize", saved.getFileSize());
            item.put("fileType", saved.getFileType());
            item.put("createTime", saved.getCreateTime());
            uploaded.add(item);
        }

        return R.ok(uploaded);
    }

    /**
     * 下载附件
     */
    @GetMapping("/files/{fileId}/download")
    public void download(@PathVariable String fileId, HttpServletResponse response) {
        TaskFileEntity file = fileService.getById(fileId);
        if (file == null) {
            response.setStatus(404);
            return;
        }
        /* 改为使用storageClient sdk获取
        Path filePath = Paths.get(uploadDir, file.getFilePath());
        if (!Files.exists(filePath)) {
            response.setStatus(404);
            return;
        }*/
        //这里保存的是文件在文件服务器中的fileId
        String storageFileId = file.getFilePath();

        try {
            String encodedName = URLEncoder.encode(file.getFileName(), "UTF-8")
                    .replaceAll("\\+", "%20");
            response.setContentType("application/octet-stream");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename*=UTF-8''" + encodedName);
            response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.getFileSize()));

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
            response.setStatus(500);
        }
    }

    /**
     * 删除附件
     */
    @DeleteMapping("/files/{fileId}")
    public R<Void> delete(@PathVariable String fileId) {
        TaskFileEntity file = fileService.getById(fileId);
        if (file == null) {
            return R.fail(404, "文件不存在");
        }
        // 删除物理文件
        try {
            /* 改为删除文件服务器中的文件
            Path filePath = Paths.get(uploadDir, file.getFilePath());
            Files.deleteIfExists(filePath); */
            storageClient.destroy(file.getFilePath());
        } catch (Exception ignored) {}
        fileService.removeById(fileId);
        return R.ok();
    }
}

package top.yms.task.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.yms.task.entity.TaskFileEntity;
import top.yms.task.mapper.TaskFileMapper;
import top.yms.task.service.TaskFileService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class TaskFileServiceImpl extends ServiceImpl<TaskFileMapper, TaskFileEntity> implements TaskFileService {

    @Value("${task-file.upload-dir:./uploads/task-files}")
    private String uploadDir;

    @Override
    public TaskFileEntity upload(String taskId, MultipartFile file, String uploadBy) {
        // 按日期分子目录
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String storedName = UUID.randomUUID().toString().replace("-", "") + ext;
        String relativePath = dateDir + "/" + storedName;

        try {
            Path targetDir = Paths.get(uploadDir, dateDir);
            Files.createDirectories(targetDir);
            Path targetFile = targetDir.resolve(storedName);
            file.transferTo(targetFile.toFile());
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败: " + e.getMessage(), e);
        }

        TaskFileEntity entity = new TaskFileEntity();
        entity.setId(UUID.randomUUID().toString().replace("-", ""));
        entity.setTaskId(taskId);
        entity.setFileName(originalName != null ? originalName : "unknown");
        entity.setStoredName(storedName);
        entity.setFilePath(relativePath);
        entity.setFileSize(file.getSize());
        entity.setFileType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        entity.setCreatedBy(uploadBy);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());

        save(entity);
        return entity;
    }

    @Override
    public List<TaskFileEntity> listByTaskId(String taskId) {
        return lambdaQuery()
                .eq(TaskFileEntity::getTaskId, taskId)
                .orderByAsc(TaskFileEntity::getCreateTime)
                .list();
    }
}

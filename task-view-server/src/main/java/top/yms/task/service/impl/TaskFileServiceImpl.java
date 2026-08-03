package top.yms.task.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.yms.storage.client.StorageClient;
import top.yms.storage.entity.UploadResp;
import top.yms.task.entity.TaskFileEntity;
import top.yms.task.mapper.TaskFileMapper;
import top.yms.task.service.TaskFileService;
import top.yms.task.util.IdWorker;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class TaskFileServiceImpl extends ServiceImpl<TaskFileMapper, TaskFileEntity> implements TaskFileService {

    private final static Logger log = LoggerFactory.getLogger(TaskFileServiceImpl.class);

    @Resource
    private StorageClient storageClient;

    @Resource
    private IdWorker idWorker;

    @Override
    public TaskFileEntity upload(String taskId, MultipartFile file, String uploadBy) {
        // 按日期分子目录
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        //生成id
        long newId = idWorker.nextId();
        String storedName = newId + ext;

        TaskFileEntity entity = new TaskFileEntity();
        try {
            /*
            Path targetDir = Paths.get(uploadDir, dateDir);
            Files.createDirectories(targetDir);
            Path targetFile = targetDir.resolve(storedName);
            file.transferTo(targetFile.toFile());*/
            UploadResp uploadResp = storageClient.upload(file.getInputStream(), storedName);
            log.info("uploadResp={}", JSONObject.toJSONString(uploadResp));
            String fileId = uploadResp.getFileId(); //获取在文件服务器中文件id,保存到filePath中,下载时可以通过fileId下载
            entity.setFilePath(fileId);  //填充到filePath中
        } catch (Exception e) {
            throw new RuntimeException("文件保存失败: " + e.getMessage(), e);
        }
        entity.setId(newId+"");
        entity.setTaskId(taskId);
        entity.setFileName(originalName != null ? originalName : "unknown");
        entity.setStoredName(storedName);
        entity.setFileSize(file.getSize());
        String fileType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        if (fileType.length() > 63) {
            fileType = ext.substring(1);
        }
        entity.setFileType(fileType);
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

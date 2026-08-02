package top.yms.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;
import top.yms.task.entity.TaskFileEntity;

import java.util.List;

public interface TaskFileService extends IService<TaskFileEntity> {

    /**
     * 上传文件
     */
    TaskFileEntity upload(String taskId, MultipartFile file, String uploadBy);

    /**
     * 获取任务的所有文件
     */
    List<TaskFileEntity> listByTaskId(String taskId);
}

package top.yms.task.service;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;
import top.yms.task.entity.TaskFileEntity;

import java.util.List;
import java.util.Map;

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

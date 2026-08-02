package top.yms.task.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.yms.task.entity.TaskEntity;
import top.yms.task.mapper.TaskMapper;
import top.yms.task.service.TaskService;

/**
 * 待办任务 Service 实现
 */
@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, TaskEntity> implements TaskService {
}

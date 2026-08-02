package top.yms.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.yms.task.entity.TaskEntity;

import java.util.List;

/**
 * 待办任务 Mapper
 */
@Mapper
public interface TaskMapper extends BaseMapper<TaskEntity> {

    /**
     * 全局搜索：匹配 title、assigned_to、project
     */
    List<TaskEntity> search(@Param("keyword") String keyword);
}

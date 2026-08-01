package top.yms.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.yms.task.entity.UserEntity;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

}
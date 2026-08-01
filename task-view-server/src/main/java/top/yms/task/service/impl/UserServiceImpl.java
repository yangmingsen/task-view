package top.yms.task.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.yms.task.entity.UserEntity;
import top.yms.task.mapper.UserMapper;
import top.yms.task.service.UserService;

@Service
public class UserServiceImpl  extends ServiceImpl<UserMapper, UserEntity> implements UserService {

}

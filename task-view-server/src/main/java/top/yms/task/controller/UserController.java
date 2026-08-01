package top.yms.task.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.yms.task.entity.UserEntity;
import top.yms.task.service.UserService;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/user")
@RestController
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/findAll")
    public List<UserEntity> findAll() {
        List<UserEntity> list = userService.list();
        return list;
    }
}

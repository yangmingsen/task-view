package top.yms.task.controller;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import top.yms.task.common.R;
import top.yms.task.dto.LoginDTO;
import top.yms.task.entity.UserEntity;
import top.yms.task.service.UserService;
import top.yms.task.util.JwtUtil;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 登录
     */
    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody LoginDTO dto) {
        String username = dto.getUsername();
        String password = dto.getPassword();

        // 参数校验
        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            return R.fail(400, "用户名和密码不能为空");
        }

        // 查询用户
        UserEntity user = userService.lambdaQuery()
                .eq(UserEntity::getUsername, username.trim())
                .one();

        // 简单密码校验 (MD5)
        if (user == null) {
            return R.fail(401, "用户名或密码错误");
        }

        String md5Pwd = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        if (!md5Pwd.equals(user.getPassword())) {
            return R.fail(401, "用户名或密码错误");
        }

        // 生成 JWT
        String token = JwtUtil.generateToken(user.getId(), user.getUsername());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", token);

        Map<String, Object> userInfo = new LinkedHashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("name", user.getRealName());
        userInfo.put("username", user.getUsername());
        data.put("user", userInfo);

        return R.ok(data);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public R<Map<String, Object>> me(@RequestHeader("Authorization") String auth) {
        try {
            String token = auth.replace("Bearer ", "");
            if (!JwtUtil.validateToken(token)) {
                return R.fail(401, "Token 已过期");
            }
            Claims claims = JwtUtil.parseToken(token);
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("userId", claims.get("userId"));
            info.put("username", claims.get("username"));
            return R.ok(info);
        } catch (Exception e) {
            return R.fail(401, "无效的 Token");
        }
    }
}

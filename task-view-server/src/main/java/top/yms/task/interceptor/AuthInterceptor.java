package top.yms.task.interceptor;

import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证拦截器 —— 放行 /api/auth/**、/api/share/**、/api/markdown/**
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    /** 无需认证的路径前缀 */
    private static final String[] SKIP_PREFIXES = {"/api/auth", "/api/share", "/api/markdown"};

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String path = request.getRequestURI();

        // 放行无需认证的路径
        for (String prefix : SKIP_PREFIXES) {
            if (path.startsWith(prefix)) return true;
        }

        // OPTIONS 预检放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        // 检查 Authorization 头
        String auth = request.getHeader("Authorization");
        if (auth == null || auth.isEmpty()) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
//            Map<String, Object> body = Map.of("code", 401, "message", "未登录");
            Map<String, Object> body = new HashMap<>();
            body.put("code", 401);
            body.put("message", "未登录");
            response.getWriter().write(JSONObject.toJSONString(body));
            return false;
        }

        return true;
    }
}

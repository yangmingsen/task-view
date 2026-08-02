package top.yms.task.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.yms.task.util.IdWorker;

/**
 * Created by yangmingsen on 2024/10/2.
 */
@Configuration
public class TaskViewConfig {
    @Bean
    public IdWorker idWorker() {
        return new IdWorker(0, 2);
    }

}

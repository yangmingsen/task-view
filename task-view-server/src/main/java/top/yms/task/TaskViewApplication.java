package top.yms.task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Created by yangmingsen on 2026/8/01.
 */
@EnableTransactionManagement
@SpringBootApplication
@ComponentScan("top.yms")
public class TaskViewApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskViewApplication.class, args);
    }

}

package com.taskmanager.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync // Bạn có thể chuyển @EnableAsync sang đây cho gọn
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);    // Luôn giữ 5 nhân viên trực chiến
        executor.setMaxPoolSize(10);    // Tối đa 10 nhân viên (nếu việc quá nhiều)
        executor.setQueueCapacity(500); // Hàng chờ chứa được 500 việc
        executor.setThreadNamePrefix("LogThread-");
        executor.initialize();
        return executor;
    }
}
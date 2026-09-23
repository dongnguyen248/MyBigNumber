package com.dongnguyen248.add2num.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("additionExecutor")
    public TaskExecutor additionExecutor(
            @Value("${add2num.executor.core-pool-size:2}") int corePoolSize,
            @Value("${add2num.executor.max-pool-size:8}") int maxPoolSize,
            @Value("${add2num.executor.queue-capacity:100}") int queueCapacity) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("addition-");
        executor.initialize();
        return executor;
    }
}
package com.shenhaoinfo.shucai_module_java.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 *
 * @author liuzhao
 * @date 2022/1/4
 */
@EnableAsync
@Configuration
@Slf4j
public class ExecutorConfig {

    @Bean("taskExecutor")
    public ThreadPoolTaskExecutor asyncServiceExecutor() {

        log.info("初始化线程池");


        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        //配置核心线程数

        executor.setCorePoolSize(16);

        //配置最大线程数

        executor.setMaxPoolSize(50);

        //配置队列大小

        executor.setQueueCapacity(200);

        //配置线程池中的线程的名称前缀

        executor.setThreadNamePrefix("async-service-");

        // rejection-policy：当pool已经达到max size的时候，如何处理新任务

        // CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        //执行初始化
        executor.initialize();

        return executor;

    }

}

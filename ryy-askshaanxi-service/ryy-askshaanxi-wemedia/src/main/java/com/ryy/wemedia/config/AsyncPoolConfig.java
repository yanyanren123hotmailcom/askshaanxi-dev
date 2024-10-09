package com.ryy.wemedia.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
/*
**文章发布与文章审核之间采用异步处理，因此自定义线程池
 */
@Configuration
public class AsyncPoolConfig{
//public class AsyncPoolConfig implements AsyncConfigurer {
//    @Override
//    @Bean
//    public Executor getAsyncExecutor(){
//        ThreadPoolTaskExecutor executor=new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
//        executor.setMaxPoolSize(200);
//        executor.setQueueCapacity(100);
//        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
//        executor.setThreadNamePrefix("myself-thread");
//        return executor;
//    }

    @Bean("asyncPool")
    public Executor asyncExecutor(){
        ThreadPoolTaskExecutor executor=new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        executor.setMaxPoolSize(200);
        executor.setQueueCapacity(100);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("myself-thread");
        return executor;
    }
}

package com.iotmining.services.notification.configuration;

import com.iotmining.services.notification.backoff.ExponentialBackoffHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AsyncConfig {

    //    @Bean(name = "notificationExecutor")
//    public Executor notificationExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(5);
//        executor.setMaxPoolSize(10);
//        executor.setQueueCapacity(100);
//        executor.setThreadNamePrefix("NotifyAsync-");
//        executor.initialize();
//        return executor;
//    }
    @Bean(name = "notificationExecutor")
    public TaskExecutor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Set the core and maximum pool size for scaling based on your system's load.
        executor.setCorePoolSize(10);  // Start with 10 threads
        executor.setMaxPoolSize(50);   // Allow up to 50 threads for high load
        executor.setQueueCapacity(200); // Set a larger queue to accommodate backlogged tasks
        executor.setKeepAliveSeconds(60);  // Keep idle threads alive for 60 seconds

        // Set the thread name prefix for easier identification of threads in logs.
        executor.setThreadNamePrefix("NotifyAsync-");

        // Use a custom RejectedExecutionHandler for better management of rejected tasks
//        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // Set Exponential Backoff Handler for rejected tasks
        executor.setRejectedExecutionHandler(new ExponentialBackoffHandler(
                5,            // Max retries
                1000,         // Initial delay of 1 second
                10000,        // Max delay of 10 seconds
                2.0           // Backoff factor (doubling the delay)
        ));
        // Initialize the executor
        executor.initialize();

        // Return the configured executor
        return executor;
    }
}

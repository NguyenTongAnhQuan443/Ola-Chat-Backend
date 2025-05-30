/*
 * @ (#) SchedulerConfig.java       1.0     30/05/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.configs;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 30/05/2025
 * @version:    1.0
 */

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class SchedulerConfig {
    @Bean
    public ScheduledExecutorService callTimeoutScheduler() {
        return Executors.newScheduledThreadPool(10);
    }
}

package com.zingly.events.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
/** Provides the application clock used for testable UTC timestamps. */
public class TimeConfig {

    @Bean
    /** Provides a UTC system clock bean. */
    Clock clock() {
        return Clock.systemUTC();
    }
}

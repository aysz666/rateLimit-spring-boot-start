package com.yuisole.config;


import com.yuisole.aspect.RateLimitAspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author Yuisole
 */
@AutoConfiguration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig {
    private static final Logger logger = LoggerFactory.getLogger(RateLimitConfig.class);

    @Bean
    public RateLimitAspect rateLimitAspect(RateLimitProperties properties){
        logger.info("\u001B[32m" + "[YUISOLE-RATELIMIT]加载完毕[{}]" + "\u001B[0m", properties);
        return RateLimitAspect.getInstance(properties);
    }
}

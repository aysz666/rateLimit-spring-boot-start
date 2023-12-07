package com.yuisole.config;


import com.yuisole.aspect.RateLimitAspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;



/**
 * 该类定义了一个名为rateLimitAspect的方法，用于创建RateLimitAspect对象。
 * 该方法使用@Bean注解，表示它将作为一个Spring Bean进行管理。
 * rateLimitAspect方法接受一个RateLimitProperties参数，并返回一个RateLimitAspect对象。
 *
 * @author Yuisole
 */
@AutoConfiguration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig {
    private static final Logger logger = LoggerFactory.getLogger(RateLimitConfig.class);

    /**
     * 创建并返回一个RateLimitAspect对象,并注入bean。
     * @param properties RateLimitProperties对象，用于配置RateLimitAspect。
     * @return RateLimitAspect对象。
     */
    @Bean
    public RateLimitAspect rateLimitAspect(RateLimitProperties properties){
        logger.info("\u001B[32m" + "[YUISOLE-RATELIMIT]加载完毕[{}]" + "\u001B[0m", properties);
        return RateLimitAspect.getInstance(properties);
    }
}

package com.yuisole.strategy.impl;

import com.yuisole.annotation.RateLimited;
import com.yuisole.config.RateLimitProperties;
import com.yuisole.exception.RateLimitExceededException;
import com.yuisole.storagebase.Storage;
import com.yuisole.strategy.RateLimitStrategy;
import org.aspectj.lang.JoinPoint;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author Yuisole
 */

public class DefaultRateLimitStrategy implements RateLimitStrategy {

    private static volatile DefaultRateLimitStrategy defaultRateLimitStrategy;

    private final RateLimitProperties properties;
    private DefaultRateLimitStrategy(RateLimitProperties properties) {
        this.properties = properties;
    }
    public static DefaultRateLimitStrategy getInstance(RateLimitProperties properties){
        if(defaultRateLimitStrategy == null){
            synchronized (DefaultRateLimitStrategy.class){
                if(defaultRateLimitStrategy == null){
                    defaultRateLimitStrategy = new DefaultRateLimitStrategy(properties);
                }
            }
        }
        return defaultRateLimitStrategy;
    }
    @Override
    public Object extracted(RateLimited rateLimited, Storage storage, JoinPoint joinPoint ) throws IOException {

        String methodName = joinPoint.getSignature().toShortString();

        String key = "default"+":"+ methodName;
        String coolKey = "default"+":" + methodName;

        long coolDownEndTime = storage.getCooldown(coolKey);
        if (System.currentTimeMillis() < coolDownEndTime) {
            if(rateLimited.captcha()){
                String uuid = null;
                String imageCode = null;

                // 获取当前请求的上下文信息,获取请求头
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                HttpServletRequest request = null;
                if (attributes != null) {
                    request = attributes.getRequest();
                    uuid = request.getHeader("uuid");
                    imageCode = request.getHeader("imageCode");
                }

                boolean validateCaptcha = storage.validateCaptcha(key, uuid, imageCode);
                if (!validateCaptcha) {
                    throw new RateLimitExceededException("Rate limit exceeded for methodName: " + methodName + ". Please wait for cooldown.");
                }else{
                    storage.removeRequestCount(key);
                }

            }else{
                throw new RateLimitExceededException("Rate limit exceeded for methodName: " + methodName + ". Please wait for cooldown.");
            }

        }

        return  doLimite(rateLimited,storage,key);

    }
}

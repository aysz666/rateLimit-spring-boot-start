package com.yuisole.strategy.impl;

import com.yuisole.annotation.RateLimited;
import com.yuisole.config.RateLimitProperties;
import com.yuisole.exception.RateLimitExceededException;
import com.yuisole.storagebase.Storage;
import com.yuisole.storagemode.RedisStorage;
import com.yuisole.strategy.RateLimitStrategy;
import org.aspectj.lang.JoinPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author Yuisole
 */

public class TokenRateLimitStrategy implements RateLimitStrategy {


    private static volatile TokenRateLimitStrategy tokenRateLimitStrategy;

    private final RateLimitProperties properties;
    private TokenRateLimitStrategy(RateLimitProperties properties) {
        this.properties = properties;
    }
    public static TokenRateLimitStrategy getInstance(RateLimitProperties properties){
        if(tokenRateLimitStrategy == null){
            synchronized (TokenRateLimitStrategy.class){
                if(tokenRateLimitStrategy == null){
                    tokenRateLimitStrategy = new TokenRateLimitStrategy(properties);
                }
            }
        }
        return tokenRateLimitStrategy;
    }

    @Override
    public Object extracted(RateLimited rateLimited, Storage storage, JoinPoint joinPoint) throws IOException {
        String token = null;
        // 获取当前请求的上下文信息,获取请求头
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        if (attributes != null) {
            request = attributes.getRequest();
            token = request.getHeader(properties.getTokenName());
        }


        String methodName = joinPoint.getSignature().toShortString();

        String key = token+":"+ methodName;
        String coolKey = token+":" + methodName;

        long currentTime = System.currentTimeMillis();

        long coolDownEndTime = storage.getCooldown(coolKey);
        if (currentTime < coolDownEndTime) {
            if(rateLimited.captcha()){
                String uuid = null;
                String imageCode = null;
                if(request != null){
                    uuid = request.getHeader("uuid");
                    imageCode = request.getHeader("imageCode");
                }
                boolean validateCaptcha = storage.validateCaptcha(key, uuid, imageCode);
                if(!validateCaptcha){
                    throw new RateLimitExceededException("Rate limit exceeded for token: " + token + ". Please wait for cooldown.");
                }else{
                    storage.removeRequestCount(key);
                }
            }else{
                throw new RateLimitExceededException("Rate limit exceeded for token: " + token + ". Please wait for cooldown.");
            }
        }


        return doLimite(rateLimited,storage,key);

    }

}

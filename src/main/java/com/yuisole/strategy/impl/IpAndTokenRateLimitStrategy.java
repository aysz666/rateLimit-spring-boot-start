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

public class IpAndTokenRateLimitStrategy implements RateLimitStrategy {

    private static volatile IpAndTokenRateLimitStrategy ipAndTokenRateLimitStrategy;

    private final RateLimitProperties properties;
    private IpAndTokenRateLimitStrategy(RateLimitProperties properties) {
        this.properties = properties;
    }
    public static IpAndTokenRateLimitStrategy getInstance(RateLimitProperties properties){
        if(ipAndTokenRateLimitStrategy == null){
            synchronized (IpAndTokenRateLimitStrategy.class){
                if(ipAndTokenRateLimitStrategy == null){
                    ipAndTokenRateLimitStrategy = new IpAndTokenRateLimitStrategy(properties);
                }
            }
        }
        return ipAndTokenRateLimitStrategy;
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

        String ip = null;
        if (request != null) {
            ip = request.getRemoteAddr();
        }
        String key = token+":"+ip +":"+ methodName;
        String coolKey = token+":"+ip+":" + methodName;

        long coolDownEndTime = storage.getCooldown(coolKey);
        if (System.currentTimeMillis() < coolDownEndTime) {

            if(rateLimited.captcha()){
                String uuid = null;
                String imageCode = null;
                if (request != null) {
                    uuid = request.getHeader("uuid");
                    imageCode = request.getHeader("imageCode");
                }
                boolean validateCaptcha = storage.validateCaptcha(key, uuid, imageCode);
                if (!validateCaptcha) {
                    throw new RateLimitExceededException("Rate limit exceeded for ip and token: " + ip + ":" + token + ". Please wait for cooldown.");
                }else{
                    storage.removeRequestCount(key);
                }
            }
            else{
                throw new RateLimitExceededException("Rate limit exceeded for ip and token: " + ip + ":" + token + ". Please wait for cooldown.");
            }
        }

        return doLimite(rateLimited,storage,key);

    }

    private String getToken(Object[] args,String tokenName) {
        for (Object arg : args) {
            if (arg instanceof HttpServletRequest) {
                return ((HttpServletRequest) arg).getHeader(tokenName);
            }
        }
        return null;
    }
}

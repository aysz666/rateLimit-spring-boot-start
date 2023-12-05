package com.yuisole.strategy.impl;

import com.yuisole.annotation.RateLimited;
import com.yuisole.config.RateLimitProperties;
import com.yuisole.exception.RateLimitExceededException;
import com.yuisole.storagebase.Storage;
import com.yuisole.storagemode.RedisStorage;
import com.yuisole.strategy.RateLimitStrategy;
import org.aspectj.lang.JoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author Yuisole
 */

public class IpRateLimitStrategy implements RateLimitStrategy {


    private static volatile IpRateLimitStrategy ipRateLimitStrategy;

    private final RateLimitProperties properties;
    private IpRateLimitStrategy(RateLimitProperties properties) {
        this.properties = properties;
    }
    public static IpRateLimitStrategy getInstance(RateLimitProperties properties){
        if(ipRateLimitStrategy == null){
            synchronized (IpRateLimitStrategy.class){
                if(ipRateLimitStrategy == null){
                    ipRateLimitStrategy = new IpRateLimitStrategy(properties);
                }
            }
        }
        return ipRateLimitStrategy;
    }

    @Override
    public Object extracted(RateLimited rateLimited, Storage storage, JoinPoint joinPoint ) throws IOException {

        String methodName = joinPoint.getSignature().toShortString();

        //获取请求上下文
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        if (requestAttributes != null) {
            request = ((ServletRequestAttributes)requestAttributes).getRequest();
        }
        String ip = null;
        if (request != null) {
            ip = request.getRemoteAddr();
        }
        String key = ip +":"+ methodName;
        String coolKey = ip+":" + methodName;

        long coolDownEndTime = storage.getCooldown(coolKey);
        if (System.currentTimeMillis() < coolDownEndTime) {
            if(rateLimited.captcha()){
                String uuid = null;
                String imageCode = null;
                if(request != null){
                    uuid = request.getHeader("uuid");
                    imageCode = request.getHeader("imageCode");
                }
                boolean validateCaptcha = storage.validateCaptcha(key, uuid, imageCode);
                if(!validateCaptcha) {
                    throw new RateLimitExceededException("Rate limit exceeded for ip: " + ip + ". Please wait for cooldown.");
                }else{
                    storage.removeRequestCount(key);
                }
            }else{
                throw new RateLimitExceededException("Rate limit exceeded for ip: " + ip + ". Please wait for cooldown.");
            }
        }



        return doLimite(rateLimited,storage,key);

    }

}
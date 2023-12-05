package com.yuisole.strategy;

import com.yuisole.annotation.RateLimited;
import com.yuisole.exception.RateLimitExceededException;
import com.yuisole.storagebase.Storage;
import org.aspectj.lang.JoinPoint;

import java.io.IOException;

/**
 * @author Yuisole
 */
public interface RateLimitStrategy {
    Object extracted(RateLimited rateLimited, Storage storage, JoinPoint joinPoint) throws IOException;

    /**
     * 一系列操作
     * @param rateLimited 注解
     * @param storage 存储模式
     * @param key 键
     * @return null 或者 图片验证码
     * @throws IOException io异常
     */
    default Object doLimite(RateLimited rateLimited, Storage storage,String key) throws IOException {
        int allowedRequests = rateLimited.value();
        long duration = rateLimited.duration();
        long currentTime = System.currentTimeMillis();

        // 获取滑动窗口的起始时间戳
        long slidingWindowStart = currentTime/ 1000 - duration;

        // 过滤掉早于滑动窗口起始时间的请求
        storage.filterRequestsBefore(key, slidingWindowStart);

        // 计算当前滑动窗口内的请求数
        int requestCount = storage.getRequestCount(key);

        // 增加当前请求的计数
        storage.incrementRequestCount(key);

        // 如果请求数超过限制，则抛出异常
        if (requestCount >= allowedRequests) {
            storage.setCooldown(key,rateLimited.cooldown());
            if (rateLimited.captcha()){

                return storage.setImageCode(key, duration);
            }
            throw new RateLimitExceededException("Rate limit exceeded for methodName: " +key );
        }


        return null;
    }

}
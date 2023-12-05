package com.yuisole.aspect;


import com.yuisole.annotation.RateLimited;
import com.yuisole.config.RateLimitProperties;
import com.yuisole.enums.RateLimitType;
import com.yuisole.exception.RateLimitExceededException;
import com.yuisole.storagebase.Storage;
import com.yuisole.storagemode.LocalStorage;
import com.yuisole.storagemode.RedisStorage;
import com.yuisole.strategy.RateLimitStrategy;
import com.yuisole.strategy.impl.DefaultRateLimitStrategy;
import com.yuisole.strategy.impl.IpAndTokenRateLimitStrategy;
import com.yuisole.strategy.impl.IpRateLimitStrategy;
import com.yuisole.strategy.impl.TokenRateLimitStrategy;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Yuisole
 */
@Aspect
public class RateLimitAspect {
    private HttpServletResponse response;

    private final Map<RateLimitType, RateLimitStrategy> STRATEGY_MAP;

    private RateLimitProperties properties;
    private static volatile RateLimitAspect rateLimitAspect;

    public static RateLimitAspect getInstance(RateLimitProperties properties){
        if(rateLimitAspect == null){
            synchronized (RateLimitAspect.class){
                if(rateLimitAspect == null){
                    rateLimitAspect = new RateLimitAspect(properties);
                }
            }
        }
        return rateLimitAspect;
    }

    private RateLimitAspect(RateLimitProperties properties) {
        this.properties = properties;
        // 初始化策略映射
        STRATEGY_MAP = new HashMap<>();
        STRATEGY_MAP.put(RateLimitType.IP, IpRateLimitStrategy.getInstance(properties));
        STRATEGY_MAP.put(RateLimitType.TOKEN, TokenRateLimitStrategy.getInstance(properties));
        STRATEGY_MAP.put(RateLimitType.IP_AND_TOKEN, IpAndTokenRateLimitStrategy.getInstance(properties));
        STRATEGY_MAP.put(RateLimitType.DEFAULT,DefaultRateLimitStrategy.getInstance(properties));
    }



    /**
     * RateLimitAspect.java
     * 该文件包含 RateLimitAspect 类的 Java 实现。该切面负责将速率限制应用于带有 @RateLimited 注解的方法。
     * 它检查用户配置的存储模式，如果指定了无效模式，则会抛出异常。然后，它根据存储模式选择适当的存储实现。
     * 该切面还检查用户指定的速率限制类型，如果提供了无效类型，则会抛出异常。最后，它调用适当的速率限制策略来处理速率限制。
     */
    @Around("@annotation(rateLimited)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {
        // 获取用户配置的模式
        if(!"redis".equals(properties.getMode()) && !"local".equals(properties.getMode())){
            throw new RateLimitExceededException("Rate limit exceeded for: Invalid rate limit storage mode");
        }
        // 根据用户配置的存储方式选择不同的实现
        Storage storage = "redis".equalsIgnoreCase(properties.getMode()) ? RedisStorage.getInstance(properties) : LocalStorage.getInstance();
        RateLimitType type = rateLimited.type();
        RateLimitStrategy strategy = STRATEGY_MAP.get(type);
        if (strategy == null) {
            throw new RateLimitExceededException("Rate limit exceeded for: Invalid rate limit type");
        }
        Map<String,String> extracted = (Map<String,String>)strategy.extracted(rateLimited, storage, joinPoint);
        if(extracted != null){
            response.setStatus(555);
            response.setHeader("uuid",extracted.get("uuid"));
            response.setHeader("imageCode",extracted.get("imageCode"));
            throw new RateLimitExceededException("Rate limit exceeded . Please put the uuid and imageCode.");
        }else{

            // 执行方法
            Object proceed = joinPoint.proceed();
            return proceed;
        }




    }
}

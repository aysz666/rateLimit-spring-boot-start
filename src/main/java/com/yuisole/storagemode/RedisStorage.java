package com.yuisole.storagemode;

import cn.hutool.extra.spring.SpringUtil;
import com.yuisole.config.RateLimitProperties;
import com.yuisole.storagebase.Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @author Yuisole
 */
public class RedisStorage implements Storage {

    private final RedisTemplate<String, String> redisTemplate;

    private final RateLimitProperties properties;

    private  RedisStorage(RateLimitProperties properties) {
        this.properties = properties;
        this.redisTemplate = SpringUtil.getBean(StringRedisTemplate.class);
    }
    private static volatile RedisStorage redisStorage;

    public static RedisStorage getInstance(RateLimitProperties properties){
        if(redisStorage == null){
            synchronized (RedisStorage.class){
                if(redisStorage == null){
                    redisStorage = new RedisStorage(properties);
                }
            }
        }
        return redisStorage;
    }


    @Override
    public Integer getRequestCount(String key) {
        Long size = redisTemplate.opsForZSet().zCard(properties.getCountPrefix()+key);
        return size != null ? size.intValue() : 0;
    }

    @Override
    public void removeRequestCount(String key) {
        redisTemplate.delete(properties.getCountPrefix()+key);
    }

    @Override
    public void setCooldown(String key, long cooldown) {
        redisTemplate.opsForValue().set( properties.getCoolPrefix()+key, String.valueOf(System.currentTimeMillis() + cooldown * 1000), cooldown, TimeUnit.SECONDS);
    }

    @Override
    public long getCooldown(String key) {
        String value = redisTemplate.opsForValue().get(properties.getCoolPrefix()+key);
        return value != null ? Long.parseLong(value)  : 0;
    }

    @Override
    public void filterRequestsBefore(String key, long timestamp) {
        redisTemplate.opsForZSet().removeRangeByScore(properties.getCountPrefix()+key, 0, timestamp);
    }

    @Override
    public void incrementRequestCount(String key) {
        long currentTime = System.currentTimeMillis() / 1000;
        redisTemplate.opsForZSet().add(properties.getCountPrefix()+key, String.valueOf(currentTime), currentTime);
    }

    @Override
    public void saveImageCode(String key, String text,long timestamp) {
        redisTemplate.opsForValue().set(properties.getCodePrefix()+key,text,timestamp,TimeUnit.SECONDS);
    }

    @Override
    public boolean validateCaptcha(String key, String uuid, String text) {
        String newKey = key + ":" + uuid;
        Object redisValue =  redisTemplate.opsForValue().get(properties.getCodePrefix()+newKey);
        // 默认编码
        String newText = null;
        if (redisValue != null) {
            newText = redisValue.toString();
        }
        boolean b = text.equals(newText);

        if(b){
            redisTemplate.delete(properties.getCodePrefix()+newKey);
            redisTemplate.delete(properties.getCoolPrefix()+key);
        }
        return b;
    }

}

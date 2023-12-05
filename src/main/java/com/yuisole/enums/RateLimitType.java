package com.yuisole.enums;

/**
 * @author Yuisole
 */

public enum RateLimitType {
    /**
     * 通过ip进行防刷
     */
    IP,
    /**
     * 通过token进行防刷
     */
    TOKEN,
    /**
     * 通过ip与token的组合进行防刷
     */
    IP_AND_TOKEN,
    /**
     * 默认接口限流
     */
    DEFAULT
}
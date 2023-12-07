package com.yuisole.annotation;


import com.yuisole.enums.RateLimitType;

import java.lang.annotation.*;

/**
 *
 * @author Yuisole
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimited {
    RateLimitType type() default RateLimitType.DEFAULT;
    //默认允许访问的次数
    int value() default 10;
    //默认时间窗口s
    long duration() default 30;
    //冷静时间s
    long cooldown() default 60;
    //是否通过验证码校验
    boolean captcha() default false;
}

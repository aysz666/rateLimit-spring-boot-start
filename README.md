# ratelimit
## 简介
ratelimit 是一款配置简单，只需要在api上面打上注解，就能实现接口防刷的功能！`接口防刷，顾名思义，想让某个接口某个人在某段时间内只能请求N次，或者某个接口对外在某一段时间内只允许访问N次。`
这个组件提供了两种存储模式（分布式，单机），支持多种方式的防刷（ip，token，以及ip和token的组合）。支持图片验证码进行验证。

## 特性
1.使用简单，引入依赖，打上注解即可
2.支持ip，token，ipANDtoken的方式防刷
3.支持redis，以及local的存储

## 使用教程

引入依赖
```xml
    <dependency>
        <groupId>io.github.yuisole</groupId>
        <artifactId>rateLimit-spring-boot-start</artifactId>
        <version>1.0.0</version>
    </dependency>
    
```
注意：springboot版本需要2.7以上才可以实现自动注入，否则注解不生效

使用说明
1.application.yml的配置
```yml
  storage:
      mode: "local"  #存储模式，redis（需要做redis的连接配置） or local（默认）
      coolPrefix: "raleLimit_Cool_Key"  #默认即可
      countPrefix: "raleLimit_Count_Key" #默认即可
      codePrefix: "raleLimit_Code_Key"  #默认即可
      tokenName: "token"    #请求头token的名字
```

2.在接口上打上默认注解 @RateLimited
```java
    //不加参数，默认是local存储，token名字默认是token，默认接口名字防刷（相对于限流）
    @GetMapping("/")
    @RateLimited
    public String test(){
        return "成功！";
    }
```
3.使用ip方式进行防刷
```java
    //将注解参数设置type = RateLimitType.IP即可
    @GetMapping("/")
    @RateLimited(type = RateLimitType.IP)
    public String test(){
        return "成功！";
    }
```
4.使用token进行防刷
```java
    //将注解参数设置type = RateLimitType.TOKEN即可
    @GetMapping("/")
    @RateLimited(type = RateLimitType.TOKEN)
    public String test(){
        return "成功！";
    }
    //要指定application配置文件里面storage.tokenName的值，否则默认为token
```
5.使用ip+token的方式防刷
```java
    //将注解参数设置type = RateLimitType.TOKEN即可
    @GetMapping("/")
    @RateLimited(type = RateLimitType.IP_AND_TOKEN)
    public String test(){
        return "成功！";
    }
    //要指定application配置文件里面storage.tokenName的值，否则默认为token
```
6.注解的可选参数
```java
    //value，允许在指定时间段访问的次数，默认10
    @RateLimited(value = 10)

    //duration，时间段，默认30s
    @RateLimited(duration = 30) 

    //cooldown，冷静时间，当访问限制后，需要等待cooldown的时间后回复，默认60s
    @RateLimited(cooldown = 60)

    //captcha,是否开启图片验证
    //当访问限制后，会在response的header里面传入uuid，imageCode（base64），直接放在img标签即可显示
    //下次请求header需要带上uuid，以及imageCode的内容，校验成功后会清零cooldown，恢复访问
    @RateLimited(captcha = true)
```
7.全局异常拦截
```java
    /**
     * 频率异常
     * @param e 异常
     * @return 返回信息
     */
    @ExceptionHandler(value = RateLimitExceededException.class)
    public ApiResult<?> ratelimit(RateLimitExceededException e){
        log.info("Business exception! the reason is ：{}",e.getMessage());
        return ApiResult.fail(CommonErrorEnum.LOCK_LIMIT);
    }
```

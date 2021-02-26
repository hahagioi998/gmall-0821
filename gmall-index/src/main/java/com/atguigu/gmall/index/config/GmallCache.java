package com.atguigu.gmall.index.config;

import java.lang.annotation.*;

//元注解从@Transactional中复制
@Target({ElementType.TYPE, ElementType.METHOD})//注解作用在方法上
@Retention(RetentionPolicy.RUNTIME)//运行时注解
@Documented
public @interface GmallCache {

    //为何自定义注解中的变量名都是以()结尾的，因为这些变量是提供给用户自己去设置值的

    /**
     * 缓存的前缀
     * 将来缓存的key：prefix + 方法参数（比如lock）
     * @return
     */
    String prefix() default "";

    /**
     * 缓存的过期时间
     * 单位是min
     * @return
     */
    int timeout() default 5;

    /**
     * 为了防止缓存雪崩，可以让用户指定随机值范围
     * @return
     */
    int random() default 5;

    /**
     * 为了防止缓存击穿，添加分布式锁
     * 此处需要指定分布式锁的前缀
     * @return
     */
    String lock() default "lock:";



}

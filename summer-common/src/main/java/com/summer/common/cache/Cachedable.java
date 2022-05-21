package com.summer.common.cache;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cachedable {
    /** 缓存执行操作 **/
    Command cmd();

    /** 缓存时长(单位： 秒), 0-永久缓存, Command.InGet 时启作用 **/
    int expire() default 0;

    /** 禁用缓存， 作用于方法级 **/
    boolean disabled() default false;

    /** 缓存KEY生成器 **/
    Class<? extends CachedStrategy> clz();
}

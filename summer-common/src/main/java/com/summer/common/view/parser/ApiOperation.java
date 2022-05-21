package com.summer.common.view.parser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiOperation {
    /** API名称 **/
    String name();
    /** 是否记录日志 **/
    boolean note() default true;
    /** 是否需要认证 **/
    boolean permit() default true;
}

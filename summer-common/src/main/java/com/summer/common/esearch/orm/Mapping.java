package com.summer.common.esearch.orm;


import com.summer.common.esearch.IDynamicES;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Mapping {
    /** ES操作 **/
    String operation() default IDynamicES.DEFAULT;

    /** INDICES 类似于库,当为空时取 name 的值 **/
    String indices() default "";
    /** INDEX HASH数 **/
    int partitions() default 1;
    /** INDEX 分片数 **/
    int shards() default 3;
    /** INDEX 备份数 **/
    int replicas() default 2;

    /** 映射类型名  类似于表 **/
    String name();

    boolean dynamic() default false;

    /** 是否不区分大小写 false 区分，true 不区分 **/
    boolean sensitiveWord() default false;
}

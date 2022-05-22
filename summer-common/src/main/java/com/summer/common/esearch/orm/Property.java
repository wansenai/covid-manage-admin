package com.summer.common.esearch.orm;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {
    /**
     * 映射字段类型
     **/
    Typical type();

    /**
     * 是否加入全文检索
     **/
    boolean all() default false;

    /**
     * 1. 使用的分词器， 如： ik_max_word
     * 2. 注意： keyword 类型的可选值为 "" | "analyzed" | "not_analyzed" | "no"
     **/
    String analyzer() default "";

    /**
     * 字段描述
     **/
    String desc() default "";
}

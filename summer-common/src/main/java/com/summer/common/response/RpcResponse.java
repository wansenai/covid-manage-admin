package com.summer.common.response;

import com.summer.common.core.RpcReply;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 响应结构体为{@link RpcReply}，使用注解代替手动写响应结构
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcResponse {
}

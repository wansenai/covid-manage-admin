package com.summer.common.cache;

public enum Command {
    /** 创建|获取 缓存 **/
    InGet,
    /** 创建|获取列表批处理 缓存 **/
    Multi,
    /** 清除缓存 **/
    Evict,

    // 当为Multi时使用
    MultiKey,
    MultiVal
}

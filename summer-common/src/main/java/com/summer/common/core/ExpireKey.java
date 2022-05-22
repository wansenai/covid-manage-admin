package com.summer.common.core;

import java.util.concurrent.TimeUnit;

/**
 * 缓存过期键值
 **/
public enum ExpireKey {
    Forever(0, TimeUnit.MILLISECONDS, "不过期"),
    Seconds1(1, TimeUnit.SECONDS, "1秒"),
    Seconds30(30, TimeUnit.SECONDS, "30秒"),
    Minutes1(60, TimeUnit.SECONDS, "1分钟"),
    Minutes5(300, TimeUnit.SECONDS, "5分钟"),
    Minutes15(900, TimeUnit.SECONDS, "15分钟"),
    Minutes30(1800, TimeUnit.SECONDS, "30分钟"),
    Hours1(3600, TimeUnit.SECONDS, "1小时"),
    Hours12(43200, TimeUnit.SECONDS, "12小时"),
    Days1(86400, TimeUnit.SECONDS, "1天"),
    Days3(259200, TimeUnit.SECONDS, "3天"),
    Days7(604800, TimeUnit.SECONDS, "7天");

    public final int expire;
    public final TimeUnit unit;

    ExpireKey(int expire, TimeUnit unit, String desc) {
        this.expire = expire;
        this.unit = unit;
    }
}

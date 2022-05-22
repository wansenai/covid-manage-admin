package com.summer.common.support;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public abstract class PoolConfig {
    private final static GenericObjectPoolConfig CONF = new GenericObjectPoolConfig();

    static {
        CONF.setTimeBetweenEvictionRunsMillis(60 * 1000L);
        CONF.setSoftMinEvictableIdleTimeMillis(100 * 1000L);
        CONF.setTestOnBorrow(true);
        CONF.setTestWhileIdle(true);
        CONF.setMaxWaitMillis(3000L);
        CONF.setNumTestsPerEvictionRun(16);
    }

    public static <T> GenericObjectPoolConfig<T> of(int maxIdle) {
        CONF.setMinIdle(2);
        CONF.setMaxIdle(maxIdle);
        CONF.setMaxTotal(maxIdle + 5);
        //noinspection unchecked
        return (GenericObjectPoolConfig<T>) CONF;
    }
}

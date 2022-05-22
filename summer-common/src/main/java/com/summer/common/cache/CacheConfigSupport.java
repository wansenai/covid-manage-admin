package com.summer.common.cache;

import com.summer.common.redis.JedisCmdHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.inject.Inject;

public abstract class CacheConfigSupport {
    /**
     * Redis连接地址  REDIS_URI={SCHEME}://{NAME}:{PWD}@{HOST:PORT},{HOST:PORT}/{DB}
     * SCHEME=[shard | shards | sentinel | cluster | clusters]
     */
    protected abstract String redisUri(Environment env);

    /**
     * 禁用缓存， 作用于全局级
     **/
    protected boolean disabled() {
        return false;
    }

    @Bean
    @Inject
    public CacheInterceptor cachingInterceptor(Environment env) {
        // 禁用缓存
        if (disabled()) {
            return new CacheInterceptor(null);
        }
        return new CacheInterceptor(JedisCmdHelper.createRedisOperations(redisUri(env)));
    }
}

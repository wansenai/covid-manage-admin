package com.summer.common.redis;

import com.summer.common.helper.SpringHelper;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.util.Map;

public abstract class RedisConfigSupport {
    protected RedisConfigSupport() {
        addMultiRedisOperations(SpringHelper.getEnvironment(), RedisFactory.REDIS_MAP);
    }

    /**
     * 添加多个Redis操作
     **/
    protected abstract void addMultiRedisOperations(Environment env, Map<String, RedisOperations> redisMap);

    /**
     * Redis连接地址  REDIS_URI={SCHEME}://{NAME}:{PWD}@{HOST:PORT},{HOST:PORT}/{DB}
     * SCHEME=[shard | shards | sentinel | cluster | clusters]
     */
    protected RedisOperations newborn(final String uri) {
        return JedisCmdHelper.createRedisOperations(uri);
    }

    @Bean
    public RedisHealth redisHealth() {
        return new RedisHealth();
    }

    public static class RedisHealth implements HealthIndicator {
        @Override
        public Health health() {
            Health.Builder builder = Health.up();
            for (Map.Entry<String, RedisOperations> entry : RedisFactory.REDIS_MAP.entrySet()) {
                builder.withDetail(entry.getKey(), entry.getValue().isOk());
            }
            return builder.build();
        }
    }
}

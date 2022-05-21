package com.summer.common.redis;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.summer.common.support.PoolConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;

import java.util.List;
import java.util.Set;

public final class JedisCmdHelper {
    private static final int TIMEOUT = 3000, RETRIES = 5, POOL = 255;

    private JedisCmdHelper() {}

    /**
     * Redis连接地址  REDIS_URI={SCHEME}://{NAME}:{PWD}@{HOST:PORT},{HOST:PORT}/{DB}
     * SCHEME=[single | singles | shard | shards | sentinel | cluster | clusters]
     */
    public static RedisOperations createRedisOperations(String uri) {
        RedisFactory.UriInfo info = new RedisFactory.UriInfo(uri);
        // 单机模式
        if (uri.startsWith("single")) {
            return new RedisOperations(JedisCmdHelper.createSingleJedisPool(info), null);
        }
        // 分片模式
        if (uri.startsWith("shard")) {
            return new RedisOperations(JedisCmdHelper.createShardedJedisPool(info), null);
        }
        // 哨兵模式
        else if (uri.startsWith("sentinel")) {
            return new RedisOperations(JedisCmdHelper.createShardedJedisPool(info), null);
        }
        // 集群模式
        else if (uri.startsWith("cluster")) {
            return new RedisOperations(null, JedisCmdHelper.createJedisCluster(info));
        } else {
            throw new RedisException("Redis uri scheme error, please select in [single | singles | shard | shards | sentinel | cluster | clusters] ");
        }
    }

    private static JedisPool createSingleJedisPool(RedisFactory.UriInfo info) {
        HostAndPort hap = info.hapSet.iterator().next();
        return new JedisPool(PoolConfig.of(POOL), hap.getHost(), hap.getPort(), TIMEOUT, info.password, info.dbIdx, info.name, info.ssl);
    }

    private static ShardedJedisPool createShardedJedisPool(RedisFactory.UriInfo info) {
        List<JedisShardInfo> shards = Lists.newArrayList();
        for(HostAndPort hap: info.hapSet) {
            shards.add(RedisFactory.shardInfo(hap.getHost(), hap.getPort(), info.name, info.ssl, info.dbIdx, info.password)
                                   .ofTimeout(TIMEOUT));
        }
        return new ShardedJedisPool(PoolConfig.of(POOL), shards);
    }

    private static JedisSentinelPool createJedisSentinelPool(RedisFactory.UriInfo info) {
        Set<String> sentinels = Sets.newHashSet();
        for(HostAndPort hap: info.hapSet) {
            sentinels.add(hap.toString());
        }
        return new JedisSentinelPool(info.name, sentinels, PoolConfig.of(POOL), TIMEOUT, info.password, info.dbIdx);
    }

    private static JedisCluster createJedisCluster(RedisFactory.UriInfo info) {
        return new JedisCluster(info.hapSet, TIMEOUT, TIMEOUT, RETRIES, info.password, PoolConfig.of(POOL));
    }
}

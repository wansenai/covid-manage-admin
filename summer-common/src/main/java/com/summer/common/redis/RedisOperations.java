package com.summer.common.redis;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.summer.common.core.CacheSerialize;
import com.summer.common.helper.CollectsHelper;
import com.summer.common.helper.DateHelper;
import com.summer.common.helper.JsonHelper;
import com.summer.common.helper.BytesHelper;
import com.summer.common.helper.StringHelper;
import javafx.util.Pair;
import org.apache.ibatis.cache.CacheException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.Tuple;
import redis.clients.util.Pool;

import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/** Redis 缓存操作类 **/
public class RedisOperations {
    private static final String VAL_KEY = "val", TYPE_KEY = "vt", TIME_LUA = "local a=redis.call('TIME');return a[1]*1000000+a[2]";

    private final boolean cluster;
    private final JedisCluster jedisC;
    private final Pool<? extends JedisCommands> pool;

    RedisOperations(Pool<? extends JedisCommands> pool, JedisCluster jedisC) {
        this.pool = pool;
        this.jedisC = jedisC;
        this.cluster = null == pool;
    }

    /** 获取 REDIS 匹配的KEY **/
    public Set<String> keysGet(String patten) {
        JedisCommands redis = null;
        try {
            redis = redisCommands();
            if (redis instanceof Jedis) {
                return ((Jedis) redis).keys(patten);
            }
            return Sets.newHashSet();
        } finally {
            if(!cluster && null != redis) {
                BytesHelper.close((Closeable)redis);
            }
        }
    }
    /**更新过期时间**/
    public boolean expire(final String key, int expire){
        JedisCommands redis = null;
        try {
            redis = redisCommands();
            if (redis instanceof Jedis) {
                redis.expire(key,expire);
                return true;
            }
            return false;
        }finally {
            if (!cluster && null != redis) {
                BytesHelper.close((Closeable)redis);
            }
        }
    }

    /** 获取 REDIS 毫秒时间  **/
    public long timeGet() {
        JedisCommands redis = null;
        try {
            redis = redisCommands();
            if (redis instanceof Jedis) {
                String time = String.valueOf(((Jedis)redis).eval(TIME_LUA));
                if (time.length() == 16 && StringHelper.isNumeric(time)) {
                    return Long.parseLong(time.substring(0, time.length() -3));
                }
            }
            return DateHelper.time();
        } finally {
            if(!cluster && null != redis) {
                BytesHelper.close((Closeable)redis);
            }
        }
    }

    /** 获取 REDIS 秒时间  **/
    public long secondGet() {
        return timeGet() / DateHelper.SECOND_TIME;
    }

    /** 原子自增 **/
    public long incrGet(final String key, long step, int expire) {
        JedisCommands redis = null;
        try {
            redis = redisCommands();
            Long value = redis.incrBy(key, step);
            setExpire(redis,key,expire);
            return value;
        } finally {
            if(!cluster && null != redis) {
                BytesHelper.close((Closeable)redis);
            }
        }
    }

    /** 原子自增 **/
    public long incrGet(final String key, long step) {
        JedisCommands redis = null;
        try {
            redis = redisCommands();
            return redis.incrBy(key, step);
        } finally {
            if(!cluster && null != redis) {
                BytesHelper.close((Closeable)redis);
            }
        }
    }

    public long zAdd(final String key, Map<String,Double> map) {
        JedisCommands redis = null;
        try {
            redis = redisCommands();
            return redis.zadd(key, map);
        } finally {
            if(!cluster && null != redis) {
                BytesHelper.close((Closeable)redis);
            }
        }
    }

    public Set<Tuple> zRevrangeWithScores(final String key, long a, long b) {
        JedisCommands redis = null;
        try {
            redis = redisCommands();
            return redis.zrevrangeWithScores(key, a, b);
        } finally {
            if(!cluster && null != redis) {
                BytesHelper.close((Closeable)redis);
            }
        }
    }

    public long zRem(final String key,String value) {
        JedisCommands redis = null;
        try {
            redis = redisCommands();
            return redis.zrem(key,value);
        } finally {
            if(!cluster && null != redis) {
                BytesHelper.close((Closeable)redis);
            }
        }
    }
    public long zCard(final String key) {
        JedisCommands redis = null;
        try {
            redis = redisCommands();
            return redis.zcard(key);
        } finally {
            if(!cluster && null != redis) {
                BytesHelper.close((Closeable)redis);
            }
        }
    }

    public int zrevrank(final String key, String a) {
        JedisCommands redis = null;
        try {
            redis = redisCommands();
            Long zRevrank = redis.zrevrank(key, a);
            return zRevrank == null ? -1 : zRevrank.intValue();
        } finally {
            if(!cluster && null != redis) {
                BytesHelper.close((Closeable)redis);
            }
        }
    }

    /** 原子自减 **/
    public long decrGet(final String key, long start) {
        JedisCommands redis = null;
        try {
            redis = redisCommands();
            return redis.decrBy(key, start);
        } finally {
            if(!cluster && null != redis) {
                BytesHelper.close((Closeable)redis);
            }
        }
    }

    /** 写入缓存 **/
    public <T> void put(final String key, final T val, int expire) {
        JedisCommands redis = null;
        try {
            redis = redisCommands();
            if(null != val) {
                setRedisVal(redis, key, expire, RedisValue.newborn(val));
            }
        } finally {
            if(!cluster && null != redis) {
                BytesHelper.close((Closeable)redis);
            }
        }
    }
    /** 读取单个缓存数据 **/
    public <T> T one(final String key, Class<T> clazz) {
        JedisCommands redis = null;
        try {
            redis = redisCommands();
            return oneGet(key, clazz, redis);
        } finally {
            if(!cluster && null != redis) {
                BytesHelper.close((Closeable)redis);
            }
        }
    }

    /** 读取多个缓存数据 **/
    public <T> List<T> multi(final Set<String> keys, Class<T> clazz) {
        List<T> listT = Lists.newArrayList();
        if(!CollectsHelper.isNullOrEmpty(keys)) {
            JedisCommands redis = null;
            try {
                redis = redisCommands();
                for(String key: keys) {
                    T dt = oneGet(key, clazz, redis);
                    if(dt != null) { listT.add(dt); }
                }
            } finally {
                if(!cluster && null != redis) {
                    BytesHelper.close((Closeable)redis);
                }
            }
        }
        return listT;
    }
    /** 读取集合缓存数据 **/
    public <T> List<T> list(final String key, Class<T> clazz) {
        JedisCommands redis = null;
        try {
            redis = redisCommands();
            Map<String, String> rvMap = redis.hgetAll(key);
            if (!CollectsHelper.isNullOrEmpty(rvMap) && rvMap.containsKey(TYPE_KEY) && rvMap.containsKey(VAL_KEY)) {
                if (RedisValue.VT.Collection.name().equals(rvMap.get(TYPE_KEY))) {
                    String serializeKey = serializableKey(rvMap.get(VAL_KEY));
                    return JsonHelper.parseArray(redis.get(serializeKey), clazz);
                }
                throw new CacheException("The key=" + key + " cache is collection please use one method...");
            } else {
                return Lists.newArrayList();
            }
        } finally {
            if(!cluster && null != redis) {
                BytesHelper.close((Closeable)redis);
            }
        }
    }

    /** 清除缓存 **/
    public int clearAtomic(String ...keys) {
        int deletes = 0;
        JedisCommands redis = null;
        try {
            redis = redisCommands();
            for (String key: keys) {
                redis.del(key);
            }
        } finally {
            if(!cluster && null != redis) {
                BytesHelper.close((Closeable)redis);
            }
        }
        return deletes;
    }
    public int clear(String ...keys) {
        int deletes = 0;
        if(CollectsHelper.isNullOrEmpty(keys)) {
            return deletes;
        }
        JedisCommands redis = null;
        try {
            redis = redisCommands();
            for(String key: keys) {
                Map<String, String> rvMap = redis.hgetAll(key);
                deletes += redis.del(key);
                if(!RedisValue.VT.Primitive.name().equals(rvMap.get(TYPE_KEY))) {
                    redis.del(serializableKey(rvMap.get(VAL_KEY)));
                }
            }
        } finally {
            if(!cluster && null != redis) {
                BytesHelper.close((Closeable)redis);
            }
        }
        return deletes;
    }

    /** Multi 类的缓存使用， 返回未中缓存的KEY和结果列表 **/
    public Pair<Set<String>, List<Object>> multiGet(final List<String> keys, Class<?> clazz) {
        List<Object> listT = Lists.newArrayList();
        if(!CollectsHelper.isNullOrEmpty(keys)) {
            JedisCommands redis = null;
            try {
                redis = redisCommands();
                Set<String> overflow = Sets.newHashSet();
                for(String key: keys) {
                    Object dt = oneGet(key, clazz, redis);
                    if(dt != null) {
                        listT.add(dt);
                    } else {
                        overflow.add(key);
                    }
                }
                return new Pair<>(overflow, listT);
            } finally {
                if(!cluster && null != redis) {
                    BytesHelper.close((Closeable)redis);
                }
            }
        }
        return new Pair<>(Sets.newHashSet(keys), listT);
    }

    private <T> T oneGet(String key, Class<T> clazz, JedisCommands redis) {
        Map<String, String> rvMap = redis.hgetAll(key);
        if(!CollectsHelper.isNullOrEmpty(rvMap) && rvMap.containsKey(TYPE_KEY) && rvMap.containsKey(VAL_KEY)) {
            if (RedisValue.VT.Primitive.name().equals(rvMap.get(TYPE_KEY))) {
                return JsonHelper.parseObject(rvMap.get(VAL_KEY), clazz);
            }
            if (RedisValue.VT.Composite.name().equals(rvMap.get(TYPE_KEY))) {
                String serializeKey = serializableKey(rvMap.get(VAL_KEY));
                return JsonHelper.parseObject(redis.get(serializeKey), clazz);
            }
            throw new CacheException("The key=" + key + " cache is collection please use list method...");
        } else {
            return null;
        }
    }

    // 设置缓存
    private void setRedisVal(JedisCommands redis, String key, int expire, RedisValue rv) {
        switch (rv.vt) {
            case Primitive:
                redis.hmset(key, ImmutableMap.of(VAL_KEY, JsonHelper.toJSONString(rv.val), TYPE_KEY, rv.vt.name()));
                break;
            case Composite:
            case Collection:
                String serializeId = ((CacheSerialize)rv.val).getSerializableId();
                redis.hmset(key, ImmutableMap.of(VAL_KEY, serializeId, TYPE_KEY, rv.vt.name()));
                String serializeKey = serializableKey(serializeId);
                redis.set(serializeKey, JsonHelper.toJSONString(rv.val));
                setExpire(redis, serializeKey, expire);
                break;
        }
        setExpire(redis, key, expire);
    }
    // 设置过期时间
    private void setExpire(JedisCommands redis, String key, int expire) {
        if(expire > 0) {
            redis.expire(key, expire);
        }
    }

    boolean isOk() {
        return cluster ? null != jedisC : null != pool;
    }

    private String serializableKey(String serializableId) {
        return "@objects:" + serializableId;
    }

    private JedisCommands redisCommands() {
        if(cluster) {
            return jedisC;
        } else { //synchronized (pool) {
            JedisCommands jedis = null;
            for (int tries = 0; tries < 3; tries++) {
                try {
                    jedis = pool.getResource();
                    if (null != jedis) {
                        break;
                    }
                } catch (Exception e) {
                    try {
                        TimeUnit.SECONDS.sleep(1L);
                    } catch (InterruptedException ex) {
                        // continue;
                    }
                    if (2 == tries) {
                        throw new RedisException("Get jedis resource error ", e);
                    }
                }
            }
            if (null != jedis) {
                return jedis;
            } else {
                throw new RedisException("Could not get a resource from the pool...");
            }
        }
    }
    @PreDestroy @SuppressWarnings("unused")
    private void destroy() throws IOException {
        if(null != pool) {
            pool.close();
        }
        if(null != jedisC) {
            jedisC.close();
        }
    }

    /** Multi 类的缓存使用， 返回未中缓存的KEY和结果列表,value为list**/
    public Pair<Set<String>, List<Object>> multiList(final List<String> keys, Class<?> clazz) {
        List<Object> listT = Lists.newArrayList();
        if(!CollectsHelper.isNullOrEmpty(keys)) {
            JedisCommands redis = null;
            try {
                redis = redisCommands();
                Set<String> overflow = Sets.newHashSet();
                for(String key: keys) {
                    List dt = list(key, clazz);
                    if(dt != null) {
                        listT.add(dt);
                    } else {
                        overflow.add(key);
                    }
                }
                return new Pair<>(overflow, listT);
            } finally {
                if(!cluster && null != redis) {
                    BytesHelper.close((Closeable)redis);
                }
            }
        }
        return new Pair<>(Sets.newHashSet(keys), listT);
    }

//    public List<String> mget(final List<String> ids)  {
//        return jedisC.mget(ids.toArray(new String[ids.size()]));
//    }
//
//    public <T> List<T> mgetSingle(final List<String> keys , Class<T> clazz) {
//        List<T> listT = Lists.newArrayList();
//        if(!CollectsHelper.isNullOrEmpty(keys)) {
//            MultiKeyCommands redis = null;
//            try {
//                redis = multiPool.getResource();
//                List<String> results = redis.mget(keys.toArray(new String[keys.size()]));
//                if (!CollectsHelper.isNullOrEmpty(results)) {
//                   listT.addAll(results.stream().
//                                               filter(result -> null != result).
//                                               map(result -> JSONObject.parseObject(result).toJavaObject(clazz)).
//                                               collect(Collectors.toList()));
//                }
//            } finally {
//                if(!cluster && null != redis) {
//                    BytesHelper.close((Closeable)redis);
//                }
//            }
//        }
//        return listT;
//    }

    /**
     * 获取redis值无封装格式
     */
    public String getNoFormat(final String key) {
        JedisCommands redis = null;
        try {
            redis = redisCommands();
            return redis.get(key);
        } finally {
            if(!cluster && null != redis) {
                BytesHelper.close((Closeable)redis);
            }
        }
    }

    /**
     * 设置redis值无封装格式
     */
    public void setNoFormat(final String key,final String value,int expire) {
        JedisCommands redis = null;
        try {
            redis = redisCommands();
            redis.set(key,value);
            setExpire(redis, key, expire);
        } finally {
            if(!cluster && null != redis) {
                BytesHelper.close((Closeable)redis);
            }
        }
    }
}

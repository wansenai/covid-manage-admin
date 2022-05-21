package com.summer.common.cache;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.summer.common.core.CacheSerialize;
import com.summer.common.core.RpcException;
import com.summer.common.helper.BeanHelper;
import com.summer.common.helper.EncryptHelper;
import com.summer.common.helper.GenericHelper;
import com.summer.common.helper.BytesHelper;
import com.summer.common.helper.StringHelper;
import com.summer.common.redis.RedisOperations;
import javafx.util.Pair;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.FutureTask;
import java.util.function.Supplier;

@Aspect
@Order(Integer.MIN_VALUE)
public class CacheInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(CacheInterceptor.class);
    private static final Map<Method, Pair<Cachedable, ? extends CachedStrategy>> MCE_MAP = Maps.newHashMap();
    private final RedisOperations redisOperations;

    CacheInterceptor(RedisOperations redisOperations) {
        this.redisOperations = redisOperations;
    }

    @Around("execution(* (com.summer.common.cache.CachedMapper+).*(..)) || @annotation(com.summer.common.cache.Cachedable)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Method method = joinPointMethod(joinPoint);
            if (null == redisOperations) {
                return joinPoint.proceed();
            }
            //缓存操作
            Pair<Cachedable, ? extends CachedStrategy> ckm = maker(method);
            // 有注解切缓存启用（缓存类型启用 且 缓存方法启用）
            if (null != ckm && null != ckm.getKey() && null != ckm.getValue() && !ckm.getValue().disabled() && !ckm.getKey().disabled()) {
                switch (ckm.getKey().cmd()) {
                    case InGet:
                    case Multi:
                        return doInGet(joinPoint, method, ckm.getKey(), ckm.getValue());
                    case Evict:
                        return doEvict(joinPoint, method, ckm.getValue());
                }
            }
            return joinPoint.proceed();
        } catch (Throwable cause) {
            if (!(cause instanceof RpcException)) {
                LOG.warn("Cache aspect invoke error ", cause);
            }  throw cause;
        }
    }

    //先走缓存，如果缓存没有则查询数据库，如果缓存有则直接返回缓存数据
    private Object doInGet(ProceedingJoinPoint point, Method method, Cachedable exec, CachedStrategy stg) throws Throwable {
        Object[] args = point.getArgs();
        Class<?> rt = method.getReturnType();
        boolean optional = Optional.class.equals(rt);
        boolean collection = Collection.class.isAssignableFrom(rt);
        boolean single = !collection && BeanHelper.isPrimitiveType(rt);
        //noinspection unchecked
        boolean assignable = stg.cacheGenericType().isAssignableFrom(rt);
        boolean serializable = Serializable.class.equals(stg.cacheGenericType());
        // 返回 optional | 集合类型
        if(collection || optional) {
            Type genericType = method.getGenericReturnType();
            rt = GenericHelper.type(genericType);
        }
        boolean p = single || BeanHelper.isPrimitiveType(rt);
        // 类型相同 || 基础类型且serializable || 非基础类型且返回子类
        if(rt.equals(stg.cacheGenericType()) || (p && serializable && assignable) || (!p && !serializable && assignable)) {
            String typeName = stg.cacheName();
            List<String> realKeys = CachedStrategy.realKeys(typeName, stg.inGet(exec.cmd(), point.getSignature(), method, args));
            if(collection && Command.Multi == exec.cmd()) {
                Pair<Set<String>, List<Object>> pair;
                try {
                    pair = redisOperations.multiGet(realKeys, rt);
                } catch (Exception e) {
                    LOG.warn("MultiGet from redis cache error ", e);
                    pair = new Pair<>(Sets.newHashSet(realKeys), Lists.newArrayList());
                }
                Set<String> overflow = pair.getKey(); List<Object> objects = pair.getValue();
                if(overflow.size() > 0) {
                    @SuppressWarnings("unchecked")
                    Supplier<Map<String, ?>> supplier = () -> stg.multiRs(typeName, point, method, args, overflow);
                    String callerS = point.getSignature().getDeclaringTypeName() + overflow.toString();
                    String callerKey = EncryptHelper.toHex(BytesHelper.utf8Bytes(callerS));
                    Object rs = StormTask.apply(callerKey, multiCaller(typeName, exec.expire(), overflow, supplier));
                    if(!isNone(rs)) {
                        objects.addAll((Collection<?>)rs);
                    }
                }
                return objects;
            } else {
                String realKey = realKeys.size() > 0 ? realKeys.get(0) : StringHelper.EMPTY;
                Object value = valueFromCache(realKey, rt, collection);
                // 缓存没有则执行后序服务, StormTask 防止缓存穿透导致后端服务压力雪崩问题
                return isNone(value) ? StormTask.apply(realKey, caller(point, exec.expire(), args, realKey)) : optVal(optional, value);
            }
        } else {
            throw new CacheException("Cache value generic type=" + stg.cacheGenericType().getSimpleName()
                                             + " but method " + method.getName() + " generic return type=" + rt.getSimpleName());
        }
    }

    private boolean isNone(Object value) {
        return null == value
                || ((value instanceof String) && ((String)value).length() < 1)
                || ((value instanceof Collection) && ((Collection<?>)value).isEmpty());
    }

    private Object optVal(boolean optional, Object value) {
        return optional ? Optional.ofNullable(value) : value;
    }

    // 先执行业务, 再清空缓存
    private Object doEvict(ProceedingJoinPoint point, Method method, CachedStrategy stg) throws Throwable {
        Object[] args = point.getArgs();
        Object result = point.proceed(args);
        String[] keys = stg.evict(point.getSignature(), method, point.getArgs(), result);
        redisOperations.clear(CachedStrategy.realKeys(stg.cacheName(), keys).toArray(new String[0]));
        return result;
    }
    // 获取缓存数据
    private Object valueFromCache(String key, Class<?> type, boolean collection) {
        if(StringHelper.isBlank(key)) {
            return null;
        }
        // 返回列表数据
        if (collection) {
            return redisOperations.list(key, type);
        }
        // 返回单个数据
        else {
            return redisOperations.one(key, type);
        }
    }

    private Method joinPointMethod(ProceedingJoinPoint point) {
        return ((MethodSignature)point.getSignature()).getMethod();
    }

    private Pair<Cachedable, ? extends CachedStrategy> maker(Method method) throws IllegalAccessException, InstantiationException {
        Pair<Cachedable, ? extends CachedStrategy> ckm = MCE_MAP.get(method);
        if(null == ckm) {
            synchronized (MCE_MAP) {
                ckm = MCE_MAP.get(method);
                if (null == ckm) {
                    Cachedable cable = method.getAnnotation(Cachedable.class);
                    // 有注解且缓存非禁用
                    if (null != cable && !cable.disabled()) {
                        // 当需要击中缓存时，方法返回值不能为void
                        if(Command.InGet == cable.cmd()) {
                            Class<?> returnType = method.getReturnType();
                            if (Void.TYPE.equals(returnType)) {
                                ckm = new Pair<>(null, null);
                            } else {
                                ckm = new Pair<>(cable, cable.clz().newInstance());
                            }
                        } else {
                            ckm = new Pair<>(cable, cable.clz().newInstance());
                        }
                    } else {
                        ckm = new Pair<>(null, null);
                    }
                    MCE_MAP.put(method, ckm);
                }
            }
        }
        return ckm;
    }

    private Callable<Object> multiCaller(String typeName, int expire, Set<String> within, Supplier<Map<String, ?>> supplier) {
        return () -> {
            try {
                Map<String, ?> rsMap = supplier.get();
                if(null != rsMap && within.size() > 0) {
                    for(Map.Entry<String, ?> entry: rsMap.entrySet()) {
                        String key = CachedStrategy.realKeys(typeName, entry.getKey()).get(0);
                        caching(expire, key, entry.getValue());
                    }
                    return rsMap.values();
                } else {
                    return Collections.EMPTY_LIST;
                }
            } catch (Throwable cause) {
                return cause;
            }
        };
    }

    private Callable<Object> caller(ProceedingJoinPoint point, int expire, Object[] args, String key) {
        return () -> {
            try {
                Object val = point.proceed(args);
                if(null != val && !StringHelper.isBlank(key)) {
                    caching(expire, key, val);
                }
                return val;
            } catch (Throwable cause) {
                return cause;
            }
        };
    }

    private void caching(int expire, String key, Object val) {
        // 返回 optional
        if (val instanceof Optional) {
            if (((Optional<?>) val).isPresent()) {
                redisOperations.put(key, ((Optional<?>) val).get(), expire);
            }
        }
        // 返回集合数据
        else if (val instanceof Collection) {
            if (((Collection<?>) val).size() > 0) {
                redisOperations.put(key, new CacheSerialize.Flock<>((Collection<?>) val), expire);
            }
        }
        // 常规则数据返回
        else {
            redisOperations.put(key, val, expire);
        }
    }

    private static class StormTask {
        private static final ConcurrentMap<String, FutureTask<Object>> STORM_MAP = Maps.newConcurrentMap();
        static Object apply(String key, Callable<Object> caller) throws Throwable {
            String callerKey = StringHelper.isBlank(key) ? UUID.randomUUID().toString() : key;
            try {

                FutureTask<Object> future = STORM_MAP.get(callerKey);
                if (null == future) {
                    FutureTask<Object> task = new FutureTask<>(caller);
                    future = STORM_MAP.putIfAbsent(callerKey, task);
                    if (null == future) {
                        future = task; task.run();
                    }
                }
                Object result = future.get();
                if((result instanceof Throwable)) {
                    throw (Throwable)result;
                } else {
                    return result;
                }
            } finally{
                STORM_MAP.remove(callerKey);
            }
        }
    }
}

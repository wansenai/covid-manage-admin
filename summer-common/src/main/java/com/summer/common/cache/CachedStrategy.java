package com.summer.common.cache;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.summer.common.helper.CollectsHelper;
import com.summer.common.helper.GenericHelper;
import com.summer.common.helper.StringHelper;
import javafx.util.Pair;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class CachedStrategy<T> {
    private static final Logger LOG = LoggerFactory.getLogger(CachedStrategy.class);
    private static final Map<String, Pair<Boolean, Method>> mmMap = Maps.newHashMap();
    private final Class<T> type;
    private final Class<?> thisT;
    private final String typeName;
    protected CachedStrategy() {
        this.thisT = this.getClass();
        this.type = GenericHelper.type(thisT);
        this.typeName = type.getSimpleName();
    }

    /** 缓存值类型 **/
    Class<T> cacheGenericType() {
        return type;
    }
    /** 禁用缓存， 作用于缓存类型级 **/
    protected boolean disabled() {
        return false;
    }
    /** 缓存类型名, 可重写算定义缓存名 **/
    protected String cacheName() {
        return typeName;
    }

    // 创建|获取 缓存时的 KEY 列表
    String[] inGet(Command cmd, Signature signature, Method method, Object[] args){
        String mName = callName(cmd, signature, method);
        return invoke(args, method(cmd, mName, method));
    }
    // 批量查询 未中缓存则后序业务处理
    Map<String, T> multiRs(String typeName, ProceedingJoinPoint point, Method method, Object[] args, Set<String> overflow) {
        List<Object> argList = Lists.newArrayList(args);
        argList.add(new Aop(typeName, overflow, point));
        String mName = callName(Command.MultiVal, point.getSignature(), method);
        return multiInvoke(argList.toArray(), method(Command.MultiVal, mName, method));
    }
    // 清除缓存时的 KEY 列表
    String[] evict(Signature signature, Method method, Object[] args, Object result){
        List<Object> argList = Lists.newArrayList(args); argList.add(result);
        String mName = callName(Command.Evict, signature, method);
        return invoke(argList.toArray(), method(Command.Evict, mName, method));
    }
    // KEY真实值
    static List<String> realKeys(String typeName, String ...keys)  {
        List<String> keyList = Lists.newArrayList();
        if(!CollectsHelper.isNullOrEmpty(keys)) {
            for (String key : keys) {
                if(!StringHelper.isBlank(key)) {
                    keyList.add(typeName + ":" + key);
                }
            }
        }
        return keyList;
    }

    private Pair<Boolean, Method> method(Command cmd, String mName, Method method) {
        Pair<Boolean, Method> pair = mmMap.get(mName);
        if (null == pair) synchronized (mmMap) {
            pair = mmMap.get(mName);
            if (null == pair) {
                try {
                    Class<?>[] types = method.getParameterTypes();
                    if (Command.Evict == cmd) {
                        List<Class<?>> pts = Lists.newArrayList(types);
                        Class<?> resultType = method.getReturnType();
                        pts.add(Void.TYPE.equals(resultType) ? Void.class : resultType);
                        types = pts.toArray(new Class[0]);
                    } else if(Command.MultiVal == cmd) {
                        List<Class<?>> pts = Lists.newArrayList(types);
                        pts.add(Aop.class); types = pts.toArray(new Class[0]);
                    }
                    pair = new Pair<>(true, thisT.getMethod(mName, types));
                } catch (Exception e) {
                    LOG.warn("Find cache key method={} error={}", mName, e);
                    pair = new Pair<>(false, null);
                }
                mmMap.put(mName, pair);
            }
        }
        return pair;
    }

    private String[] invoke(Object[] args, Pair<Boolean, Method> pair) {
        if(pair.getKey()) {
            Class<?> rt = pair.getValue().getReturnType();
            if (rt.equals(String[].class)) {
                try {
                    return (String[]) (pair.getValue().invoke(this, args));
                } catch (Exception e) {
                    LOG.warn("Generate cache keys by method={} error={}", pair.getValue().getName(), e.getMessage());
                }
            } else {
                LOG.warn("Method={} should declare return String[] but {}", pair.getValue().getName(), rt.getSimpleName());
            }
        }
        return new String[0];
    }

    private Map<String, T> multiInvoke(Object[] args, Pair<Boolean, Method> pair) {
        if(pair.getKey()) {
            Class<?> rc = pair.getValue().getReturnType();
            Type rt = pair.getValue().getGenericReturnType();
            Class<?> keyC = GenericHelper.type(rt); Class<?> valC = GenericHelper.type(rt, 1);
            if (Map.class.isAssignableFrom(rc) && String.class.equals(keyC) && type.isAssignableFrom(valC)) {
                try {
                    //noinspection unchecked
                    return (Map<String, T>) (pair.getValue().invoke(this, args));
                } catch (Exception e) {
                    LOG.warn("Generate cache keys by method={} error={}", pair.getValue().getName(), e.getMessage());
                }
            } else {
                String returnS = String.format("java.util.Map<String, %s>", type.getName());
                LOG.warn("Method={} should declare return {} but {}", pair.getValue().getName(), returnS, rt.getTypeName());
            }
        }
        return Maps.newHashMap();
    }

    private String callName(Command cmd, Signature signature, Method method) {
        String name = signature.getDeclaringTypeName();
        int offset = name.lastIndexOf(".") + 1;
        String preName = Command.Multi == cmd ? Command.MultiKey.name() : cmd.name();
        return preName + "_" + name.substring(offset) + "_" + method.getName();
    }

    protected static class Aop {
        private final String typeName;
        private final Set<String> keys;
        private ProceedingJoinPoint point;
        Aop(String typeName, Set<String> keys, ProceedingJoinPoint point) {
            this.keys = keys;
            this.point = point;
            this.typeName = typeName;
        }
        public boolean sterilize(String key) {
            return null != keys && keys.contains(realKeys(typeName, key).get(0));
        }
        public <T> T proceed(Object[] args) throws Throwable {
            //noinspection unchecked
            return (T)point.proceed(args);
        }
    }
}

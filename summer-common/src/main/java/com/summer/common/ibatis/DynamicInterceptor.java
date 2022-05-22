package com.summer.common.ibatis;

import com.summer.common.core.ICodeMSG;
import com.summer.common.core.RpcException;
import com.summer.common.helper.StringHelper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DuplicateKeyException;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Aspect
@Order(Integer.MIN_VALUE + 100)
public class DynamicInterceptor {
    private static final String WITH_STG = "withSTG";

    @Around("execution(* (com.summer.common.ibatis.DynamicMapper+).*(..))")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        DynamicMapper mapper = (DynamicMapper) point.getTarget();
        Method method = joinPointMethod(point);
        try {
            if (WITH_STG.equals(method.getName())) {
                return point.proceed();
            }
            Object result = null;
            // 是否需要对 result 做 union 处理
            boolean needUnionReturn = needUnionReturn(method.getReturnType());
            Set<DynamicStrategy.Target> targets = strategy(mapper);
            for (DynamicStrategy.Target target : targets) {
                String originalDS = DataSourceManager.get().getDataSource();
                //如果DataSourceManager中的数据源就是最新的则不需要切换
                if (!target.datasource.equals(originalDS)) {
                    DataSourceManager.get().setDataSource(target.datasource);
                }
                for (String tableSuffix : target.tableSuffix) {
                    DataSourceManager.get().setTableSuffix(tableSuffix);
                    // union ResultSet
                    if (needUnionReturn) {
                        result = unionResult(result, point.proceed(point.getArgs()), method.getReturnType());
                    } else {
                        result = point.proceed(point.getArgs());
                    }
                }
            }
            return needUnionReturn ? (null == result ? unionDefaultReturn(method.getReturnType()) : result) : result;
        } catch (Throwable e) {
            throw newVerifyException(method, e);
        } finally {
            //每次使用完策略立即清除，以防止后面指定默认的策略不被应用
            if (!WITH_STG.equals(method.getName())) {
                DataSourceManager.get().setSTG(null);
            }
        }
    }

    //数据库异常处理
    private Throwable newVerifyException(Method method, Throwable e) {
        final DuplicateVerify verify = method.getAnnotation(DuplicateVerify.class);
        if (null != verify && (e instanceof DuplicateKeyException)) {
            return new RpcException(ICodeMSG.create(512, StringHelper.defaultIfBlank(verify.msg(), e.getCause().getMessage())));
        } else if (e instanceof DuplicateKeyException) {
            return new DuplicateKeyException(e.getMessage());
        } else {
            return e;
        }
    }

    //获取策略
    private LinkedHashSet<DynamicStrategy.Target> strategy(DynamicMapper mapper) {
        DynamicStrategy stg = DataSourceManager.get().getSTG();
        return null == stg ? mapper.defaultSTG().strategy() : stg.strategy();
    }

    private Method joinPointMethod(ProceedingJoinPoint point) {
        return ((MethodSignature) point.getSignature()).getMethod();
    }

    private Object unionResult(Object rs, Object proceed, Class<?> returnType) {
        if (null == proceed) {
            return rs;
        }
        if (isByte(returnType)) {
            return ((null == rs) ? 0 : (byte) rs) + (byte) proceed;
        } else if (isShort(returnType)) {
            return ((null == rs) ? 0 : (short) rs) + (short) proceed;
        } else if (isInt(returnType)) {
            return ((null == rs) ? 0 : (int) rs) + (int) proceed;
        } else if (isLong(returnType)) {
            return ((null == rs) ? 0 : (long) rs) + (long) proceed;
        } else if (isFloat(returnType)) {
            return ((null == rs) ? 0 : (float) rs) + (float) proceed;
        } else if (isDouble(returnType)) {
            return ((null == rs) ? 0 : (double) rs) + (double) proceed;
        } else if (isBigDecimal(returnType)) {
            return ((null == rs) ? BigDecimal.ZERO : ((BigDecimal) rs)).add((BigDecimal) proceed);
        } else if (isList(returnType)) {
            List<Object> rsList = new ArrayList<>();
            if (null != rs) {
                rsList.addAll((List) rs);
            }
            rsList.addAll((List) proceed);
            return rsList;
        } else {
            throw new RuntimeException("dynamic datasource invoke return type: " + returnType + " error");
        }
    }

    private Object unionDefaultReturn(Class<?> returnType) {
        return isBigDecimal(returnType) ? BigDecimal.ZERO : isList(returnType) ? new ArrayList<>() : 0;
    }

    private boolean needUnionReturn(Class<?> rt) {
        return isByte(rt) || isShort(rt) || isInt(rt) || isLong(rt) || isFloat(rt) || isDouble(rt) || isBigDecimal(rt) || isList(rt);
    }

    private boolean isByte(Class<?> returnType) {
        return byte.class.equals(returnType) || Byte.class.isAssignableFrom(returnType);
    }

    private boolean isShort(Class<?> returnType) {
        return short.class.equals(returnType) || Short.class.isAssignableFrom(returnType);
    }

    private boolean isInt(Class<?> returnType) {
        return int.class.equals(returnType) || Integer.class.isAssignableFrom(returnType);
    }

    private boolean isLong(Class<?> returnType) {
        return long.class.equals(returnType) || Long.class.isAssignableFrom(returnType);
    }

    private boolean isFloat(Class<?> returnType) {
        return float.class.equals(returnType) || Float.class.isAssignableFrom(returnType);
    }

    private boolean isDouble(Class<?> returnType) {
        return double.class.equals(returnType) || Double.class.isAssignableFrom(returnType);
    }

    private boolean isBigDecimal(Class<?> returnType) {
        return BigDecimal.class.isAssignableFrom(returnType);
    }

    private boolean isList(Class<?> returnType) {
        return List.class.isAssignableFrom(returnType);
    }
}

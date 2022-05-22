package com.summer.common.view;

import com.summer.common.core.IRequest;
import com.summer.common.core.RpcReply;
import com.summer.common.helper.CollectsHelper;
import com.summer.common.helper.JsonHelper;
import com.summer.common.helper.StringHelper;
import com.summer.common.helper.ThreadFactoryHelper;
import com.summer.common.support.IConstant;
import com.summer.common.support.OperationLog;
import com.summer.common.view.parser.ApiOperation;
import com.summer.common.view.parser.RequestContext;
import com.summer.common.view.parser.RequestSession;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Aspect
@Order(0)
public final class ApiOperationInterceptor {
    private static final Logger OUT = LoggerFactory.getLogger(OperationLog.class);
    private static final ExecutorService LOG_EXECUTOR = Executors.newSingleThreadExecutor(ThreadFactoryHelper.threadFactoryOf("operation-log"));
    private final ApplicationContext context;
    boolean logged = false;

    ApiOperationInterceptor(ApplicationContext context) {
        this.context = context;
        logged = "true".equalsIgnoreCase(context.getEnvironment().getProperty(IConstant.KEY_OPERATIONS_LOG_ENABLE));
    }

    /**
     * 记录操作日志
     **/
    public static void operationLog(final ProceedingJoinPoint joinPoint, final ApiOperation operation, final Object reply, boolean logged) {
        if (!logged) {
            return;
        }
        if (null != operation && !operation.note()) {
            return;
        }
        if (null != joinPoint) {
            final Method method = joinPointMethod(joinPoint);
            if (method.getReturnType().equals(Void.TYPE)) {
                return;
            }
        }
        final RequestSession session = RequestContext.get().getSession();
        if (null == session) {
            return;
        }
        session.operate = null == operation ? StringHelper.EMPTY : operation.name();
        final long endTime = System.currentTimeMillis();
        //异步处理操作日志
        LOG_EXECUTOR.submit(() -> {
            OperationLog log = OperationLog.newborn(session, endTime, null);
            if (null != session.icm && !session.apiIntercept) {
                log.setResponseBody(RpcReply.onFail(session.icm));
                OUT.info("{}", JsonHelper.toJSONString(log));
                return;
            }
            if (reply instanceof RpcReply) {
                log.setResponseBody(reply);
            } else if (reply instanceof Throwable) {
                log.setResponseBody(((Throwable) reply).getMessage());
            } else {
                log.setResponseBody(reply);
            }
            OUT.info("{}", JsonHelper.toJSONString(log));
        });
    }

    //获取拦截方法
    private static Method joinPointMethod(ProceedingJoinPoint joinPoint) {
        return ((MethodSignature) joinPoint.getSignature()).getMethod();
    }

    @Around("@annotation(operation)")
    public Object around(ProceedingJoinPoint joinPoint, ApiOperation operation) throws Throwable {
        Object response;
        Object[] args = joinPoint.getArgs();
        try {
            verifyArgs(args);
            response = joinPoint.proceed(joinPoint.getArgs());
            operationLog(joinPoint, operation, response, logged);
        } catch (Throwable e) {
            operationLog(joinPoint, operation, e, logged);
            throw e;
        }
        return response;
    }

    /**
     * 校验参数
     **/
    private void verifyArgs(Object[] args) {
        if (!CollectsHelper.isNullOrEmpty(args)) {
            for (Object arg : args)
                if (arg instanceof IRequest) {
                    ((IRequest) arg).verify();
                }
        }
    }
}

package com.summer.common.view;

import com.google.common.collect.Maps;
import com.summer.common.helper.StringHelper;
import com.summer.common.support.IConstant;
import com.summer.common.view.parser.RequestContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Map;

public class RequestMappingInterceptor implements HandlerInterceptor {
    private final Map<Method, String> moMap = Maps.newConcurrentMap();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();
        if (WebConfigurationSupport.GlobalControllerHandler.ERROR_PATH.equals(uri)
                || IConstant.isActuatorEndpoint(uri)
                || IConstant.isStaticsEndpoint(uri)
                || IConstant.isStreamEndpoint(uri)) {
            return true;
        }
        if (!(handler instanceof ResourceHttpRequestHandler)) {
            Method method = ((HandlerMethod) handler).getMethod();
            String operate = moMap.get(method);
            if (null == operate) {
                operate = operateGet((HandlerMethod) handler);
                moMap.put(method, operate);
            }
            RequestContext.get().getSession().operate = operate;
        }
        return true;
    }

    private String operateGet(HandlerMethod handler) {
        RequestMapping mapping = handler.getMethodAnnotation(RequestMapping.class);
        if (null != mapping) {
            return mapping.name();
        }
        PostMapping post = handler.getMethodAnnotation(PostMapping.class);
        if (null != post) {
            return post.name();
        }
        GetMapping get = handler.getMethodAnnotation(GetMapping.class);
        if (null != get) {
            return get.name();
        }
        return StringHelper.EMPTY;
    }
}

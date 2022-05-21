package com.summer.common.view;

import com.summer.common.view.parser.RequestContext;
import com.summer.common.view.parser.RequestSession;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class RequestSessionResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return !parameter.hasParameterAnnotations() && parameter.getParameterType().equals(RequestSession.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer container, NativeWebRequest request, WebDataBinderFactory binder) {
        return RequestContext.get().getSession();
    }
}

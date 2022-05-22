package com.summer.common.view;

import com.summer.common.helper.BeanHelper;
import com.summer.common.helper.StrCastHelper;
import com.summer.common.view.parser.Queries;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

public class RequestQueriesResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(Queries.class) != null && !BeanHelper.isPrimitiveType(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer container, NativeWebRequest request, WebDataBinderFactory binder) {
        HttpServletRequest servletRequest = request.getNativeRequest(HttpServletRequest.class);
        return StrCastHelper.form2Bean(servletRequest.getQueryString(), parameter.getParameterType());
    }
}

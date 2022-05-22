package com.summer.common.view;

import com.summer.common.helper.NetworkHelper;
import com.summer.common.view.parser.ClientIP;
import com.summer.common.view.parser.RequestContext;
import com.summer.common.view.parser.RequestSession;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

public class RequestClientIpResolver implements HandlerMethodArgumentResolver {
    private static final String EMPTY = "";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(ClientIP.class) != null;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer container, NativeWebRequest request, WebDataBinderFactory binder) throws Exception {
        // 先取 request session client ip
        RequestSession session = RequestContext.get().getSession();
        String clientIP = null == session ? EMPTY : session.clientIp;
        if (EMPTY.equals(clientIP)) {
            HttpServletRequest servletRequest = request.getNativeRequest(HttpServletRequest.class);
            clientIP = NetworkHelper.ofClientIp(servletRequest);
        }
        Class<?> type = parameter.getParameterType();
        if (String.class.equals(type)) {
            return clientIP;
        } else if (long.class.equals(type) || Long.class.isAssignableFrom(type)) {
            return NetworkHelper.ip2long(clientIP);
        } else {
            throw new ServletRequestBindingException("@ClientIP parameter should be declare as Strong or long or Long");
        }
    }
}

package com.summer.common.filter;

import com.summer.common.core.ResultSet;
import com.summer.common.core.RpcReply;
import com.summer.common.response.RealResponse;
import com.summer.common.response.RpcResponse;
import com.summer.common.response.StandardResponse;
import com.summer.common.support.CommonCode;
import com.summer.common.view.FastJsonMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Objects;

/**
 * 包装响应，默认为最新的结构体</a>
 * 当queryString或者header中包含RPC=true的键值对时，则使用cmp结构响应数据
 * 当queryString或者header中包含X-FRONT=true且RPC=true的键值对时，则使用err_no,err_msg,results结构响应数据
 *
 */
@ControllerAdvice("com.thinker")
public class ResponseBodyFilter implements ResponseBodyAdvice<Object> {

    private final static Logger log = LoggerFactory.getLogger(ResponseBodyFilter.class);

    private static final String RPC_KEY = "RPC";

    private static final String FRONT_KEY = "X-FRONT";

    /**
     * 只有数据类型转换为{@link FastJsonMessageConverter} 支持的类型才进行数据转换
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return converterType.getName().equalsIgnoreCase(FastJsonMessageConverter.class.getName());
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        //获取请求路径
        String requestUrl = request.getURI().getRawPath();
        try {
            // 排除spring-boot-actuator日志;非response结构(已进行异常处理);json 进行包装，非json使用默认处理
            if (!requestUrl.startsWith("/actuator")
                    && (selectedContentType.toString().startsWith(MediaType.APPLICATION_JSON.toString()))
                    && (!(body instanceof ResultSet || body instanceof RpcReply))) {
                if (Objects.nonNull(returnType.getMethodAnnotation(RealResponse.class))) {
                    return body;
                }
                // 标记标准注解的，header或queryString未特殊说明为rpc标记的，则使用标准结构体包装，否则使用rpc结构体包装
                if (Objects.nonNull(returnType.getMethodAnnotation(StandardResponse.class))) {
                    Boolean headerKey = getHeaderKey(request.getHeaders(), RPC_KEY);
                    if (Objects.nonNull(headerKey) && headerKey) {
                        return RpcReply.onOk(body);
                    }
                    Boolean queryKey = getQueryKey(request.getURI().getQuery(), RPC_KEY);
                    if (Objects.nonNull(queryKey) && !queryKey) {
                        return RpcReply.onOk(body);
                    }

                    return ResultSet.onOk(body);
                }
                // 标记Rpc注解的，按照rpc的格式返回数据，否则按照自定义结构返回
                if (isRpc(returnType, request.getHeaders(), request.getURI().getQuery())) {
                    // 标记Rpc且标记Front，则返回简化的rpc结构
                    if (isFront(request.getHeaders(), request.getURI().getQuery())) {
                        return new RpcReply.Response<>(CommonCode.SuccessOk.code(), CommonCode.SuccessOk.msg(), body);
                    }

                    return RpcReply.onOk(body);
                }

                return ResultSet.onOk(body);
            }
        } catch (Exception e) {
            log.error("数据接口转换发生错误", e);
        }

        return body;
    }

    private static Boolean getQueryKey(String query, String key) throws UnsupportedEncodingException {
        if (StringUtils.isEmpty(query)) {
            return null;
        }

        String[] pairs = query.split("&");

        for (String pair : pairs) {
            int    idx      = pair.indexOf("=");
            String queryKey = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
            String value    = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
            return key.equalsIgnoreCase(queryKey) && Boolean.parseBoolean(value);
        }

        return null;
    }

    private static boolean isQueryKey(String query, String key) throws UnsupportedEncodingException {
        Boolean queryKey = getQueryKey(query, key);

        return Objects.nonNull(queryKey) && queryKey;
    }

    private static Boolean getHeaderKey(HttpHeaders httpHeaders, String key) {
        if (httpHeaders.containsKey(key)) {
            List<String> rpcList = httpHeaders.get(key);
            if (!CollectionUtils.isEmpty(rpcList)) {
                for (String rpcString : rpcList) {
                    return Boolean.parseBoolean(rpcString);
                }
            }
        }

        return null;
    }

    private static boolean isHeaderKey(HttpHeaders httpHeaders, String key) {
        Boolean headerKey = getHeaderKey(httpHeaders, key);

        return Objects.nonNull(headerKey) && headerKey;
    }

    private static boolean isRpc(MethodParameter returnType, HttpHeaders httpHeaders, String query)
            throws UnsupportedEncodingException {
        // 标记注解，且未指定header或queryString的，以注解为准
        if (Objects.nonNull(returnType.getMethodAnnotation(RpcResponse.class))) {
            Boolean headerKey = getHeaderKey(httpHeaders, RPC_KEY);
            if (Objects.nonNull(headerKey) && !headerKey) {
                return false;
            }
            Boolean queryKey = getQueryKey(query, RPC_KEY);
            if (Objects.nonNull(queryKey) && !queryKey) {
                return false;
            }

            return true;
        }
        return isHeaderKey(httpHeaders, RPC_KEY) || isQueryKey(query, RPC_KEY);
    }

    private static boolean isFront(HttpHeaders httpHeaders, String query) throws UnsupportedEncodingException {
        return isHeaderKey(httpHeaders, FRONT_KEY) || isQueryKey(query, FRONT_KEY);
    }
}

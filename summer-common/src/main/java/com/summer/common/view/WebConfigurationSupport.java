package com.summer.common.view;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.summer.common.core.ResultSet;
import com.summer.common.core.RpcException;
import com.summer.common.core.RpcQuery;
import com.summer.common.core.RpcReply;
import com.summer.common.helper.BytesHelper;
import com.summer.common.helper.DateHelper;
import com.summer.common.helper.EncryptHelper;
import com.summer.common.helper.ExceptionHelper;
import com.summer.common.helper.JsonHelper;
import com.summer.common.helper.JvmOSHelper;
import com.summer.common.helper.SpringHelper;
import com.summer.common.helper.StringHelper;
import com.summer.common.ibatis.DataSourceManager;
import com.summer.common.initializ.SpringBootApplication;
import com.summer.common.support.CommonCode;
import com.summer.common.support.IConstant;
import com.summer.common.view.parser.ApiOperation;
import com.summer.common.view.parser.RequestContext;
import com.summer.common.view.parser.RequestHeader;
import com.summer.common.view.parser.RequestSession;
import javafx.util.Pair;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class WebConfigurationSupport extends WebMvcConfigurationSupport implements WebSocketConfigurer {
    private static IWebAuthenticationFilter AUTHENTICATION_FILTER;

    /**
     * Websocket允许的域
     **/
    protected String[] websocketAllowedOrigins(ApplicationContext context) {
        return new String[]{"*"};
    }

    /**
     * you can implements your own AuthenticationFilter
     **/
    @SuppressWarnings("WeakerAccess")
    protected IWebAuthenticationFilter injectAuthenticationFilter(@SuppressWarnings("unused") ApplicationContext context) {
        return new IWebAuthenticationFilter() {
        };
    }

    /**
     * you can implements your own WebSocketTransactor
     **/
    protected IWebSocketTransactor injectWebSocketTransactor(ApplicationContext context) {
        return new IWebSocketTransactor() {
        };
    }

    /**
     * 配置 HttpMessageConverters
     **/
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.addAll(Lists.newArrayList(FastJsonMessageConverter.INSTANCE,
                                             FormMessageConverter.INSTANCE,
                                             StringMessageConverter.INSTANCE
                                            ));
    }

    /**
     * 静态资源配置
     **/
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/file/**").addResourceLocations("file:" + JvmOSHelper.projectDir() + "/uploadPath/");
        registry.addResourceHandler("/**").addResourceLocations("classpath:/statics/");
    }

    /**
     * HTTP 参数注入, ALL MAP args can not resolver
     **/
    @Override
    protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> argResolvers) {
        argResolvers.add(new RequestClientIpResolver());
        argResolvers.add(new RequestQueriesResolver());
        argResolvers.add(new RequestSessionResolver());
    }

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        if (SpringHelper.mscResponseUsed()) {
            registry.addInterceptor(new RequestMappingInterceptor());
        }
    }

    /**
     * 异步 Controller 支持
     **/
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(DateHelper.MINUTE_TIME);
    }

    /**
     * 注册 WebSocket 入口
     **/
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        ApplicationContext context = getApplicationContext();
        String[] origins = websocketAllowedOrigins(context);
        WebSocketMessageHandler handler = new WebSocketMessageHandler(injectWebSocketTransactor(context));
        WSHandshakeInterceptor interceptor = new WSHandshakeInterceptor(getAuthenticationFilter(context));
        registry.addHandler(handler, "/tunnel").setAllowedOrigins(origins).addInterceptors(interceptor);
        registry.addHandler(handler, "/tunnel/sjs").setAllowedOrigins(origins).addInterceptors(interceptor).withSockJS();

        // WebSocket 心跳检测（客户端心跳频率小于等于 DateHelper.MINUTE_TIME/3， 比如为：DateHelper.MINUTE_TIME/4）
        final long fixedRate = DateHelper.MINUTE_TIME / 3;
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            for (WebSocketSession session : RequestContext.allWSS()) {
                // WebSocket 心跳超时，关闭连接并从 WSS_MAP 中移除
                if ((System.currentTimeMillis() - RequestContext.latestReceiveTimeWSS(session)) > DateHelper.MINUTE_TIME) {
                    RequestContext.offWSS(session);
                }
            }
        }, fixedRate, fixedRate, TimeUnit.MILLISECONDS);
    }

    private IWebAuthenticationFilter getAuthenticationFilter(ApplicationContext context) {
        if (null == AUTHENTICATION_FILTER) {
            AUTHENTICATION_FILTER = injectAuthenticationFilter(context);
        }
        return WebConfigurationSupport.AUTHENTICATION_FILTER;
    }

    private FilterRegistrationBean createFilterRegistrationBean(Filter filter, int order) {
        FilterRegistrationBean frBean = new FilterRegistrationBean();
        frBean.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));
        frBean.setFilter(filter);
        frBean.setOrder(order);
        return frBean;
    }

    @Bean
    /** 请求返回 Response > 2k 压缩处理 **/
    public FilterRegistrationBean reply2KCompressionFilter() {
        return createFilterRegistrationBean(new Reply2kCompressionFilter(), Integer.MIN_VALUE);
    }

    @Bean
    /** HTTP请求安全认证 **/
    public FilterRegistrationBean authenticationFilter() {
        return createFilterRegistrationBean(getAuthenticationFilter(super.getApplicationContext()), (Integer.MIN_VALUE + 1));
    }

    @Bean
    /** 接口日志, 开启配置： operations.log.enable=true **/
    public ApiOperationInterceptor apiOperationInterceptor() {
        return new ApiOperationInterceptor(super.getApplicationContext());
    }

    /**
     * 全局 Controller
     **/
    @Controller
    @ControllerAdvice
    public static class GlobalControllerHandler implements ErrorController {
        public static final String ERROR_PATH = "/error", FAVICON_ICON = "/favicon.ico";
        private static final String MIN_BASE64_IMAGE = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWMoKyv7DwAFKgJi3Cd5fQAAAABJRU5ErkJggg==";
        private static final Set<String> STATE_SET = Sets.newHashSet("UP", "DOWN");

        @Override
        public String getErrorPath() {
            return ERROR_PATH;
        }

        @ExceptionHandler(Throwable.class)
        void errorThrowable(HttpServletResponse response, Throwable cause) {
            ExceptionHelper.responseWrite(response, cause);
        }

        @RequestMapping(ERROR_PATH)
        void errorNotfound(HttpServletResponse response) throws IOException {
            RpcException exception = new RpcException(CommonCode.NotFound);
            ExceptionHelper.responseWrite(response, exception);
            RequestSession session = RequestContext.get().getSession();
            if (null != session && SpringHelper.mscResponseUsed() && !IConstant.isStaticsEndpoint(session.uri)) {
                OutputStream os = null;
                Pair<String, byte[]> result = new Pair<>(MediaType.APPLICATION_JSON_UTF8_VALUE,
                                                         JsonHelper.toJSONBytes(ResultSet.onFail(exception.code(), exception.msg())));
                try {
                    ExceptionHelper.clearResponseError(response);
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setHeader(RequestHeader.Rid.name(), session.rid);
                    response.setContentType(result.getKey());
                    os = response.getOutputStream();
                    os.write(result.getValue());
                } finally {
                    // 清理 ThreadLocal
                    BytesHelper.close(os);
                    RequestContext.get().clear();
                    DataSourceManager.get().clear();
                    RpcReply.Helper.get().clear();
                }
            }
        }

        @RequestMapping(FAVICON_ICON)
        void favicon(HttpServletResponse response) throws IOException {
            OutputStream os = response.getOutputStream();
            try {
                BytesHelper.copy(EncryptHelper.decode64Stream(MIN_BASE64_IMAGE), os);
            } finally {
                os.flush();
                BytesHelper.close(os);
            }
        }

        @GetMapping(path = "/actuator/jvm", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
        @ResponseBody
        Map<String, Map<String, Number>> jvmM() {
            return ImmutableMap.of("heap", JvmOSHelper.heapMemory(),
                                   "direct", JvmOSHelper.directMemory(),
                                   "nonHeap", JvmOSHelper.nonHeapMemory());
        }

        @GetMapping(path = "/actuator/request/stats", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
        @ResponseBody
        Map<String, Map<Long, Map<String, Number>>> requestStats() {
            return SpringBootApplication.requestStatsGet();
        }

        @GetMapping(path = "/actuator/inst/stats", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
        @ResponseBody
        Map<Long, Map<String, Number>> instStats() {
            return SpringBootApplication.instStatsGet();
        }

        @PostMapping(path = "/actuator/health/{state}")
        @ResponseBody
        String healthState(@PathVariable String state) {
            if (STATE_SET.contains(state)) {
                return SpringHelper.getBean(SelfHealthChecker.class).setState(state).state();
            }
            return String.format("未知的服务状态： %s", state);
        }

        @Component //自定义监控内容
        public static class SelfHealthChecker implements HealthIndicator {
            private static final String UP = "UP", DOWN = "DOWN";
            private String state = UP;

            SelfHealthChecker setState(String state) {
                this.state = state;
                return this;
            }

            String state() {
                return UP.equals(state) ? UP : DOWN;
            }

            @Override
            public Health health() {
                // 本地服务注册直接 DOWN 状态
                if (JvmOSHelper.isWindows()) {
                    setState(DOWN);
                    return new Health.Builder().down().build();
                }
                return UP.equals(state) ? new Health.Builder().up().build() : new Health.Builder().down().build();
            }
        }
    }

    /**
     * 只有使用PRC CMP 方式调用时才启作用
     **/
    @Controller
    @Deprecated
    public static class RpcController {
        private static String uri(String ctrl, String method) {
            if (StringHelper.isBlank(ctrl) && StringHelper.isBlank(method)) {
                throw new RpcException(CommonCode.Unavailable);
            }
            if (!StringHelper.isBlank(ctrl)) {
                if (!ctrl.startsWith("/")) {
                    ctrl = "/" + ctrl;
                }
                if (ctrl.endsWith("/")) {
                    ctrl = ctrl.substring(0, ctrl.length() - 1);
                }
            }
            if (!StringHelper.isBlank(method) && !method.startsWith("/")) {
                method = "/" + method;
            }
            return ctrl + method;
        }

        @RequestMapping(path = {"", "/", "/rest"}, method = RequestMethod.POST, name = "RPC CMP 请求转发入口")
        public void dispatcher(@RequestBody StringRpcQuery query, HttpServletRequest request, HttpServletResponse response) throws Exception {
            RpcReply.Helper.get().setQuery(query);
            String url = uri(query.ctrl(), query.method());

            final byte[] body = BytesHelper.utf8Bytes(query.args());
            request.getRequestDispatcher(url).forward(new HttpServletRequestWrapper(request) {
                @Override
                public int getContentLength() {
                    return body.length;
                }

                @Override
                public String getMethod() {
                    return 0 == body.length ? "GET" : "POST";
                }

                @Override
                public ServletInputStream getInputStream() throws IOException {
                    return 0 == body.length ? request.getInputStream() : BytesHelper.castServletInputStream(new ByteArrayInputStream(body));
                }
            }, response);
            final RequestSession session = RequestContext.get().getSession();
            if (null != session && ExceptionHelper.isCMPRequest(session) && null != session.icm && !session.apiIntercept) {
                ApiOperationInterceptor.operationLog(null, null, null, true);
            }
        }

        @ApiOperation(name = "服务状态监控")
        @RequestMapping(path = "/{wn}/health_check", method = RequestMethod.GET)
        @ResponseBody
        Map<String, Object> healthCheck(@PathVariable("wn") String wn) {
            if (SpringHelper.applicationName().equalsIgnoreCase(wn)) {
                return ImmutableMap.of("name", wn, "status", 200);
            } else {
                return ImmutableMap.of("status", 404, "message", String.format("没有找到[%s]服务", wn));
            }
        }

        public static class StringRpcQuery extends RpcQuery<String> {
            private static final long serialVersionUID = 3645632047183378870L;

            @Override
            public void verify() {
                String dispatcher = uri(ctrl(), method());
                if (StringHelper.isBlank(dispatcher) || dispatcher.equals("/")) {
                    throw new RpcException(CommonCode.Unavailable);
                }
            }
        }
    }

}

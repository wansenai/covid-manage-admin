package com.summer.common.view;

import com.google.common.util.concurrent.SettableFuture;
import com.summer.common.helper.NetworkHelper;
import com.summer.common.helper.StringHelper;
import com.summer.common.view.parser.RequestContext;
import com.summer.common.view.parser.RequestSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WSHandshakeInterceptor implements HandshakeInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(WSHandshakeInterceptor.class);

    private final IWebAuthenticationFilter authenticationFilter;

    public WSHandshakeInterceptor(IWebAuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest req, ServerHttpResponse response, WebSocketHandler handler, Map<String, Object> attributes) {
        // 初始化用于日志打印追踪
        RequestContext.get().setSession(RequestSession.newbornWithWebsocket());

        if (req instanceof ServletServerHttpRequest) {
            //解决The extension [x-webkit-deflate-frame] is not supported 的问题
            List<String> extS = req.getHeaders().get("Sec-WebSocket-Extensions");
            if (null != extS && extS.get(0).contains("permessage-deflate")) {
                req.getHeaders().set("Sec-WebSocket-Extensions", "permessage-deflate");
            }
            HttpServletRequest request = ((ServletServerHttpRequest) req).getServletRequest();
            try {
                String token = request.getParameter(RequestContext.$TOKEN);
                String sid = request.getParameter(RequestContext.$SID);
                String did = request.getParameter(RequestContext.$DID);
                // 校验参数
                if (StringHelper.isBlank(token) || StringHelper.isBlank(sid) || StringHelper.isBlank(did)) {
                    LOG.warn("Websocket missing args {} or {} or {}", RequestContext.$TOKEN, RequestContext.$SID, RequestContext.$DID);
                    return false;
                }
                // 安全IP
                String clientIp = NetworkHelper.ofClientIp(request);
                if (authenticationFilter.clientDeny(clientIp, did)) {
                    LOG.warn("The websocket client deny, clientIp: {} did: {}", clientIp, did);
                    return false;
                }
                SettableFuture<String> uidFuture = SettableFuture.create();
                authenticationFilter.doFilterWS(request.getServerName(), token, did, sid, uidFuture);
                attributes.put(RequestContext.$UID, uidFuture.get(5, TimeUnit.SECONDS));
                attributes.put(RequestContext.$WID, request.getQueryString());
                attributes.put(RequestContext.$DID, did);
                attributes.put(RequestContext.$SID, sid);
                return true;
            } catch (Exception e) {
                if (e instanceof InterruptedException || e instanceof TimeoutException || e instanceof ExecutionException) {
                    LOG.warn("Websocket connect error while not get uid from uidFuture...");
                } else {
                    LOG.warn("Websocket BeforeHandshake error ", e);
                }
                return false;
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler handler, Exception ex) {
        if (null != ex) {
            LOG.error("Websocket AfterHandshake error ", ex);
        }
    }
}

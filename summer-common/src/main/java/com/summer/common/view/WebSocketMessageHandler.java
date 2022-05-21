package com.summer.common.view;

import com.summer.common.view.parser.RequestContext;
import com.summer.common.view.parser.RequestSession;
import com.summer.common.helper.BytesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.nio.ByteBuffer;

public class WebSocketMessageHandler implements WebSocketHandler {
    private static final Logger LOG = LoggerFactory.getLogger(WebSocketMessageHandler.class);
    // 心跳消息
    private static final TextMessage HBM = new TextMessage(BytesHelper.utf8Bytes("@HBM"));

    private static volatile IWebSocketTransactor TRANSACTOR;
    public WebSocketMessageHandler(IWebSocketTransactor transactor) {
        TRANSACTOR = transactor;
    }
    public static IWebSocketTransactor transactor() {
        return WebSocketMessageHandler.TRANSACTOR;
    }
    /** 连接建立后处理 **/
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        RequestContext.refreshReceiveTimeWSS(session);
        LOG.info("Connect to the Websocket success session={}", RequestContext.wssId(session));
        RequestContext.acceptWSS(session);
    }

    /** 接收客户端消息处理 **/
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // 初始化用于日志打印追踪
        RequestContext.get().setSession(RequestSession.newbornWithWebsocket());

        RequestContext.refreshReceiveTimeWSS(session);
        if (message instanceof TextMessage) {
            TextMessage tm = (TextMessage) message;
            if(HBM.getPayload().equals(tm.getPayload())) {
                session.sendMessage(HBM);
            } else {
                TRANSACTOR.handleString(session, tm.getPayload());
            }
        } else {
            TRANSACTOR.handleByteBuffer(session, (ByteBuffer)message.getPayload());
        }
    }

    /** 抛出异常时处理 **/
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        LOG.warn("Websocket session={} closed={}, error ", RequestContext.wssId(session), !session.isOpen(), exception);
        RequestContext.offWSS(session);
    }

    /** 连接关闭后处理 **/
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        LOG.info("WebSocket session={} closed status={}", RequestContext.wssId(session), status);
        RequestContext.offWSS(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}

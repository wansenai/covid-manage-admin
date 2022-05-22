package com.summer.common.view;

import com.google.common.collect.Lists;
import com.summer.common.helper.JsonHelper;
import com.summer.common.helper.ThreadFactoryHelper;
import com.summer.common.view.parser.RequestContext;
import com.summer.common.view.parser.WebSockResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

public interface IWebSocketTransactor {
    Logger LOG = LoggerFactory.getLogger(IWebSocketTransactor.class);

    ExecutorService WS_ES = ThreadFactoryHelper.newCachedThreadPool("WebSocketTransactor");

    /**
     * 发送消息给指定的 WebSocketSession
     **/
    static void sendText2Session(WebSocketSession session, WebSockResponse response) {
        sendTextTo(Lists.newArrayList(session), response);
    }

    /**
     * 发送消息给指定的 uid 的 WebSocketSession
     **/
    static void sendText2Uid(final String uid, final WebSockResponse response) {
        sendTextTo(RequestContext.wssByUid(uid), response);
    }

    /**
     * 发送消息给指定的 uid, sid 的 WebSocketSession
     **/
    static void sendText2Uid(final Collection<String> uidC, final String sid, final WebSockResponse response) {
        for (String uid : uidC) {
            sendText2Uid(uid, sid, response);
        }
    }

    /**
     * 发送消息给指定的 uid, sid 的 WebSocketSession
     **/
    static void sendText2Uid(final String uid, final String sid, final WebSockResponse response) {
        sendTextTo(RequestContext.wssById(uid, sid), response);
    }

    /**
     * 发送消息给所有的 WebSocketSession
     **/
    static void sendText2All(final WebSockResponse response) {
        sendTextTo(RequestContext.allWSS(), response);
    }

    /**
     * 发送消息给指定列表的 WebSocketSession
     **/
    static void sendTextTo(Collection<WebSocketSession> wssC, WebSockResponse response) {
        WS_ES.submit(() -> {
            try {
                for (WebSocketSession session : wssC) {
                    if (session.isOpen()) synchronized (session) {
                        try {
                            session.sendMessage(new TextMessage(JsonHelper.toJSONBytes(response)));
                        } catch (Exception e) {
                            LOG.error("WebSocket session: {} send TextMessage error ", RequestContext.wssId(session), e);
                        }
                    }
                    else {
                        RequestContext.offWSS(session);
                    }
                }
            } catch (Exception ex) {
                LOG.error("WebSocket send TextMessage unexpected error ", ex);
            }
        });
    }

    /**
     * 接收客户端文本数据
     **/
    default void handleString(WebSocketSession session, String message) {
        LOG.info("Websocket ID={}, receive TEXT={}", RequestContext.wssId(session), message);
    }

    /**
     * 接收客户端 ByteBuffer 数据
     **/
    default void handleByteBuffer(WebSocketSession session, ByteBuffer message) {
        LOG.info("Websocket ID={}, receive ByteBuffer={}", RequestContext.wssId(session), message.array().length);
    }

    /**
     * 进入
     **/
    default void into(WebSocketSession session) {

    }

    /**
     * 离开
     **/
    default void leave(WebSocketSession session) {

    }
}

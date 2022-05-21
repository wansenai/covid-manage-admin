package com.summer.common.view.parser;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.summer.common.helper.BytesHelper;
import com.summer.common.helper.StringHelper;
import com.summer.common.view.WebSocketMessageHandler;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 请求认证的 ThreadLocal
 */
public final class RequestContext {
    public static final String $WID = "@WID", $TOKEN = "@token", $UID = "@uid", $SID = "@sid", $DID = "@did", WSS_LRT = "L_R_T";
    private static final Map<String, ConcurrentMap<String, WebSocketSession>> WSS_MAP = Maps.newHashMap();

    /**
     * 保存在当前线程中，使用{@link InheritableThreadLocal}方便异步处理时的上下文传递
     * 修改初始化方式，InheritableThreadLocal不支持{@link ThreadLocal#withInitial(Supplier)}方式初始化
     */
    private static InheritableThreadLocal<RequestContext> holder = new InheritableThreadLocal<RequestContext>() {
        @Override
        protected RequestContext initialValue() {
            return new RequestContext();
        }
    };



    private RequestSession session;

    /**
     * 获取 RequestContext
     */
    public static RequestContext get() {
        return holder.get();
    }

    /**
     * 获取HTTP认证 SESSION
     */
    public RequestSession getSession() {
        return session;
    }

    /**
     * 设置HTTP的认证 RequestSession
     */
    public void setSession(RequestSession session) {
        this.session = session;
    }

    /**
     * 刷新WebSocket接收消息时间
     */
    public static void refreshReceiveTimeWSS(WebSocketSession wss) {
        if (null != wss) {
            wss.getAttributes().put(WSS_LRT, System.currentTimeMillis());
        }
    }

    /**
     * 获取最近一次接收消息时间
     */
    public static long latestReceiveTimeWSS(WebSocketSession wss) {
        if (null != wss) {
            Object lrt = wss.getAttributes().get(WSS_LRT);
            return null == lrt ? 0L : (long) lrt;
        }
        return 0L;
    }

    /**
     * 接收新的Websocket连接
     */
    public static void acceptWSS(WebSocketSession wss) {
        if (null != wss && wss.isOpen()) {
            String uid = (String) wss.getAttributes().get($UID);
            synchronized (WSS_MAP) {
                WSS_MAP.putIfAbsent(uid, Maps.newConcurrentMap());
                WSS_MAP.get(uid).put((String) wss.getAttributes().get($DID), wss);
                WebSocketMessageHandler.transactor().into(wss);
            }
        }
    }

    /**
     * 移除指定的 WebSocketSession
     */
    public static void offWSS(WebSocketSession wss) {
        if (null != wss) {
            if (wss.isOpen()) {
                BytesHelper.close(wss);
            }
            Object obj = wss.getAttributes().get($UID);
            if (null == obj || StringHelper.isBlank((String) obj)) {
                return;
            }
            synchronized (WSS_MAP) {
                ConcurrentMap<String, WebSocketSession> didWSS = WSS_MAP.get(obj);
                if (null != didWSS) {
                    obj = wss.getAttributes().get($DID);
                    if (null != obj && !StringHelper.isBlank((String) obj)) {
                        WebSocketSession session = didWSS.remove(obj);
                        if (null != session) {
                            if (session.isOpen()) {
                                BytesHelper.close(session);
                            }
                            WebSocketMessageHandler.transactor().leave(wss);
                        }
                    }
                }
            }
        }
    }

    /**
     * 移除指定 uid 的所有 WebSocketSession
     */
    public static void offUidWSS(String uid) {
        if (StringHelper.isBlank(uid)) {
            return;
        }
        synchronized (WSS_MAP) {
            ConcurrentMap<String, WebSocketSession> didWSS = WSS_MAP.get(uid);
            if (null != didWSS) {
                Collection<WebSocketSession> wssC = Collections.unmodifiableCollection(didWSS.values());
                for (WebSocketSession wss : wssC) {
                    if (wss.isOpen()) {
                        BytesHelper.close(wss);
                    }
                }
            }
            WSS_MAP.remove(uid);
        }
    }

    /**
     * 获取指定 WebSocketSession 的 sid
     */
    public static String wsSid(WebSocketSession wss) {
        if (null == wss) {
            return StringHelper.EMPTY;
        }
        return StringHelper.defaultString((String) wss.getAttributes().get($SID));
    }

    /**
     * 获取指定 WebSocketSession 的 uid
     */
    public static String wsUid(WebSocketSession wss) {
        if (null == wss) {
            return StringHelper.EMPTY;
        }
        return StringHelper.defaultString((String) wss.getAttributes().get($UID));
    }

    /**
     * 获取指定 WebSocketSession 的 did
     */
    public static String wsDid(WebSocketSession wss) {
        if (null == wss) {
            return StringHelper.EMPTY;
        }
        return StringHelper.defaultString((String) wss.getAttributes().get($DID));
    }

    /**
     * 获取指定 WebSocketSession 的 ID
     */
    public static String wssId(WebSocketSession wss) {
        if (null == wss) {
            return StringHelper.EMPTY;
        }
        return wss.getAttributes().get($WID).toString();
    }

    /**
     * 获取指定 uid 的所有 WebSocketSession
     */
    public static Collection<WebSocketSession> wssByUid(String uid) {
        ConcurrentMap<String, WebSocketSession> didWSS = WSS_MAP.get(StringHelper.defaultString(uid));
        if (null != didWSS) {
            return Collections.unmodifiableCollection(didWSS.values());
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * 获取指定 uid, sid 的所有 WebSocketSession
     */
    public static Collection<WebSocketSession> wssById(String uid, String sid) {
        ConcurrentMap<String, WebSocketSession> didWSS = WSS_MAP.get(StringHelper.defaultString(uid));
        if (null != didWSS) {
            return didWSS.values().stream().filter(ws -> ws.getAttributes().get($SID).equals(sid)).collect(Collectors.toList());
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * 获取指定 uid, sid 的所有 WebSocketSession
     */
    public static Collection<WebSocketSession> wssBySid(String sid) {
        Collection<WebSocketSession> wssC = Lists.newArrayList();
        if (!WSS_MAP.isEmpty()) {
            for (ConcurrentMap<String, WebSocketSession> ccm : WSS_MAP.values()) {
                wssC.addAll(ccm.values().stream().filter(ws -> ws.getAttributes().get($SID).equals(sid)).collect(Collectors.toList()));
            }
        }
        return wssC;
    }

    /**
     * 获取当前服务中的所有连接 WebSocketSession
     */
    public static Collection<WebSocketSession> allWSS() {
        Collection<WebSocketSession> wssC = Lists.newArrayList();
        if (!WSS_MAP.isEmpty()) {
            for (ConcurrentMap<String, WebSocketSession> ccm : WSS_MAP.values()) {
                wssC.addAll(ccm.values());
            }
        }
        return wssC;
    }

    /**
     * 每个请求完成清除 ThreadLocal 中的数据
     */
    public void clear() {
        holder.remove();
    }

    public static boolean keyAnswerUnderline() {
        RequestSession session = get().getSession();
        return null != session && 1 == session.keyStyle;
    }

    public static boolean keyFrontRequest() {
        RequestSession session = get().getSession();
        return null != session && 1 == session.frontREQ;
    }
}

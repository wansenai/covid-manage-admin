package com.summer.common.core;

import com.alibaba.fastjson.JSONObject;
import com.summer.common.support.CommonCode;
import com.summer.common.view.parser.RequestContext;
import com.summer.common.view.parser.RequestSession;
import com.summer.common.helper.GenericHelper;
import com.summer.common.helper.JsonHelper;
import com.summer.common.helper.MathHelper;
import com.summer.common.helper.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * RPC 请求体
 **/
public abstract class RpcQuery<T> implements IRequest, IGeneric<T>, Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(RpcQuery.class);

    private Header header;

    private Request request;

    public JsonStyle style() {
        return (null == header || null == header.style) ? JsonStyle.CamelCase : header.style;
    }

    public String appId() {
        return null == header ? StringHelper.EMPTY : StringHelper.defaultString(header.appId);
    }

    public String ip() {
        return null == header ? StringHelper.EMPTY : StringHelper.defaultString(header.ip);
    }

    public String ctrl() {
        return null == request ? StringHelper.EMPTY : StringHelper.defaultString(request.c);
    }

    public String method() {
        return null == request ? StringHelper.EMPTY : StringHelper.defaultString(request.m);
    }

    public T args() {
        Class<?> requestClazz = clazz();
        if (String.class.equals(requestClazz)) {
            return (T) request.p;
        }
        if (List.class.isAssignableFrom(requestClazz)) {
            if (null == request) {
                return (T) new ArrayList();
            }
            return (T) JsonHelper.parseArray(request.p, GenericHelper.type(requestClazz));
        }
        return null == request ? null : (T) JsonHelper.parseObject(request.p, requestClazz);
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Header getHeader() {
        return header;
    }

    public Request getRequest() {
        return request;
    }

    public static String Reply(RemoteReply<?> reply) {
        if (reply.success()) {
            JSONObject json = reply.body();
            // 规定的标准 CMP 返回格式
            if (json.containsKey("response")) {
                JSONObject response = json.getJSONObject("response");
                if (null != response) {
                    if (0 == response.getInteger("err_no")) {
                        return response.getString("results");
                    } else {
                        throw new RpcException(ICodeMSG.create(response.getInteger("err_no"), response.getString("err_msg")));
                    }
                } else {
                    LOG.warn("rpc reply json: {}", json);
                    throw new RpcException(CommonCode.SvError);
                }
            }
            // 简单 CMP 返回格式
            else {
                if (200 == MathHelper.nvl(json.getInteger("status"))) {
                    return json.getString("result");
                } else {
                    throw new RpcException(ICodeMSG.create(json.getInteger("status"), json.getString("message")));
                }
            }
        } else {
            throw new RpcException(ICodeMSG.create(reply.code, reply.body));
        }
    }

    public static class Header {
        private JsonStyle style;
        private String appId;
        private String ip;

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public JsonStyle getStyle() {
            return style;
        }

        public void setStyle(JsonStyle style) {
            this.style = style;
        }

        public String getAppId() {
            return appId;
        }

        public String getIp() {
            return ip;
        }
    }

    public static class Request {
        private String c;
        private String m;
        private String p;

        public void setC(String c) {
            this.c = c;
        }

        public void setM(String m) {
            this.m = m;
        }

        public void setP(String p) {
            this.p = p;
        }

        public String getC() {
            return c;
        }

        public String getM() {
            return m;
        }

        public String getP() {
            return p;
        }
    }

    public String currentUID() {
        RequestSession session = RequestContext.get().getSession();
        return null != session ? session.uid : StringHelper.EMPTY;
    }

    public String currentTNO() {
        RequestSession session = RequestContext.get().getSession();
        return null != session ? session.tid : StringHelper.EMPTY;
    }

    public RequestSession session() {
        return RequestContext.get().getSession();
    }
}

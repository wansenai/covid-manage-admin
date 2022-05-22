package com.summer.common.core;

import com.summer.common.helper.BytesHelper;
import com.summer.common.helper.JsonHelper;
import com.summer.common.helper.StringHelper;

import java.io.Serializable;
import java.util.StringJoiner;

public class RemoteReply<R> implements Serializable {
    private static final long serialVersionUID = -3805001835800051432L;

    protected final int code;
    protected final String body;
    protected final Class<R> rt;

    protected RemoteReply(Class<R> rt, int code, String body) {
        this.rt = rt;
        this.code = code;
        this.body = body;
    }

    public static <R> RemoteReply of(Class<R> rt, int code, String body) {
        return new RemoteReply(rt, code, body);
    }

    public boolean success() {
        // HTTP code
        return code >= 200 && code < 300;
    }

    public <T> T body() {
        if (success()) {
            // 直接返回字符串
            if (String.class.equals(rt)) {
                return !StringHelper.isBlank(body) ? (T) body : (T) StringHelper.EMPTY;
            }
            // 返回 bytes
            else if (byte[].class.equals(rt) || Byte[].class.equals(rt)) {
                return !StringHelper.isBlank(body) ? (T) BytesHelper.utf8Bytes(body) : (T) new byte[0];
            }
            // 返回 json
            else {
                return body.startsWith("[") && body.endsWith("]") ? (T) JsonHelper.parseArray(body, rt) : (T) JsonHelper.parseObject(body, rt);
            }
        } else {
            throw new RpcException(ICodeMSG.create(code, body));
        }
    }

    public ICodeMSG icm() {
        return ICodeMSG.create(code, body);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RemoteReply.class.getSimpleName() + "[", "]")
                .add("rt=" + rt)
                .add("code=" + code)
                .add("body='" + body + "'")
                .toString();
    }
}

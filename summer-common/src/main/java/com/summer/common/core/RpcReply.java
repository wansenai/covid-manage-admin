package com.summer.common.core;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.ImmutableMap;
import com.summer.common.support.CommonCode;
import com.summer.common.helper.JsonHelper;
import com.summer.common.view.WebConfigurationSupport;

import java.util.Map;

/** RPC 返回体 **/
public final class RpcReply<T> implements IGeneric<T> {
    public Response<T> response;

    public RpcReply() {
    }

    public RpcReply(Response<T> response) {
        this.response = response;
    }

    public static <R> RpcReply<R> say(int code, String msg, final R results){
        return new RpcReply<>(new Response<>(code, msg, results));
    }

    public static RpcReply<Void> onOk() {
        return onOk(null);
    }

    public static <R> RpcReply<R> onOk(final R results) {
        return new RpcReply<>(new Response<>(CommonCode.SuccessOk.code(), CommonCode.SuccessOk.msg(), results));
    }

    public static <R> RpcReply<R> onFail(final ICodeMSG cm) {
        return onFail(null == cm ? CommonCode.SvError : cm, null);
    }
    public static <R> RpcReply<R> onFail(final ICodeMSG cm, final R results) {
        ICodeMSG icm = null == cm ? CommonCode.SvError : cm;
        return new RpcReply<>(new Response<>(icm.code(), icm.msg(), results));
    }

    public static class Response<T> {
        @JSONField(name = "err_no")
        public int errorNo;
        @JSONField(name = "err_msg")
        public String errorMSG;
        public Object results;

        public Response() {
        }

        public Response(int errorNo, String errorMSG, T results) {
            this.errorNo = errorNo;
            this.errorMSG = errorMSG;
            this.results = objectAs(results);
        }

        private Object objectAs(T results) {
            if(null == results) {
                return null;
            }
            if(JsonStyle.Underline == Helper.get().getJsonStyle()) {
                Map<String, T> kvT = ImmutableMap.of("$jst$", results);
                return JsonHelper.parseObject(JsonHelper.toUnderlineJson(kvT)).get("$jst$");
            }
            return results;
        }
    }

    public static final class Helper {
        private static ThreadLocal<Helper> holder = ThreadLocal.withInitial(Helper::new);

        private WebConfigurationSupport.RpcController.StringRpcQuery query;

        public JsonStyle getJsonStyle() {
            return (null != query && null != query.getHeader()) ? query.getHeader().getStyle() : JsonStyle.CamelCase;
        }
        public String getBody() {
            return JsonHelper.toJSONString(query);
        }

        public void setQuery(WebConfigurationSupport.RpcController.StringRpcQuery query) {
            this.query = query;
        }

        private Helper() {}
        public static Helper get(){
            return holder.get();
        }

        public void clear() {
            holder.remove();
        }
    }
}

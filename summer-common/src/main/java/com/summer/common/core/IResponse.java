package com.summer.common.core;

public class IResponse<T> {
    public int code;
    public String msg;
    public T data;

    public IResponse() {
    }

    private IResponse(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static IResponse<Void> onOk() {
        return new IResponse<>(0, "成功", null);
    }

    public static <R> IResponse<R> onOk(R res) {
        return new IResponse<>(0, "成功", res);
    }

    public static <R> IResponse<R> onFail(int code, String msg) {
        return new IResponse<>(code, msg, null);
    }
}

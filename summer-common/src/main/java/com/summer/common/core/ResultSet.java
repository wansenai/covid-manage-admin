package com.summer.common.core;

import com.alibaba.fastjson.annotation.JSONField;
import javafx.util.Pair;

public class ResultSet<T> {
    @JSONField(ordinal = 0)
    public int code;
    @JSONField(ordinal = 1)
    public String msg;
    @JSONField(ordinal = 2)
    public T data;

    public ResultSet() {
    }

    private ResultSet(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static ResultSet<Void> onOk() {
        return new ResultSet<>(0, "成功", null);
    }

    public static <R> ResultSet<R> onOk(R res) {
        return new ResultSet<>(0, "成功", res);
    }

    public static <R> ResultSet<R> onFail(int code, String msg) {
        return new ResultSet<>(code, msg, null);
    }

    public static <R> ResultSet<R> onFail(Pair<Integer, String> cm) {
        return new ResultSet<>(cm.getKey(), cm.getValue(), null);
    }
}

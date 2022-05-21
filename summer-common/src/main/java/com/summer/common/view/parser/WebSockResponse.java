package com.summer.common.view.parser;

import com.summer.common.core.ICodeMSG;
import com.summer.common.support.CommonCode;

import java.io.Serializable;

public class WebSockResponse implements Serializable {
    public int code;

    public String msg;

    public String mdt;

    public Object data;

    public static WebSockResponse mtOK(String mdt, Object data) {
        WebSockResponse response = new WebSockResponse();
        response.code = CommonCode.SuccessOk.code();
        response.msg = CommonCode.SuccessOk.msg();
        response.mdt = mdt;
        response.data = data;
        return response;
    }

    public static WebSockResponse voidOK() {
        WebSockResponse response = new WebSockResponse();
        response.code = CommonCode.SuccessOk.code();
        response.msg = CommonCode.SuccessOk.msg();
        response.mdt = "OK";
        return response;
    }

    public static WebSockResponse fail(ICodeMSG icm) {
        WebSockResponse response = new WebSockResponse();
        response.code = icm.code();
        response.msg = icm.msg();
        response.mdt = "ERROR";
        return response;
    }
}

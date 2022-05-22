package com.summer.common.support;

import com.summer.common.core.ICodeMSG;

/**
 * 公共错误码
 */
public enum CommonCode implements ICodeMSG {
    SuccessOk(0, "成功"),

    Failure(-1, "失败"),

    Unavailable(400, "请求数据无效，服务器无法解析"),

    NoAuthority(401, "未授权访问或授权码过期，请登录授权后再继续操作"),

    Forbidden(403, "您没有权限访问该服务"),

    NotFound(404, "资源未找到"),

    Unsafety(406, "请求数据验证失败（您的网络环境可能不安全）"),

    InvalidToekn(407, "token已过期"),

    IllegalToekn(408, "非法的token"),

    Timeout(408, "请求服务处理超时"),

    InvalidTime(412, "客户端时间异常，请调整正确时间后再试"),

    RequestMaxExceeded(413, "请求数据过大，服务器无法处理"),

    SvError(500, "服务异常，请联系系统管理员");


    private final int code;
    private final String desc;

    CommonCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public String msg() {
        return desc;
    }
}

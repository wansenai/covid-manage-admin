package com.summer.common.view.parser;

import com.summer.common.core.StringEnum;

public enum RequestHeader implements StringEnum {
    RID("X-Request-ID", "每次请求唯一编号"),

    Rid("X-Request-Id", "每次请求唯一编号"),

    SpanID("X-Span-Id", "统一访问ID的多个跨度"),

    Vno("X-Vno", "APP版本， 如： V_1.0, V_2.0, V_3.0"),

    DeviceID("X-Device-NO", "设备编号，每个设备的唯一标识"),

    ClientTime("X-Client-Time", "请求时的客户端时间戳毫秒数"),

    DataSecret("X-Data-Secret", "请求数据的签值"),

    Signature("X-Signature", "权限签名数据"),

    AKeyStyle("X-Answer-Key-Style", "返回KEY样式，1-下划线，默认驼峰"),

    FrontREQ("X-Front-REQ", "是否前端请求，0-否、1-是");

    RequestHeader(String value, String desc) {
        changeNameTo(this, value);
    }
}

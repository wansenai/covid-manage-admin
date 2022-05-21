package com.summer.common.core;

/** Json样式 **/
public enum JsonStyle implements StringEnum {
    /** 驼峰 **/
    CamelCase("0"),

    /** 下划线 **/
    Underline("1");

    JsonStyle(String style) {
        changeNameTo(this, style);
    }
}

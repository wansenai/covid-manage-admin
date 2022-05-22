package com.summer.common.esearch.orm;


import com.summer.common.core.StringEnum;

public enum QueryEQL implements StringEnum {
    /**
     * 模糊匹配
     **/
    Matched("like"),

    /**
     * 大于
     **/
    Greater(">"),

    /**
     * 小于
     **/
    Little("<"),

    /**
     * 大于等于
     **/
    GreaterE(">="),

    /**
     * 小于等于
     **/
    LittleE("<="),

    /**
     * 不包含
     **/
    Excluded("><"),

    /**
     * 包含等于
     **/
    Included("<>"),

    /**
     * 字段为空
     **/
    IsNull("isNull");

    QueryEQL(String eql) {
        changeNameTo(this, eql);
    }
}

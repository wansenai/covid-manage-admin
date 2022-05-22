package com.summer.common.support;

import com.summer.common.core.StringEnum;

/**
 * 时间格式化
 **/
public enum DateFormat implements StringEnum {
    ShortNumDate("yyMMdd"),

    NumDate("yyyyMMdd"),

    StrikeDate("yyyy-MM-dd"),

    SplitterYMD("yyyy/MM/dd"),

    NumDateTime("yyyyMMddHHmmss"),

    NumDateMinute("yyyyMMddHHmm"),

    TwoYearNumDateTime("yyMMddHHmmss"),

    StrikeDateTime("yyyy-MM-dd HH:mm:ss"),

    DoubleDateTime("yyyyMMddHHmmss.SSS"),

    MillisecondTime("yyyy-MM-dd HH:mm:ss SSS"),

    TimeStamp("yyyy-MM-dd HH:mm:ss.SSS"),

    RFC3339("yyyy-MM-dd'T'HH:mm:ss"),

    NumTime("HHmmss"),

    ColonTime("HH:mm:ss"),
    noMillisecondTime("HH:mm"),

    ChineseYearMonthTime("yyyy年MM月"),
    YearMonth("yyyy-MM"),
    ChineseMonthDayTime("MM月dd日"),
    PointYearMonthTime("yyyy.MM");

    DateFormat(String value) {
        changeNameTo(this, value);
    }
}

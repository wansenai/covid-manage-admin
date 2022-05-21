package com.summer.common.esearch.orm;

import com.summer.common.core.StringEnum;

public enum Typical implements StringEnum {
    Keyword("keyword"),

    Text("text"),

    Integer("integer"),

    Long("long"),

    Float("float"),

    Double("double"),

    Date("date"),

    Object("object");

    private final String type;
    Typical(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}

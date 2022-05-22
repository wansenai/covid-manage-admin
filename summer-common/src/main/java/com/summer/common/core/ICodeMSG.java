package com.summer.common.core;

import com.summer.common.helper.StringHelper;

public interface ICodeMSG {
    static ICodeMSG create(int code, String msg) {
        return new ICodeMSG() {
            @Override
            public int code() {
                return code;
            }

            @Override
            public String msg() {
                return StringHelper.defaultString(msg);
            }
        };
    }

    int code();

    String msg();

    default String message() {
        return code() + " -> " + msg();
    }
}

package com.summer.common.core;

import com.summer.common.helper.GenericHelper;

import java.io.Serializable;

interface IGeneric<T> extends Serializable {
    default Class<?> clazz() {
        return GenericHelper.type(this.getClass());
    }
}

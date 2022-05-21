package com.summer.common.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** REST基类 **/
public abstract class BaseRest {
    protected final Logger LOG;

    protected BaseRest() {
        LOG = LoggerFactory.getLogger(this.getClass());
    }
}

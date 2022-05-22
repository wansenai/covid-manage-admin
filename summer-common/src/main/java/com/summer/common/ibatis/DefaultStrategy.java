package com.summer.common.ibatis;


import com.summer.common.helper.StringHelper;

import java.util.LinkedHashSet;

public class DefaultStrategy implements DynamicStrategy {
    private String datasource;
    private String tableSuffix;

    private DefaultStrategy(String datasource, String tableSuffix) {
        this.datasource = datasource;
        this.tableSuffix = tableSuffix;
    }

    public static DefaultStrategy stg() {
        return new DefaultStrategy(IDynamicDS.DEFAULT, StringHelper.EMPTY);
    }

    @Override
    public LinkedHashSet<Target> strategy() {
        return DynamicStrategy.ofTargetSet(new Target(datasource, DynamicStrategy.ofTableSuffixSet(tableSuffix)));
    }

    public DefaultStrategy ofDatasource(String datasource) {
        this.datasource = datasource;
        return this;
    }

    public DefaultStrategy ofTableSuffix(String tableSuffix) {
        this.datasource = tableSuffix;
        return this;
    }
}

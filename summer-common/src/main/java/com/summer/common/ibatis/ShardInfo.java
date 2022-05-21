package com.summer.common.ibatis;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class ShardInfo {

    public final int    dsIndex;
    public final int    tabIndex;

    public ShardInfo(int dsIndex, int tabIndex) {
        this.dsIndex = dsIndex;
        this.tabIndex = tabIndex;
    }

    private String dsKey() {
        return "datasource";
    }

    @Override
    public String toString() {
        return new StringBuilder("DataSource{dataSourceKey=").append(dsKey())
                                                             .append(", dataSourceIndex=").append(dsIndex)
                                                             .append(", tableIndex=").append(tabIndex)
                                                             .append("}").toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShardInfo)) return false;
        ShardInfo shardInfo = (ShardInfo) o;
        return new EqualsBuilder().append(dsIndex, shardInfo.dsIndex).append(tabIndex, shardInfo.tabIndex).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(dsIndex).append(tabIndex).toHashCode();
    }
}

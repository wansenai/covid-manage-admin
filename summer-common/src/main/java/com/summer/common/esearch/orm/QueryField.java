package com.summer.common.esearch.orm;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class QueryField implements Serializable {
    private static final long serialVersionUID = 5942144364493055972L;
    // 查询字段的值, primitive or collection
    public Object value;
    // 查询匹配方式
    @NotNull(message = "EQL操作符不能为空")
    public QueryEQL eql;

    @NotNull(message = "查询字段不能为空")
    public String propertyAt;
}

package com.summer.common.ibatis;

import com.alibaba.fastjson.JSONObject;
import com.summer.common.core.BaseEntity;
import com.summer.common.helper.BeanHelper;
import com.summer.common.helper.DateHelper;
import com.summer.common.helper.JsonHelper;
import com.summer.common.helper.SpringHelper;
import com.summer.common.helper.StringHelper;
import com.summer.common.support.DateFormat;
import com.summer.common.support.IConstant;
import com.summer.common.view.parser.RequestContext;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/** 拦截所有 bound sql 前的操作，设置必要参数 **/
@Intercepts({
        @Signature(type=Executor.class, method="update", args={MappedStatement.class, Object.class}),
        @Signature(type=Executor.class, method="queryCursor", args={MappedStatement.class, Object.class, RowBounds.class}),
        @Signature(type=Executor.class, method="query", args={MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class BeforeSQLInterceptor implements Interceptor {
    private static final String TENANT_NO = "TenantNO";
    private static final String TABLE_SUFFIX = "TableSuffix";

    @Override
    public Object intercept(Invocation invocation) throws Exception {
        if(invocation.getTarget() instanceof Executor) {
            return invokeBeforeBoundSql(invocation);
        }
        return null;
    }

    private Object invokeBeforeBoundSql(Invocation invocation) throws Exception {
        String showSQL = SpringHelper.confValue(IConstant.KEY_SHOW_IBATIS_SQL_P);
        Object[] args = invocation.getArgs();
        Object original = args[1];
        args[1] = processParameterAsMap(original);
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        if("true".equalsIgnoreCase(showSQL)) {
            String db = DataSourceManager.get().getDataSource();
            String sql = ms.getBoundSql(args[1]).getSql().replaceAll("\\s+", " ").trim();
            String rid = "-";
            if (Objects.nonNull(RequestContext.get().getSession()) && Objects.nonNull(RequestContext.get().getSession().rid)) {
                rid = RequestContext.get().getSession().rid;
            }
            String now = DateHelper.now(DateFormat.TimeStamp);
            System.out.println(String.format("[%s] %s @db %s @cmd %s @sql %s ", now, rid, db, ms.getSqlCommandType(), sql));
            System.out.println(String.format("[%s] %s @sql-param %s", now, rid, JsonHelper.toJSONString(args[1])));
        }
        Object object =  invocation.proceed();
        //新增数据时获取到主键ID
        if(SqlCommandType.INSERT == ms.getSqlCommandType() && original instanceof BaseEntity) {
            ((BaseEntity)original).ofDBId(((JSONObject)(invocation.getArgs()[1])).getLong("id"));
        }
        return object;
    }

    private Map<String, Object> processParameterAsMap(Object paramObj) {
        Map<String, Object> paramMap = null != paramObj ? null : new MapperMethod.ParamMap<>();
        if(null != paramObj) {
            // 单参数 将 参数转为 map
            if (BeanHelper.isPrimitiveType(paramObj.getClass())) {
                paramMap = new MapperMethod.ParamMap<>();
                // agr0 is mybatis original name
                paramMap.put("arg0", paramObj);
            } else {
                if (paramObj instanceof Map) {
                    //noinspection unchecked
                    paramMap = (Map<String, Object>) paramObj;
                }
                else {
                    paramMap = BeanHelper.bean2Map(paramObj);
                }
            }
        }
        paramMap.put(TABLE_SUFFIX, DataSourceManager.get().getTableSuffix());
        paramMap.put(TENANT_NO, StringHelper.defaultString(DataSourceManager.get().getTenantNo()));
        sterilizeParamMap(paramMap);
        return paramMap;
    }

    private void sterilizeParamMap(Map<String, Object> paramMap) {
        int paramSize = paramMap.size();
        Iterator<Map.Entry<String, Object>> iterator = paramMap.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String, Object> entry = iterator.next();
            String key = entry.getKey();
            try {
                if(key.startsWith("param") && (key.length() < 8) && StringHelper.isNumeric(key.substring(5))) {
                    int no = Integer.parseInt(key.substring(5));
                    if(no > 0 && no < paramSize) {
                        iterator.remove();
                    }
                }
            } catch (Exception e) {
                // the param not be remove
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
    @Override
    public void setProperties(Properties properties) {}
}

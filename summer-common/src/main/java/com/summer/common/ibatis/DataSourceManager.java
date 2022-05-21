package com.summer.common.ibatis;

import com.google.common.collect.Lists;
import com.summer.common.helper.StringHelper;
import com.summer.common.support.TableColumn;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public final class DataSourceManager {
    private static final Logger LOG = LoggerFactory.getLogger(DataSourceManager.class);

    private DataSourceManager() {
    }

    private String ds = IDynamicDS.DEFAULT;

    private boolean hasTransaction;

    private DynamicStrategy strategy;

    private String tableSuffix = StringHelper.EMPTY;

    private String tenantNo = StringHelper.EMPTY;

    @SuppressWarnings("Convert2MethodRef")
    private static final ThreadLocal<DataSourceManager> holder = ThreadLocal.withInitial(() -> new DataSourceManager());

    public static DataSourceManager get(){
        return holder.get();
    }

    String getDataSource() {
        return ds;
    }

    void setDataSource(String ds) {
        this.ds = ds;
    }

    boolean hasSpringTransaction() {
        return hasTransaction;
    }

    void setSpringTransaction(boolean hasTransaction) {
        this.hasTransaction = hasTransaction;
    }

    DynamicStrategy getSTG() {
        return strategy;
    }

    void setSTG(DynamicStrategy strategy) {
        this.strategy = strategy;
    }

    String getTableSuffix() {
        return tableSuffix;
    }

    void setTableSuffix(String tableSuffix) {
        this.tableSuffix = tableSuffix;
    }

    String getTenantNo() {
        return tenantNo;
    }

    @SuppressWarnings("unused")
    void setTenantNo(String tenantNo) {
        this.tenantNo = tenantNo;
    }

    public void clear() {
        holder.remove();
    }

    public static HikariDataSource newborn(String uri) {
        if (StringHelper.isBlank(uri)) {
            throw new MySqlException("MYSQL uri must not null/empty.....");
        }
        LOG.info("hikari datasource url: {}", uri);
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(uri);
        cfg.setMinimumIdle(2);
        cfg.setAutoCommit(false);
        cfg.setMaximumPoolSize(32);
        cfg.setIdleTimeout(300 * 1000L);
        cfg.setMaxLifetime(388 * 1000L);
        cfg.setDriverClassName("com.mysql.jdbc.Driver");
        cfg.addDataSourceProperty("useSSL", false);
        cfg.addDataSourceProperty("useUnicode", "true");
        cfg.addDataSourceProperty("loginTimeout", "30");
        cfg.addDataSourceProperty("autoReconnect", "true");
        // 允许一个标签中执行多条SQL
        cfg.addDataSourceProperty("allowMultiQueries", "true");
        cfg.addDataSourceProperty("characterEncoding", "utf8");
        cfg.addDataSourceProperty("prepStmtCacheSqlLimit", 8192);
        cfg.addDataSourceProperty("transformedBitIsBoolean", "true");
        // cfg.addDataSourceProperty("nullNamePatternMatchesAll", true);
        cfg.addDataSourceProperty("zeroDateTimeBehavior", "convertToNull");
        // cfg.addDataSourceProperty("serverTimezone", TimeZone.getDefault().getID());
        return new HikariDataSource(cfg);
    }

    // 获取 columns 信息
    public static List<TableColumn> columnsGet(Connection connection, String dbn, String table) {
        ResultSet set = null;
        try {
            List<TableColumn> columns = Lists.newArrayList();
            DatabaseMetaData metaData = connection.getMetaData();
            set = metaData.getColumns(dbn, dbn, table, null);
            while(set.next()){
                TableColumn col = new TableColumn();
                col.column = set.getString("COLUMN_NAME");
                col.dt = set.getString("TYPE_NAME");
                col.size = set.getInt("COLUMN_SIZE");
                col.scale = set.getInt("DECIMAL_DIGITS");
                col.nullAble = "YES".equals(set.getString("IS_NULLABLE"));
                col.comment = StringHelper.defaultString(set.getString("REMARKS"));
                columns.add(col);
            }
            return columns;
        } catch (Exception e) {
            throw new MySqlException("columnsGet error ", e);
        } finally {
            closeSqlResource(null, null, set);
        }
    }

    public static void closeSqlResource(Connection connection, Statement statement, ResultSet ...rs)  {
        try {
            if (rs != null && rs.length > 0) {
                for (ResultSet rSet : rs) {
                    if (null != rSet) {
                        rSet.close();
                    }
                }
            }
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (Exception e) {
            throw new MySqlException("closeSqlResource error ", e);
        }
    }
}

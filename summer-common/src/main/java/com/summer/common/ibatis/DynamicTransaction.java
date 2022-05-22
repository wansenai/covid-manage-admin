package com.summer.common.ibatis;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.summer.common.helper.CollectsHelper;
import javafx.util.Pair;
import org.apache.ibatis.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * 动态事务处理
 **/
public class DynamicTransaction implements Transaction {
    private static final Logger LOG = LoggerFactory.getLogger(DynamicTransaction.class);
    private final DataSource dataSource;
    private final ITransCompensate compensate;
    private final ConcurrentMap<String, Pair<Boolean, Connection>> attachedMap = Maps.newConcurrentMap();
    private boolean defaultAutoCommit;
    private boolean dynamicTransactional;
    private Connection defaultConnection;

    public DynamicTransaction(DataSource dataSource, ITransCompensate compensate) {
        this.dataSource = dataSource;
        this.compensate = compensate;
    }

    @Override
    public Connection getConnection() throws SQLException {
        String dsName = DataSourceManager.get().getDataSource();
        // 没有开启事务 或 默认数据源的情况
        if (!DataSourceManager.get().hasSpringTransaction() || IDynamicDS.DEFAULT.equals(dsName)) {
            return openDefaultConnection();
        } else {
            Pair<Boolean, Connection> accPair = attachedMap.get(dsName);
            if (null == accPair) {
                Connection connection = dataSource.getConnection();
                if (null != connection) {
                    boolean commit = connection.getAutoCommit();
                    connection.setAutoCommit(false);
                    accPair = new Pair<>(commit, connection);
                    attachedMap.put(dsName, accPair);
                } else {
                    throw new SQLException("dynamic datasource getConnection error");
                }
            }
            return accPair.getValue();
        }
    }

    @Override
    public void commit() throws SQLException {
        if (null != defaultConnection && !dynamicTransactional && !defaultAutoCommit) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("committing jdbc connection [" + defaultConnection + "]");
            }
            defaultConnection.commit();
        }
        // 有事务
        if (DataSourceManager.get().hasSpringTransaction()) {
            //事务成功提交的 connection
            Set<Connection> committedSet = Sets.newHashSet();
            try {
                // 很多情况下通过合理的业务校验，在非应用或网络故障的绝大多数情况下很少会出现多个datasource只有部分提交成功的情况。
                // 此时应用其实并不总是需要牺牲效率而获得真正的原子性（分布式事务）。
                // 我们只需要在commit事务时将我们业务中使用的多个datasource按使用顺序commit，而commit失败时停止操作并人工或自动补偿即可。
                for (Pair<Boolean, Connection> accPair : attachedMap.values()) {
                    accPair.getValue().commit();
                    committedSet.add(accPair.getValue());
                }
            } catch (Throwable cause) {
                //获取未提交事务的 connection
                Set<Connection> uncommittedSet = Sets.newHashSet();
                for (Pair<Boolean, Connection> accPair : attachedMap.values()) {
                    if (!committedSet.contains(accPair.getValue())) {
                        uncommittedSet.add(accPair.getValue());
                    }
                }
                //事务已经全部提交不做任务处理
                if (CollectsHelper.isNullOrEmpty(uncommittedSet)) {
                    return;
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("cause part transaction committed reason ", cause);
                }
                committedSet.add(defaultConnection);
                //事务部分被提交的处理方式
                compensate.dealing(committedSet, uncommittedSet, cause);
            }
        }
    }

    @Override
    public void rollback() throws SQLException {
        if (null != defaultConnection && !dynamicTransactional && !defaultAutoCommit) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("rolling jdbc connection [" + defaultConnection + "]");
            }
            this.defaultConnection.rollback();
        }
        //有事务
        if (DataSourceManager.get().hasSpringTransaction()) {
            for (Pair<Boolean, Connection> accPair : attachedMap.values()) {
                accPair.getValue().rollback();
            }
        }
    }

    @Override
    public void close() throws SQLException {
        //回收数据库连接到连接池
        if (null != defaultConnection) {
            DataSourceUtils.releaseConnection(defaultConnection, dataSource);
        }
        for (Pair<Boolean, Connection> accPair : attachedMap.values()) {
            boolean autoCommit = accPair.getValue().getAutoCommit();
            if (autoCommit != accPair.getKey()) {
                accPair.getValue().setAutoCommit(accPair.getKey());
            }
            DataSourceUtils.releaseConnection(accPair.getValue(), dataSource);
        }
        attachedMap.clear();
    }

    @Override
    public Integer getTimeout() {
        ConnectionHolder holder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
        if (null != holder && holder.hasTimeout()) {
            return holder.getTimeToLiveInSeconds();
        }
        return null;
    }

    //开启 defaultConnection
    private Connection openDefaultConnection() throws SQLException {
        if (null == defaultConnection) {
            this.defaultConnection = DataSourceUtils.getConnection(dataSource);
            this.defaultAutoCommit = this.defaultConnection.getAutoCommit();
            this.dynamicTransactional = DataSourceUtils.isConnectionTransactional(defaultConnection, dataSource);
        }
        return this.defaultConnection;
    }
}

package com.summer.common.ibatis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.TransactionRolledbackException;
import java.sql.Connection;
import java.util.Set;

public interface ITransCompensate {
    Logger LOG = LoggerFactory.getLogger(ITransCompensate.class);

    /**
     * 事务部分被提交的处理方式， 用户可根据需要自定义
     **/
    default void dealing(Set<Connection> committedSet, Set<Connection> uncommittedSet, Throwable ex) {
        try {
            rollbackUncommittedConnections(uncommittedSet);
        } catch (Throwable cause) {
            LOG.warn("part transaction committed then dealing error ", cause);
        }
    }

    /**
     * 事务没有提交的 connection 做回滚
     **/
    default void rollbackUncommittedConnections(Set<Connection> uncommittedSet) throws TransactionRolledbackException {
        try {
            for (Connection connection : uncommittedSet) {
                connection.rollback();
            }
        } catch (Exception e) {
            throw new TransactionRolledbackException("error to rollback uncommitted connections transaction...");
        }
    }
}

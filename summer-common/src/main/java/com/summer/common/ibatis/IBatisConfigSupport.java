package com.summer.common.ibatis;

import com.google.common.collect.Maps;
import com.summer.common.helper.StringHelper;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.util.Map;

@EnableTransactionManagement(proxyTargetClass = true)
@MapperScan(basePackages = {"com.*.**.mapper", "com.*.**.dao"})
public abstract class IBatisConfigSupport {
    private static final Map<String, DataSource> dsMap = Maps.newConcurrentMap();

    /**
     * 添加多个数据源
     **/
    protected abstract void addMultiDataSource(Environment env, Map<String, DataSource> dsMap);

    /**
     * 部分事务提交的处理，用户可根据需要自定义
     **/
    protected ITransCompensate transCompensate(ApplicationContext context) {
        return new ITransCompensate() {
        };
    }

    /**
     * Mapper.xml 包路径，默认为 Mapper.java 的路径
     **/
    @SuppressWarnings("WeakerAccess")
    protected String mapperLocations() {
        return "/**.xml";
    }

    /**
     * 创建数据源方法 MYSQL_URI=jdbc:mysql://{host:port}/{db}?user={username}&password={password}
     **/
    protected HikariDataSource hikariDataSource(String uri) {
        return DataSourceManager.newborn(uri);
    }

    @Bean
    @Inject
    @Primary
    public DynamicDataSource dynamicDataSource(Environment env) {
        addMultiDataSource(env, dsMap);
        DynamicDataSource dataSource = new DynamicDataSource();
        dataSource.setTargetDataSources(coverTargetDataSources(dsMap));
        dataSource.setDefaultTargetDataSource(dsMap.get(IDynamicDS.DEFAULT));
        return dataSource;
    }

    @Bean
    @Inject
    public SqlSessionFactory sqlSessionFactory(DynamicDataSource ds, ApplicationContext context) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(ds);
        //SqlSessionFactoryBean line-506  transactionFactory == null using SpringManagedTransactionFactory
        sessionFactory.setTransactionFactory(new SpringManagedTransactionFactory() {
            @Override
            public Transaction newTransaction(DataSource db, TransactionIsolationLevel l, boolean ac) {
                return new DynamicTransaction(db, transCompensate(context));
            }
        });
        sessionFactory.setConfigLocation(new ClassPathResource("/templates/mybatis-conf.xml"));
        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(locations()));
        return sessionFactory.getObject();
    }

    @Bean
    @Inject
    public PlatformTransactionManager transactionManager(DynamicDataSource dataSource) {
        return new DataSourceTransactionManager(dataSource) {
            @Override
            protected Object doGetTransaction() {
                //设置用户开启事务标志
                DataSourceManager.get().setSpringTransaction(true);
                return super.doGetTransaction();
            }

            @Override
            protected void doCleanupAfterCompletion(Object transaction) {
                super.doCleanupAfterCompletion(transaction);
                //清除用户开启事务标志
                DataSourceManager.get().setSpringTransaction(false);
            }
        };
    }


    @Bean
    public DynamicInterceptor dataSourceInterceptor() {
        return new DynamicInterceptor();
    }

    /**
     * PersistenceExceptionTranslationPostProcessor is a bean post processor
     * which adds an advisor to any bean annotated with Repository so that any
     * platform-specific exceptions are caught and then rethrown as one
     * Spring's unchecked data access exceptions (i.e. a subclass of
     * DataAccessException).
     */
    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @Bean
    public DataSourceHealth dataSourceHealth() {
        return new DataSourceHealth();
    }

    private Map<Object, Object> coverTargetDataSources(Map<String, DataSource> dsMap) {
        if (null == dsMap.get(IDynamicDS.DEFAULT)) {
            throw new MySqlException("Dynamic datasource must have IDynamicDS.DEFAULT ds and have the crud authority...");
        }
        Map<Object, Object> dsm = Maps.newHashMap();
        for (Map.Entry<String, DataSource> entry : dsMap.entrySet()) {
            dsm.put(entry.getKey(), entry.getValue());
        }
        return dsm;
    }

    private String locations() {
        String location = mapperLocations();
        return StringHelper.defaultString(location).startsWith("classpath:") ? location : ("classpath:" + location);
    }

    public static class DataSourceHealth implements HealthIndicator {
        @Override
        public Health health() {
            Health.Builder builder = Health.up();
            for (Map.Entry<String, DataSource> entry : dsMap.entrySet()) {
                builder.withDetail(entry.getKey(), entry.getValue().toString());
            }
            return builder.build();
        }
    }
}

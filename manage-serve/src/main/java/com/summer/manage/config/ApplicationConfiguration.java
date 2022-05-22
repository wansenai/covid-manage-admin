package com.summer.manage.config;

import com.summer.common.ibatis.IBatisConfigSupport;
import com.summer.common.redis.RedisConfigSupport;
import com.summer.common.redis.RedisOperations;
import com.summer.common.view.IWebAuthenticationFilter;
import com.summer.common.view.WebConfigurationSupport;
import com.summer.manage.core.LoginCodeProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.util.Map;

/**
 * 服务配置
 **/
@Configuration
public class ApplicationConfiguration {

    @Configuration
    public static class WebConfiguration extends WebConfigurationSupport {
        @Override
        @Bean
        @Inject
        protected IWebAuthenticationFilter injectAuthenticationFilter(ApplicationContext context) {
            return new WebAuthenticationFilter(context);
        }
    }

    @Configuration
    public static class IBatisConfiguration extends IBatisConfigSupport {
        @Override
        protected void addMultiDataSource(Environment env, Map<String, DataSource> dsMap) {
            dsMap.put(IBatisDS.DEFAULT, super.hikariDataSource(env.getProperty("mysql.uri.default")));
        }
    }

    @Configuration
    public static class RedisConfiguration extends RedisConfigSupport {
        @Override
        protected void addMultiRedisOperations(Environment env, Map<String, RedisOperations> redisMap) {
            redisMap.put(IRedis.DEFAULT, newborn(env.getProperty("single.redis.uri")));
        }
    }
//    /**
//     * 初始化mq默认配置
//     */
//    @Configuration
//    public static class RabbitConfiguration extends RabbitConfigSupport {
//        @Override
//        protected void addMultiRabbitOperations(Environment env, Map<String, RabbitOperations> rabbitMap) {
//            rabbitMap.put(IRabbitMQ.DEFAULT, makeRabbit(env.getProperty("rabbit.uri.default")));
//        }
//    }
        @Bean
        @ConfigurationProperties(prefix = "login", ignoreUnknownFields = true)
        public LoginCodeProperties loginCodeProperties() {
            return new LoginCodeProperties();
        }
}

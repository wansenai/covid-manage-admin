package com.summer.common.rabbit;

import com.google.common.collect.Maps;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.summer.common.helper.DateHelper;
import com.summer.common.helper.SnowIdHelper;
import com.summer.common.helper.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public final class RabbitFactory {
    private static final Logger LOG = LoggerFactory.getLogger(RabbitFactory.class);

    static final Map<String, RabbitOperations> RABBIT_MAP = Maps.newConcurrentMap();

    private static final ConcurrentMap<Class<?>, String> classNameMap = Maps.newConcurrentMap();

    public static RabbitOperations get(String rabbitId) {
        return RABBIT_MAP.get(rabbitId);
    }

    private RabbitFactory() {}

    /** RabbitMQ_URI=AMQP://{user:pwd}@{host:port}/{vHost} */
    public static Connection create(String mqUri) {
        LOG.info("rabbit mq uri: {}", mqUri);
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setUri(mqUri);
            factory.setAutomaticRecoveryEnabled(true);
            factory.setRequestedHeartbeat(ConnectionFactory.DEFAULT_HEARTBEAT/3);
            factory.setConnectionTimeout(ConnectionFactory.DEFAULT_CONNECTION_TIMEOUT/2);
            factory.setNetworkRecoveryInterval(ConnectionFactory.DEFAULT_SHUTDOWN_TIMEOUT/2);
            return factory.newConnection();
        } catch (Exception e) {
            throw new RuntimeException("create connection rabbit uri: " + mqUri, e);
        }
    }

    public static AMQP.BasicProperties propertyOf(String contentType, boolean durable) {
        return new AMQP.BasicProperties
                .Builder()
                .contentType(contentType)
                .deliveryMode(durable ? 2 : 1)
                .timestamp(DateHelper.now())
                .messageId(SnowIdHelper.uuid())
                .build();
    }

    public static String getContentType(Object message) {
        Class<?> clazz = message.getClass();
        String className = classNameMap.get(clazz);
        if(StringHelper.isBlank(className)){
            className = clazz.getSimpleName();
            classNameMap.put(clazz, className);
        }
        return className;
    }
}

package com.summer.common.rabbit;

import com.rabbitmq.client.Connection;
import com.summer.common.helper.SpringHelper;
import com.summer.common.helper.StringHelper;
import org.springframework.core.env.Environment;

import java.util.Map;

public abstract class RabbitConfigSupport {
    /** 添加多个RabbitMQ操作 **/
    protected abstract void addMultiRabbitOperations(Environment env, @SuppressWarnings("SameParameterValue") Map<String, RabbitOperations> rabbitMap);

    /** RabbitMQ_URI=AMQP://{user:pwd}@{host:port}/{vHost} */
    @SuppressWarnings("WeakerAccess")
    protected RabbitOperations makeRabbit(String uri) {
        if(StringHelper.isBlank(uri)) {
            throw new RuntimeException("rabbit mq uri must not blank or null");
        }
        return new RabbitOperations(connection(uri));
    }

    @SuppressWarnings("WeakerAccess")
    protected Connection connection(String uri) {
        return RabbitFactory.create(uri);
    }

    protected RabbitConfigSupport() {
        addMultiRabbitOperations(SpringHelper.getEnvironment(), RabbitFactory.RABBIT_MAP);
    }
}

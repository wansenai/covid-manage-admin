package com.summer.common.rabbit;

import com.google.common.collect.Sets;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

final class ChannelHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ChannelHelper.class);

    private static final Set<String> QUEUE_SET = Sets.newConcurrentHashSet();
    private static final Set<String> EXCHANGE_SET = Sets.newConcurrentHashSet();
    // 创建通道
    static Optional<Channel> channelOpt(Connection connection) {
        if (null == connection || !connection.isOpen()) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(connection.createChannel());
        } catch (IOException e) {
            LOG.error("Rabbit create channel error ", e);
            return Optional.empty();
        }
    }
    // 申明交换器
    static boolean declareExchange(Channel channel, String exchange, String type, boolean durable) {
        if (null != channel && channel.isOpen()) {
            if (!EXCHANGE_SET.contains(exchange)) {
                try {
                    channel.exchangeDeclare(exchange, type, durable);
                    EXCHANGE_SET.add(exchange);
                } catch (IOException e) {
                    LOG.error("DECLARE exchange={} with type={} durable={} error ", exchange, type, durable, e);
                    return false;
                }
            }
            return true;
        } else {
            LOG.error("DECLARE exchange with channel is null or not opened");
            return false;
        }
    }
    // 申明队列
    static boolean declareQueue(Channel channel, String queue, boolean durable) {
        if (null != channel && channel.isOpen()) {
            if (!QUEUE_SET.contains(queue)) {
                try {
                    channel.queueDeclare(queue, durable, false, false, null);
                    QUEUE_SET.add(queue);
                } catch (IOException e) {
                    LOG.error("DECLARE queue={} with durable={} error ", queue, durable, e);
                    return false;
                }
            }
            return true;
        } else {
            LOG.error("DECLARE queue with channel is null or not opened");
            return false;
        }
    }
    // 绑定队列与交换
    static boolean bindQueue(Channel channel, String queue, String exchange) {
        if (null != channel && channel.isOpen()) {
            try {
                channel.queueBind(queue, exchange, exchange);
                return true;
            } catch (IOException e) {
                LOG.error("BIND queue={} messages error ", queue, e);
                return false;
            }
        } else {
            LOG.error("BIND queue exchange with channel is null or not opened");
            return false;
        }
    }
    // 清空队列
    static boolean clearQueue(Channel channel, String queue) {
        if (null != channel && channel.isOpen()) {
            try {
                channel.queuePurge(queue);
                return true;
            } catch (IOException e) {
                LOG.error("CLEAR queue={} messages error ", queue, e);
                return false;
            }
        } else {
            LOG.error("CLEAR queue messages with channel is null or not opened");
            return false;
        }
    }
    // 删除交换器
    static boolean deleteExchange(Channel channel, String exchange) {
        if (null != channel && channel.isOpen()) {
            try {
                channel.exchangeDelete(exchange, true);
                return true;
            } catch (IOException e) {
                LOG.error("DELETE exchange={} error ", exchange, e);
                return false;
            }
        } else {
            LOG.error("DELETE exchange with channel is null or not opened");
            return false;
        }
    }
    // 删除队列
    static boolean deleteQueue(Channel channel, String queue) {
        if (null != channel && channel.isOpen()) {
            try {
                channel.queueDelete(queue, true, true);
                return true;
            } catch (IOException e) {
                LOG.error("DELETE queue={} error ", queue, e);
                return false;
            }
        } else {
            LOG.error("DELETE queue with channel is null or not opened");
            return false;
        }
    }
    // 获取队列中的消息数
    static long countMessages(Channel channel, String queue) {
        if (null != channel && channel.isOpen()) {
            try {
                return channel.messageCount(queue);
            } catch (IOException e) {
                LOG.error("COUNT queue={} messages error ", queue, e);
                return 0L;
            }
        } else {
            LOG.error("COUNT queue messages with channel is null or not opened");
            return 0L;
        }
    }
    // 获取队列中的消费者数量
    static long countConsumers(Channel channel, String queue) {
        if (null != channel && channel.isOpen()) {
            try {
                return channel.consumerCount(queue);
            } catch (IOException e) {
                LOG.error("COUNT queue={} consumers error ", queue, e);
                return 0L;
            }
        } else {
            LOG.error("COUNT queue consumers with channel is null or not opened");
            return 0L;
        }
    }
}

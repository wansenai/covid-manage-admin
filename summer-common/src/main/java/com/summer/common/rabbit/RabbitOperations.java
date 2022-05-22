package com.summer.common.rabbit;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Delivery;
import com.rabbitmq.client.Envelope;
import com.summer.common.helper.BytesHelper;
import com.summer.common.helper.JsonHelper;
import com.summer.common.helper.StringHelper;
import com.summer.common.helper.ThreadFactoryHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class RabbitOperations {
    private static final Logger LOG = LoggerFactory.getLogger(RabbitOperations.class);
    private static volatile Channel publisher;

    private final Connection connection;

    RabbitOperations(Connection connection) {
        this.connection = connection;
    }

    /**
     * 获取队列中的消息数
     **/
    public long countMessages(String queueN) {
        if (StringHelper.isBlank(queueN)) {
            return 0L;
        }
        return ChannelHelper.countMessages(channelGet(), queueN);
    }

    /**
     * 发布消息
     **/
    public boolean publish(IRabbitRouter router, Object msg) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("rabbit publisher msg to exchange={} with type={} msg={}", router.exchange(), router.exType(), JsonHelper.toJSONString(msg));
        }
        Channel channel = channelGet();
        if (null == channel) {
            return false;
        }
        try {
            ChannelHelper.declareExchange(channel, router.exchange(), router.exType(), router.durable());
            AMQP.BasicProperties props = RabbitFactory.propertyOf(RabbitFactory.getContentType(msg), router.durable());
            channel.basicPublish(router.exchange(), router.exchange(), props, msg instanceof byte[] ? (byte[]) msg : JsonHelper.toJSONBytes(msg));
            return true;
        } catch (IOException e) {
            LOG.error("rabbit publisher message error ", e);
            return false;
        }
    }

    /**
     * 订阅消息
     **/
    public SubscribeMonitor subscribe(IRabbitRouter router, final boolean exceptionAutoAck, RabbitMessageHandler messageHandler) {
        ExecutorService executor = Executors.newSingleThreadExecutor(ThreadFactoryHelper.threadFactoryOf(executorN(router)));
        Consumable consumable = new Consumable(connection, router, exceptionAutoAck, messageHandler);
        Future<?> future = executor.submit(consumable);
        return new SubscribeMonitor(future, consumable, executor);
    }

    // 保证 publisher 的单例
    private synchronized Channel channelGet() {
        if (null == publisher || !publisher.isOpen()) {
            synchronized (RabbitOperations.class) {
                if (null == publisher || !publisher.isOpen()) {
                    ChannelHelper.channelOpt(connection).ifPresent(chn -> publisher = chn);
                }
            }
        }
        return publisher;
    }

    // exchange queue
    private String executorN(IRabbitRouter router) {
        return router.exchange() + "--" + router.queue();
    }

    static final class Consumable implements Runnable {
        private final Connection connection;
        private final IRabbitRouter router;
        private final boolean exceptionAutoAck;
        private final RabbitMessageHandler messageHandler;
        private volatile boolean tries = true;
        private volatile MineConsumer consumer;

        Consumable(Connection connection, IRabbitRouter router, boolean autoAck, RabbitMessageHandler handler) {
            this.router = router;
            this.connection = connection;
            this.messageHandler = handler;
            this.exceptionAutoAck = autoAck;
        }

        @Override
        public void run() {
            if (LOG.isDebugEnabled()) {
                LOG.info("RABBIT consume exchange={} queue={}", router.exchange(), router.queue());
            }
            while (tries) {
                ChannelHelper.channelOpt(connection).ifPresent(chn -> {
                    // 交换器创建失败
                    if (!ChannelHelper.declareExchange(chn, router.exchange(), router.exType(), router.durable())) {
                        return;
                    }
                    // 队列创建失败
                    if (!ChannelHelper.declareQueue(chn, router.queue(), router.durable())) {
                        return;
                    }
                    // 队列与交换器绑定失败
                    if (!ChannelHelper.bindQueue(chn, router.queue(), router.exchange())) {
                        return;
                    }
                    consumer = new MineConsumer(chn);
                    try {
                        chn.basicQos(consumer.prefetch());
                        chn.basicConsume(router.queue(), false, consumer);
                    } catch (Exception e) {
                        LOG.error("RABBIT exchange={} queue={} error ", router.exchange(), router.queue(), e);
                        return;
                    }
                    Exception exception = consumer.loopConsume();
                    if (null != exception) {
                        LOG.error("RABBIT consume exchange={} queue={} error ", router.exchange(), router.queue(), exception);
                    }
                    consumer.close();
                });
                consumeRetry();
            }
        }

        void stop() {
            this.tries = false;
            consumer.stop();
        }

        private void consumeRetry() {
            if (tries) {
                LOG.info("RABBIT consume exchange={} queue={} about {}s to retry", router.exchange(), router.queue(), ConnectionFactory.DEFAULT_HANDSHAKE_TIMEOUT);
                try {
                    TimeUnit.MILLISECONDS.sleep(ConnectionFactory.DEFAULT_HANDSHAKE_TIMEOUT);
                } catch (InterruptedException ie) {
                    // Thread Interrupted error continue
                }
            }
        }

        private class MineConsumer extends DefaultConsumer {
            private final BlockingQueue<Delivery> blocking = new ArrayBlockingQueue<>(1);
            private volatile boolean onLoop = true;

            MineConsumer(Channel channel) {
                super(channel);
            }

            Exception loopConsume() {
                try {
                    while (onLoop) {
                        try {
                            nextDelivery();
                        } catch (InterruptedException ie) {
                            // MineConsumer Thread Interrupted continue
                        }
                    }
                    return null;
                } catch (Exception sse) {
                    return sse;
                }
            }

            int prefetch() {
                return blocking.size();
            }

            void stop() {
                this.onLoop = false;
            }

            void close() {
                Channel channel = super.getChannel();
                if (null != channel) {
                    try {
                        channel.basicCancel(super.getConsumerTag());
                    } catch (Exception e) {
                        LOG.error("RABBIT queue={} consumer close error {}", router.queue(), e.getMessage());
                    }
                }
            }

            @Override
            public void handleDelivery(String tag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                try {
                    blocking.put(new Delivery(envelope, properties, body));
                } catch (InterruptedException e) {
                    LOG.error("RABBIT consumer handle delivery exchange={} queue={} error ", router.exchange(), router.queue(), e);
                }
            }

            private void nextDelivery() throws InterruptedException, IOException {
                Delivery delivery = blocking.take();

                byte[] body = delivery.getBody();
                String type = delivery.getProperties().getContentType();
                String messageId = delivery.getProperties().getMessageId();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("RABBIT consume queue={} type={}, id={}, body={}", router.queue(), type, messageId, BytesHelper.string(body));
                }
                try {
                    messageHandler.handle(messageId, type, body);
                    super.getChannel().basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                } catch (Exception e) {
                    if (exceptionAutoAck) {
                        super.getChannel().basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                        LOG.error("RABBIT consume queue={} type={} body={} error ", router.queue(), type, BytesHelper.string(body), e);
                    }
                }
            }
        }
    }
}

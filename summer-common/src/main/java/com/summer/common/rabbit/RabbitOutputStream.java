package com.summer.common.rabbit;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.summer.common.helper.BytesHelper;
import com.summer.common.helper.ThreadFactoryHelper;
import com.summer.common.support.IConstant;
import com.summer.common.support.NaturalizeLog;
import com.summer.common.support.OperationLog;
import javafx.util.Pair;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class RabbitOutputStream extends OutputStream {
    private static final String TUNNEL = "LOG_BY_RABBIT_" + IConstant.APPENDER_STREAM;
    private static final String NL = NaturalizeLog.class.getName();
    private static final String OL = OperationLog.class.getName();
    private static final int OL_LEN = OL.length();

    private final ExecutorService executor = Executors.newSingleThreadExecutor(ThreadFactoryHelper.threadFactoryOf("LOG@RABBIT"));
    private final BlockingQueue<Pair<String, byte[]>> queue = new ArrayBlockingQueue<>(2);
    private final Connection connection;
    private final Channel channel;
    private final Future<?> task;
    private boolean loop = true;

    public RabbitOutputStream(String rabbitURI) throws IOException {
        this.connection = RabbitFactory.create(rabbitURI);
        this.channel = connection.createChannel();
        // 定义通道
        this.channel.exchangeDeclare(TUNNEL, "direct", true);
        this.task = startingWrite2Rabbit();
    }

    private static void degrade2console(byte[] ml, Throwable cause) {
        System.out.println("LOG by rabbit error log degrade to console");
        if (null != cause) cause.printStackTrace();
        System.out.println(BytesHelper.string(ml));
    }

    @Override
    public void write(@SuppressWarnings("NullableProblems") byte[] ml) {
        if (null == ml || ml.length < 1) return;
        String log = BytesHelper.string(ml);
        try {
            if (log.startsWith(OL)) {
                queue.put(new Pair<>(OL, BytesHelper.utf8Bytes(log.substring(OL_LEN))));
            } else {
                queue.put(new Pair<>(NL, ml));
            }
        } catch (Exception e) {
            degrade2console(ml, e);
        }
    }

    private Future<?> startingWrite2Rabbit() {
        return executor.submit(() -> {
            do {
                try {
                    Pair<String, byte[]> pair = queue.take();
                    if (rabbitOk()) {
                        try {
                            AMQP.BasicProperties props = RabbitFactory.propertyOf(pair.getKey(), true);
                            channel.basicPublish(TUNNEL, TUNNEL, props, pair.getValue());
                        } catch (Exception e) {
                            degrade2console(pair.getValue(), e);
                        }
                    }
                } catch (InterruptedException ie) {
                    System.out.println("LOG to elasticsearch error retry...");
                }
            } while (loop);
        });
    }

    @Override
    public void write(int b) {
    }

    @Override
    public void close() {
        this.task.cancel(true);
        BytesHelper.close(this.channel);
        BytesHelper.close(this.connection);
    }

    private boolean rabbitOk() {
        return null != connection && connection.isOpen() && null != this.channel && !this.channel.isOpen();
    }
}

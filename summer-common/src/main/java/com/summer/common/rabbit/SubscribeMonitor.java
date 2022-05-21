package com.summer.common.rabbit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class SubscribeMonitor {
    private final Future<?> future;
    private final ExecutorService executor;
    private final RabbitOperations.Consumable consumable;

    SubscribeMonitor(Future<?> future, RabbitOperations.Consumable consumable, ExecutorService executor) {
        this.future = future;
        this.executor = executor;
        this.consumable = consumable;
    }

    public void stop() {
        consumable.stop();
        future.cancel(true);
        executor.shutdown();
    }
}

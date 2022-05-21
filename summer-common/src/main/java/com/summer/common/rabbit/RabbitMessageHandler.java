package com.summer.common.rabbit;

public interface RabbitMessageHandler {
    void handle(String mid, String type, byte[] bytes);
}

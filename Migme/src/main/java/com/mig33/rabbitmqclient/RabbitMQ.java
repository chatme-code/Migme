package com.mig33.rabbitmqclient;
import com.mig33.rabbitmqclient.settings.Settings;
public abstract class RabbitMQ {
    protected RabbitMQ(Settings settings) {}
    public void publish(String exchange, String routingKey, byte[] body) throws Exception {}
    public void shutdown() {}
}

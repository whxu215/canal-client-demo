package com.github.canalclientdemo.kafka.message;

@FunctionalInterface
public interface IMessageHandler<V> {
  boolean handle(String topic, V message);
}

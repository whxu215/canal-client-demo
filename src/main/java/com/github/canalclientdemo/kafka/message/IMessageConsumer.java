package com.github.canalclientdemo.kafka.message;

public interface IMessageConsumer {
  void consume(String topic, IMessageHandler handler);

  boolean start();

  boolean stop();
}

package com.github.canalclientdemo.kafka.message;

@FunctionalInterface
public interface IMessageProducer {
  /**
   * 发送消息
   *
   * @param topic   - 主题.
   * @param message - 消息内容.
   * @return - true:发送成功；false:发送失败.
   */
  boolean send(String topic, Object message);
}

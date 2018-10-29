package com.github.canalclientdemo.kafka;

import com.github.canalclientdemo.kafka.message.IMessageConsumer;
import com.github.canalclientdemo.kafka.message.IMessageHandler;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class wraps kafka-client utilities to implement Kafka consumer functionality.
 *
 * @author panpanxu
 */
public class KafkaMessageConsumer<K, V> implements IMessageConsumer {

  private static final Logger logger = LoggerFactory.getLogger(KafkaMessageConsumer.class);

  private final AtomicBoolean stopped = new AtomicBoolean(false);

  private Map<String, Object> configs;

  /*
   * 重要：Kafka-client限定KafkaConsumer的初始化，subscribe，poll必须在一个线程里，不然会抛Exception中断消费，所以这是为什么consumer
   * 不能用注入的方式而直接new KafkaConsumer
   */
  private Consumer<K, V> consumer;

  private final Map<String, IMessageHandler> topicHandlerMap = new HashMap<>();

  public KafkaMessageConsumer(Map<String, Object> configs) {
    this.configs = configs;
  }

  @Override
  public void consume(String topic, IMessageHandler handler) {
    topicHandlerMap.put(topic, handler);
  }

  @Override
  public boolean start() {
    if (topicHandlerMap.size() == 0) {
      logger.warn("no consumers!!!");
      return false;
    }
    new Thread(createTarget(), "Kafka-consumer-thread").start();
    return true;
  }

  private Runnable createTarget() {
    return () -> {
      try {
        subscribeTopics();
      } catch (RuntimeException e) {
        handleException(e);
      } finally {
        recycleResources();
      }
    };
  }

  private void recycleResources() {
    consumer.close();
  }

  private void handleException(RuntimeException e) {
    // Ignore exception if closing
    if (!stopped.get()) {
      e.printStackTrace();
      throw e;
    }
  }

  private void subscribeTopics() {
    consumer = new KafkaConsumer<>(configs);
    consumer.subscribe(Arrays.asList(topicHandlerMap.keySet().toArray(new String[0])));
    while (!stopped.get()) {
      pollAndExecute();
    }
  }

  private void pollAndExecute() {
    ConsumerRecords<K, V> records = consumer.poll(10000);
    for (final ConsumerRecord<K, V> record : records) {
      try {
        createTask(record).run();
      } finally {
        consumer.commitSync();
      }
    }
  }

  private Runnable createTask(ConsumerRecord<K, V> record) {
    return () -> {
      final String topic = record.topic();
      final V value = record.value();
      final IMessageHandler handler = topicHandlerMap.get(topic);
      if (handler == null) {
        logger.error("no handler, topic:{}, message:{}", topic, value);
        return;
      }
      try {
        handler.handle(topic, value);
      } catch (Exception e) {
        logger.error("handle message failed. topic:{}, message:{}", topic, value, e);
      }
    };
  }

  @Override
  public boolean stop() {
    try {
      stopped.set(true);
      consumer.wakeup();
    } catch (Exception e) {
      logger.error("kafka consumer stop error.", e);
      return false;
    }
    return true;
  }
}

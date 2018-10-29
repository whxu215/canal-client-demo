package com.github.canalclientdemo;

import com.alibaba.otter.canal.kafka.client.MessageDeserializer;
import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalEntry.EntryType;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import com.alibaba.otter.canal.protocol.Message;
import com.github.canalclientdemo.kafka.KafkaMessageConsumer;
import com.github.canalclientdemo.kafka.message.IMessageConsumer;
import com.github.canalclientdemo.kafka.message.IMessageHandler;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.serialization.StringDeserializer;

public class BinlogConsumer {

  public static void main(String[] args) throws IOException {

    final IMessageConsumer consumer = new KafkaMessageConsumer<String, Message>(constructParams());
    consumer.consume("example", new IMessageHandler<Message>() {
      @Override
      public boolean handle(String topic, Message message) {
        return handleMessage(topic, message);
      }
    });
    consumer.start();
    System.in.read();

  }

  private static boolean handleMessage(String topic, Message message) {
    for (Entry entry : message.getEntries()) {
      if (entry.getEntryType() != EntryType.ROWDATA) {
        continue;
      }
      try {
        RowChange rowChange = RowChange.parseFrom(entry.getStoreValue());
        if (rowChange.getEventType() == EventType.INSERT
            || rowChange.getEventType() == EventType.UPDATE) {
          rowChange.getRowDatasList().forEach(rowData -> {
            rowData.getAfterColumnsList().forEach(column -> {
              System.out.println("column:" + column.getName() + ", value:" + column.getValue()
                  + ",isUpdated:" + column.getUpdated());
            });
          });
        }
      } catch (InvalidProtocolBufferException e) {
        e.printStackTrace();
      }

    }
    System.out
        .println("received message topic:" + topic + ", message:" + message.getId());
    return true;
  }

  private static Map<String, Object> constructParams() {
    Map<String, Object> map = new HashMap<>();
    map.put("bootstrap.servers", "127.0.0.1:9092");
    map.put("group.id", "testgroup9");
    map.put("enable.auto.commit", false);
    map.put("heartbeat.interval.ms", 5000);
    map.put("session.timeout.ms", 6000);
    map.put("max.poll.records", 100);
    map.put("key.deserializer", StringDeserializer.class.getName());
    map.put("value.deserializer", MessageDeserializer.class.getName());
    map.put("request.timeout.ms", 7000);
    map.put("auto.offset.reset", "latest");
    return map;
  }
}

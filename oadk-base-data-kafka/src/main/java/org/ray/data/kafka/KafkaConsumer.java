package org.ray.data.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.ray.oadk.core.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class KafkaConsumer {

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    /**
     * kafka listener示例
     * topics：要监听的topic
     * id：监听器对象标识
     * gid：消费者组标识
     * containerFactory：指定消费者工厂
     *
     * @param record
     */
//    @KafkaListener(topics = {"hello"},id = "id",groupId = "gid",containerFactory = "customFactory")
    public void listen(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
        }

    }


    public void startKafkaListener(String groupId) {
        MessageListenerContainer container = registry.getListenerContainer(groupId);
        if (container != null && !container.isRunning()) {
            container.start();
            LogUtil.log(LogUtil.LogLevel.INFO, "kafka listen start");
        }
    }

    public void stopKafkaListener(String groupId) {
        MessageListenerContainer container = registry.getListenerContainer(groupId);
        if (container != null && container.isRunning()) {
            container.stop();
            LogUtil.log(LogUtil.LogLevel.INFO, "kafka listen stop");
        }
    }
}

package org.ray.data.kafka.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    /**
     * 自定义kafka消费者监听
     *
     * @param configurer
     * @param consumerFactory
     * @return
     */
    @Bean("customFactory")
    public ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(ConcurrentKafkaListenerContainerFactoryConfigurer configurer,ConsumerFactory consumerFactory) {
        Map<String, Object> props = new HashMap<>();
        Map<String, Object> configurationProperties = consumerFactory.getConfigurationProperties();
        for (Map.Entry<String, Object> stringObjectEntry : configurationProperties.entrySet()) {
            props.put(stringObjectEntry.getKey(), stringObjectEntry.getValue());
        }
        //配置手动提交，设置enable.auto.commit=false
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        DefaultKafkaConsumerFactory<Object, Object> defaultKafkaConsumerFactory = new DefaultKafkaConsumerFactory<>(props);
        factory.setConsumerFactory(defaultKafkaConsumerFactory);
        //开启批量消费功能
//        factory.setBatchListener(true);
        //不自动启动
        factory.setAutoStartup(true);
        //手动提交，设置ack mode,设置这个值必须关闭enable.auto.commit
//        factory.getContainerProperties().setAckMode(AbstractMessageListenerContainer.AckMode.MANUAL_IMMEDIATE);
        configurer.configure(factory, defaultKafkaConsumerFactory);
        return factory;
    }
}

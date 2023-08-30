package org.ray.data.kafka;

import org.apache.commons.lang3.StringUtils;
import org.ray.oadk.core.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Component
public class KafkaProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;


    //发送消息方法
    public void send(TopicInterface topicInterface, Object message) {
//        om.writeValueAsString(message)
        String s = JsonUtil.writeValueAsString(message);
        if (StringUtils.isNotBlank(s)) {
            kafkaTemplate.send(topicInterface.getTopicKey(), s);
        }
    }
}

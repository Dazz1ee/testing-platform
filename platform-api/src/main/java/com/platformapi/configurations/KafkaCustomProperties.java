package com.platformapi.configurations;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "api.kafka")
public class KafkaCustomProperties {
    private String processTopic;
    private String addingTopic;
    private String processTopicDead;
    private String addingTopicDead;
}

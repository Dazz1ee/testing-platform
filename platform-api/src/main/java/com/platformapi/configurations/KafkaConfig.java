package com.platformapi.configurations;

import lombok.AllArgsConstructor;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import java.util.HashMap;
import java.util.Map;


@Configuration
@EnableConfigurationProperties(KafkaCustomProperties.class)
@EnableKafka
@AllArgsConstructor
public class KafkaConfig {
    private final KafkaProperties properties;
    private final KafkaCustomProperties customProperties;


    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers());
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic processTopic() {
        return TopicBuilder.name(customProperties.getProcessTopic())
                .partitions(10)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic addingTopic() {
        return TopicBuilder.name(customProperties.getAddingTopic())
                .partitions(10)
                .replicas(1)
                .build();
    }


    @Bean
    public NewTopic processTopicDead() {
        return TopicBuilder.name(customProperties.getProcessTopicDead())
                .partitions(3)
                .replicas(1)
                .build();
    }


    @Bean
    public NewTopic addingTopicDead() {
        return TopicBuilder.name(customProperties.getAddingTopicDead())
                .partitions(3)
                .replicas(1)
                .build();
    }


}

package com.platformapi.configurations;


import com.platformapi.exceptions.TestNotFoundException;
import com.platformapi.exceptions.TestNotStartedException;
import com.platformapi.exceptions.UnknownCustomException;
import com.platformapi.models.TestAnswer;
import com.platformapi.models.TransmittingTestDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@AllArgsConstructor
public class KafkaConsumerConfig {
    private final KafkaProperties properties;
    private final KafkaCustomProperties kafkaCustomProperties;

    @Bean
    public ConsumerFactory<String, TestAnswer> answersConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                properties.getBootstrapServers());
        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                properties.getConsumer().getGroupId());
        props.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, properties.getConsumer().getAutoOffsetReset());
        props.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                properties.getConsumer().getEnableAutoCommit());
        props.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        props.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(TestAnswer.class, false));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TestAnswer> kafkaListenerContainerFactory(
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        ConcurrentKafkaListenerContainerFactory<String, TestAnswer> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(answersConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        ExponentialBackOff backOff = new ExponentialBackOff(1000, 2);
        backOff.setMaxInterval(100000);
        ConsumerRecordRecoverer consumerRecordRecoverer = (consumerRecord, exception) -> {
            try {
                kafkaTemplate.send(kafkaCustomProperties.getProcessTopic(),
                        consumerRecord.key().toString(),
                        consumerRecord.value());
            } catch (Exception ex) {
                throw  new UnknownCustomException(ex);
            }
        };

        DefaultErrorHandler defaultErrorHandler = new DefaultErrorHandler(consumerRecordRecoverer, backOff);
        defaultErrorHandler.addRetryableExceptions(DataAccessException.class, TestNotFoundException.TestAlreadyFinished.class, TestNotStartedException.class);
        factory.setCommonErrorHandler(defaultErrorHandler);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, TransmittingTestDto> createTestConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                properties.getBootstrapServers());
        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                properties.getConsumer().getGroupId());
        props.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, properties.getConsumer().getAutoOffsetReset());
        props.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                properties.getConsumer().getEnableAutoCommit());
        props.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        props.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(TransmittingTestDto.class, false));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TransmittingTestDto> createTestListenerContainer(
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        ConcurrentKafkaListenerContainerFactory<String, TransmittingTestDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(createTestConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        ExponentialBackOff backOff = new ExponentialBackOff(1000, 2);
        backOff.setMaxInterval(100000);
        ConsumerRecordRecoverer consumerRecordRecoverer = (consumerRecord, exception) -> {
            try {
                kafkaTemplate.send(kafkaCustomProperties.getAddingTopic(),
                        consumerRecord.key().toString(),
                        consumerRecord.value());
            } catch (Exception ex) {
                throw  new UnknownCustomException(ex);
            }
        };

        DefaultErrorHandler defaultErrorHandler = new DefaultErrorHandler(consumerRecordRecoverer, backOff);
        defaultErrorHandler.addRetryableExceptions(DataAccessException.class, TestNotFoundException.TestAlreadyFinished.class, TestNotStartedException.class);
        factory.setCommonErrorHandler(defaultErrorHandler);
        return factory;
    }

}

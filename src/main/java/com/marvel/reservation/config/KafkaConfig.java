package com.marvel.reservation.config;

import com.marvel.reservation.dto.BankTransferEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@EnableKafka
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${custom.kafka.trusted-packages}")
    private String trustedPackages;

    @Bean
    public ConsumerFactory<String, BankTransferEvent> consumerFactory() {
        JsonDeserializer<BankTransferEvent> deserializer = new JsonDeserializer<>(BankTransferEvent.class);
        deserializer.addTrustedPackages(trustedPackages);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.setUseTypeMapperForKey(false);

        return new DefaultKafkaConsumerFactory<>(java.util.Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, groupId,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"),
                new StringDeserializer(),
                deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BankTransferEvent> kafkaListenerContainerFactory(KafkaTemplate<Object, Object> template) {
        ConcurrentKafkaListenerContainerFactory<String, BankTransferEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(new DeadLetterPublishingRecoverer(template), new FixedBackOff(1000L, 3));
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class, NullPointerException.class);
        errorHandler.setRetryListeners((record, ex, deliveryAttempt) -> {
            log.error("Retry attempt {} for record {} due to {}", deliveryAttempt, record.value(), ex.getMessage());
        });
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}
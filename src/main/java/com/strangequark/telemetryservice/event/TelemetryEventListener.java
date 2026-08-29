package com.strangequark.telemetryservice.event;

import com.strangequark.telemetryservice.utility.JwtUtility; // Integration line: Auth
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header; // Integration line: Auth
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Collection;
import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TelemetryEventListener {
    private final Logger LOGGER = LoggerFactory.getLogger(TelemetryEventListener.class);

    @Autowired
    TelemetryEventRepository telemetryEventRepository;
    // Integration function start: Auth
    @Autowired
    JwtUtility jwtUtility;
    // Integration function end: Auth

    @Bean
    public Collection<NewTopic> kafkaTopics() {
        return List.of(
                TopicBuilder.name("general-telemetry-events").partitions(1).replicas(1).build()
                ,TopicBuilder.name("auth-telemetry-events").partitions(1).replicas(1).build() // Integration line: Auth
                ,TopicBuilder.name("email-telemetry-events").partitions(1).replicas(1).build() // Integration line: Email
                ,TopicBuilder.name("file-telemetry-events").partitions(1).replicas(1).build() // Integration line: File
                ,TopicBuilder.name("vault-telemetry-events").partitions(1).replicas(1).build() // Integration line: Vault
                ,TopicBuilder.name("react-telemetry-events").partitions(1).replicas(1).build() // Integration line: React
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TelemetryEvent> kafkaListenerContainerFactory(ConsumerFactory<String, TelemetryEvent> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, TelemetryEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(1000L, 2)));

        return factory;
    }

    @KafkaListener(topics = "general-telemetry-events", groupId = "telemetry-group")
    public void generalTelemetryEvents(ConsumerRecord<String, TelemetryEvent> record) {
        LOGGER.info("General telemetry event received");
        saveTelemetryEvent(record);
    }
    // Integration function start: Auth
    @KafkaListener(topics = "auth-telemetry-events", groupId = "telemetry-group")
    public void authTelemetryEvents(ConsumerRecord<String, TelemetryEvent> record) {
        LOGGER.info("Auth telemetry event received");
        saveTelemetryEvent(record);
    }
    // Integration function end: Auth
    // Integration function start: Email
    @KafkaListener(topics = "email-telemetry-events", groupId = "telemetry-group")
    public void emailTelemetryEvents(ConsumerRecord<String, TelemetryEvent> record) {
        LOGGER.info("Email telemetry event received");
        saveTelemetryEvent(record);
    }
    // Integration function end: Email
    // Integration function start: File
    @KafkaListener(topics = "file-telemetry-events", groupId = "telemetry-group")
    public void fileTelemetryEvents(ConsumerRecord<String, TelemetryEvent> record) {
        LOGGER.info("File telemetry event received");
        saveTelemetryEvent(record);
    }
    // Integration function end: File
    // Integration function start: Vault
    @KafkaListener(topics = "vault-telemetry-events", groupId = "telemetry-group")
    public void vaultTelemetryEvents(ConsumerRecord<String, TelemetryEvent> record) {
        LOGGER.info("Vault telemetry event received");
        saveTelemetryEvent(record);
    }
    // Integration function end: Vault
    // Integration function start: React
    @KafkaListener(topics = "react-telemetry-events", groupId = "telemetry-group")
    public void reactTelemetryEvents(ConsumerRecord<String, TelemetryEvent> record) {
        LOGGER.info("React telemetry event received");
        saveTelemetryEvent(record);
    }
    // Integration function end: React
    // Integration function start: Auth
    public String getTokenFromKafkaConsumerRecord(ConsumerRecord<String, TelemetryEvent> record) {
        LOGGER.debug("Getting authorization token from Kafka consumer record");

        Header authHeader = record.headers().lastHeader("Authorization");
        if (authHeader == null) {
            LOGGER.error("Missing Authorization header in Kafka message");
            return null;
        }
        String token = new String(authHeader.value());

        if(!token.startsWith("Bearer ")) {
            LOGGER.error("Invalid Authorization header in Kafka message");
            return null;
        }

        LOGGER.debug("Kafka consumer authorization token retrieved");
        return token.substring(7);
    } // Integration function end: Auth

    private void saveTelemetryEvent(ConsumerRecord<String, TelemetryEvent> record) {
        TelemetryEvent telemetryEvent = record.value();

        // Integration function start: Auth
        String token = getTokenFromKafkaConsumerRecord(record);
        if(!jwtUtility.validateToken(token)) {
            LOGGER.error("Invalid JWT token - telemetry event skipped");
            return;
        }

        try {
            telemetryEvent.setServiceName(jwtUtility.getServiceNameFromToken(token));
        } catch(Exception ex) {
            LOGGER.error("JWT is not a service account token - telemetry event skipped");
            return;
        }
        // Integration function end: Auth

        telemetryEvent.setId(UUID.randomUUID());
        telemetryEvent.setTimestamp(LocalDateTime.now());
        telemetryEventRepository.save(telemetryEvent);
    }
}

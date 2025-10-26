package com.strangequark.telemetryservice.event;

import com.strangequark.telemetryservice.utility.JwtUtility; // Integration line: Auth
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collection;
import java.util.List;

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
        // Integration function start: Auth
        setAuthHeaderFromKafkaConsumerRecord(record);
        if(!jwtUtility.validateToken()) {
            LOGGER.error("Invalid JWT token - general telemetry event skipped");
            return;
        }
        // Integration function end: Auth

        telemetryEventRepository.save(record.value());
    }
    // Integration function start: Auth
    @KafkaListener(topics = "auth-telemetry-events", groupId = "telemetry-group")
    public void authTelemetryEvents(ConsumerRecord<String, TelemetryEvent> record) {
        LOGGER.info("Auth telemetry event received");
        // Integration function start: Auth
        setAuthHeaderFromKafkaConsumerRecord(record);
        if(!jwtUtility.validateToken()) {
            LOGGER.error("Invalid JWT token - auth telemetry event skipped");
            return;
        }
        // Integration function end: Auth

        telemetryEventRepository.save(record.value());
    }
    // Integration function end: Auth
    // Integration function start: Email
    @KafkaListener(topics = "email-telemetry-events", groupId = "telemetry-group")
    public void emailTelemetryEvents(ConsumerRecord<String, TelemetryEvent> record) {
        LOGGER.info("Email telemetry event received");
        // Integration function start: Auth
        setAuthHeaderFromKafkaConsumerRecord(record);
        if(!jwtUtility.validateToken()) {
            LOGGER.error("Invalid JWT token - email telemetry event skipped");
            return;
        }
        // Integration function end: Auth

        telemetryEventRepository.save(record.value());
    }
    // Integration function end: Email
    // Integration function start: File
    @KafkaListener(topics = "file-telemetry-events", groupId = "telemetry-group")
    public void fileTelemetryEvents(ConsumerRecord<String, TelemetryEvent> record) {
        LOGGER.info("File telemetry event received");
        // Integration function start: Auth
        setAuthHeaderFromKafkaConsumerRecord(record);
        if(!jwtUtility.validateToken()) {
            LOGGER.error("Invalid JWT token - file telemetry event skipped");
            return;
        }
        // Integration function end: Auth

        telemetryEventRepository.save(record.value());
    }
    // Integration function end: File
    // Integration function start: Vault
    @KafkaListener(topics = "vault-telemetry-events", groupId = "telemetry-group")
    public void vaultTelemetryEvents(ConsumerRecord<String, TelemetryEvent> record) {
        LOGGER.info("Vault telemetry event received");
        // Integration function start: Auth
        setAuthHeaderFromKafkaConsumerRecord(record);
        if(!jwtUtility.validateToken()) {
            LOGGER.error("Invalid JWT token - vault telemetry event skipped");
            return;
        }
        // Integration function end: Auth

        telemetryEventRepository.save(record.value());
    }
    // Integration function end: Vault
    // Integration function start: React
    @KafkaListener(topics = "react-telemetry-events", groupId = "telemetry-group")
    public void reactTelemetryEvents(ConsumerRecord<String, TelemetryEvent> record) {
        LOGGER.info("React telemetry event received");
        // Integration function start: Auth
        setAuthHeaderFromKafkaConsumerRecord(record);
        if(!jwtUtility.validateToken()) {
            LOGGER.error("Invalid JWT token - react telemetry event skipped");
            return;
        }
        // Integration function end: Auth

        telemetryEventRepository.save(record.value());
    }
    // Integration function end: React
    // Integration function start: Auth
    public void setAuthHeaderFromKafkaConsumerRecord(ConsumerRecord<String, TelemetryEvent> record) {
        LOGGER.info("Setting authorization header from Kafka consumer record");

        // Extract JWT from Kafka header
        Header authHeader = record.headers().lastHeader("Authorization");
        if (authHeader == null) {
            LOGGER.error("Missing Authorization header in Kafka message");
            return;
        }
        String token = new String(authHeader.value());

        // Create a mock request with Authorization header
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader("Authorization", token);

        // Bind the mock request to the current thread
        ServletRequestAttributes attrs = new ServletRequestAttributes(mockRequest);
        RequestContextHolder.setRequestAttributes(attrs);
        LOGGER.info("Kafka consumer auth header set");
    } // Integration function end: Auth
}

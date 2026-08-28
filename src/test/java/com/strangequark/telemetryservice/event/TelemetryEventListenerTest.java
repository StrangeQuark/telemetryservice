// Integration file: Auth

package com.strangequark.telemetryservice.event;

import com.strangequark.telemetryservice.utility.JwtUtility;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.*;

public class TelemetryEventListenerTest {
    private TelemetryEventListener telemetryEventListener;
    private TelemetryEventRepository telemetryEventRepository;
    private JwtUtility jwtUtility;

    @BeforeEach
    void setup() {
        telemetryEventRepository = mock(TelemetryEventRepository.class);
        jwtUtility = mock(JwtUtility.class);

        telemetryEventListener = new TelemetryEventListener();
        telemetryEventListener.telemetryEventRepository = telemetryEventRepository;
        telemetryEventListener.jwtUtility = jwtUtility;
    }

    @Test
    void generalTelemetryEventValidatesKafkaTokenTest() {
        TelemetryEvent telemetryEvent = new TelemetryEvent("test-service", "test-event", LocalDateTime.now());
        ConsumerRecord<String, TelemetryEvent> record = new ConsumerRecord<>("general-telemetry-events", 0, 0, "key", telemetryEvent);
        record.headers().add("Authorization", "Bearer test-token".getBytes());
        UUID suppliedId = telemetryEvent.getId();
        when(jwtUtility.validateToken("test-token")).thenReturn(true);
        when(jwtUtility.getServiceNameFromToken("test-token")).thenReturn("test");

        telemetryEventListener.generalTelemetryEvents(record);

        verify(telemetryEventRepository).save(telemetryEvent);
        assertNotEquals(suppliedId, telemetryEvent.getId());
        assertEquals("test", telemetryEvent.getServiceName());
    }

    @Test
    void generalTelemetryEventWithoutTokenIsSkippedTest() {
        TelemetryEvent telemetryEvent = new TelemetryEvent("test-service", "test-event", LocalDateTime.now());
        ConsumerRecord<String, TelemetryEvent> record = new ConsumerRecord<>("general-telemetry-events", 0, 0, "key", telemetryEvent);
        when(jwtUtility.validateToken(null)).thenReturn(false);

        telemetryEventListener.generalTelemetryEvents(record);

        verifyNoInteractions(telemetryEventRepository);
    }
}

package com.strangequark.telemetryservice.servicetests;

import com.strangequark.telemetryservice.event.TelemetryEvent;
import com.strangequark.telemetryservice.event.TelemetryEventRepository;
import com.strangequark.telemetryservice.telemetry.TelemetryService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

@DataMongoTest
@ActiveProfiles("test")
@Import(TelemetryService.class)
public class TelemetryServiceTest {

    static {
        System.setProperty("ENCRYPTION_KEY", "AA1A2A8C0E4F76FB3C13F66225AAAC42");
    }

    @Autowired
    TelemetryEventRepository telemetryEventRepository;

    @Autowired
    TelemetryService telemetryService;

    TelemetryEvent testEvent;
    final String testServiceName = "test-service";
    final String testEventType = "test-event-type";

    @BeforeEach
    void setup() {
        testEvent = new TelemetryEvent(testServiceName, testEventType, LocalDateTime.now());
        telemetryEventRepository.save(testEvent);
    }

    @AfterEach
    void teardown() {
        telemetryEventRepository.deleteAll();
    }

    @Test
    void createEventTest() {
        TelemetryEvent telemetryEvent =
                new TelemetryEvent(testServiceName, "createTestEvent", LocalDateTime.now());

        ResponseEntity<?> response = telemetryService.createEvent(telemetryEvent);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertTrue(telemetryEventRepository.existsById(telemetryEvent.getId()));
    }

    @Test
    void getEventsTest() {
        ResponseEntity<?> response = telemetryService.getEvents(testEventType, null, null, null);

        Assertions.assertEquals(200, response.getStatusCode().value());

        List<TelemetryEvent> telemetryEvents = (List<TelemetryEvent>) response.getBody();
        Assertions.assertNotNull(telemetryEvents, "Response body should not be null.");
        Assertions.assertFalse(telemetryEvents.isEmpty(), "Response list should not be empty.");


        boolean containsTestEvent = telemetryEvents.stream()
                // Compare the unique ID or the entire object
                .anyMatch(e -> e.getId().equals(testEvent.getId()));

        Assertions.assertTrue(containsTestEvent, "The retrieved events list must contain the pre-saved testEvent.");
    }

    @Test
    void countEventsTest() {
        ResponseEntity<?> response = telemetryService.countEvents(testServiceName, testEventType, null, null, null);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("{total=1}", response.getBody().toString());
    }
}


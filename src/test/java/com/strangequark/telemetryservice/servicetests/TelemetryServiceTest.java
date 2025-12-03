package com.strangequark.telemetryservice.servicetests;

import com.strangequark.telemetryservice.event.TelemetryEvent;
import com.strangequark.telemetryservice.event.TelemetryEventRepository;
import com.strangequark.telemetryservice.telemetry.TelemetryService;
import com.strangequark.telemetryservice.utility.JwtUtility; // Integration line: Auth
import org.junit.jupiter.api.*;
import org.mockito.Mockito; // Integration line: Auth
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // Integration line: Auth

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    @MockitoBean // Integration line: Auth
    private JwtUtility jwtUtility; // Integration line: Auth

    TelemetryEvent testEvent;
    final String testServiceName = "test-service";
    final String testEventType = "test-event-type";

    @BeforeEach
    void setup() {
        testEvent = new TelemetryEvent(testServiceName, testEventType, LocalDateTime.now());
        telemetryEventRepository.save(testEvent);
        Mockito.when(jwtUtility.validateToken()).thenReturn(true); // Integration line: Auth
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

        Map<String, Integer> responseMap = (Map<String, Integer>) response.getBody();
        Assertions.assertEquals(1, responseMap.get("total"));
    }
}


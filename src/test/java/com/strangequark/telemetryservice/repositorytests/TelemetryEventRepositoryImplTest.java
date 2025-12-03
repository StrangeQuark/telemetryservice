package com.strangequark.telemetryservice.repositorytests;

import com.strangequark.telemetryservice.event.TelemetryEvent;
import com.strangequark.telemetryservice.event.TelemetryEventRepository;
import com.strangequark.telemetryservice.event.TelemetryEventRepositoryImpl;
import com.strangequark.telemetryservice.telemetry.TelemetryService;
import com.strangequark.telemetryservice.utility.JwtUtility; // Integration line: Auth
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // Integration line: Auth

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@DataMongoTest
@ActiveProfiles("test")
@Import(TelemetryService.class)
public class TelemetryEventRepositoryImplTest {
    static {
        System.setProperty("ENCRYPTION_KEY", "AA1A2A8C0E4F76FB3C13F66225AAAC42");
    }

    @Autowired
    TelemetryEventRepository telemetryEventRepository;

    @Autowired
    TelemetryEventRepositoryImpl telemetryEventRepositoryImpl;
    @MockitoBean // Integration line: Auth
    private JwtUtility jwtUtility; // Integration line: Auth

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
    void getEventsByTypeTest() {
        List<TelemetryEvent> eventsList = telemetryEventRepositoryImpl.getEventsByEventType(testEventType, null, null, null);

        Assertions.assertEquals(1, eventsList.size());
        Assertions.assertEquals(testEvent.getId(), eventsList.getFirst().getId());
    }

    @Test
    void countEventsTest() {
        Map<String, Integer> eventsMap = telemetryEventRepositoryImpl.countEvents(testServiceName, testEventType, null, null, null);

        Assertions.assertEquals(1, eventsMap.entrySet().size());
        Assertions.assertEquals(1, eventsMap.get("total"));
    }
}

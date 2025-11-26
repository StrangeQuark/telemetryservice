package com.strangequark.telemetryservice.telemetry;

import com.strangequark.telemetryservice.event.TelemetryEvent;
import com.strangequark.telemetryservice.event.TelemetryEventRepository;
import com.strangequark.telemetryservice.event.TelemetryEventRepositoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TelemetryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TelemetryService.class);

    @Autowired
    TelemetryEventRepositoryImpl telemetryEventRepositoryImpl;

    @Autowired
    TelemetryEventRepository telemetryEventRepository;

    public ResponseEntity<?> createEvent(TelemetryEvent telemetryEvent) {
        try {
            if(telemetryEvent.getServiceName() == null)
                throw new RuntimeException("Service name must not be null");

            if(telemetryEvent.getEventType() == null)
                throw new RuntimeException("Event type must not be null");

            if(telemetryEvent.getTimestamp() == null)
                telemetryEvent.setTimestamp(LocalDateTime.now());

            telemetryEventRepository.save(telemetryEvent);

            return ResponseEntity.ok("Telemetry event successfully created");
        } catch (Exception ex) {
            LOGGER.error("Failed to create event: " + ex.getMessage());
            LOGGER.debug("Stack trace: ", ex);
            return ResponseEntity.status(404).body("Failed to create telemetry event: " + ex.getMessage());
        }
    }

    public ResponseEntity<?> getEvents(String eventType, String numberOfEvents, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if(startDateTime == null)
            startDateTime = LocalDateTime.now().minusDays(7);
        if(endDateTime == null)
            endDateTime = LocalDateTime.now();

        List<TelemetryEvent> events = telemetryEventRepositoryImpl.getEventsByEventType(eventType, numberOfEvents, startDateTime, endDateTime);

        return ResponseEntity.ok(events);
    }

    public ResponseEntity<?> countEvents(String serviceName, String eventType, String interval,
                                         LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if(startDateTime == null)
            startDateTime = LocalDateTime.now().minusDays(7);
        if(endDateTime == null)
            endDateTime = LocalDateTime.now();

        return ResponseEntity.ok(telemetryEventRepositoryImpl.countEvents(serviceName, eventType, interval, startDateTime, endDateTime));
    }
}

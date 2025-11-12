package com.strangequark.telemetryservice.telemetry;

import com.strangequark.telemetryservice.event.TelemetryEvent;
import com.strangequark.telemetryservice.event.TelemetryEventRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TelemetryService {
    @Autowired
    TelemetryEventRepositoryImpl telemetryEventRepository;

    public ResponseEntity<?> getEvents(String eventType, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null)
            startDateTime = LocalDateTime.now().minusDays(7);
        if (endDateTime == null)
            endDateTime = LocalDateTime.now();

        List<TelemetryEvent> events = telemetryEventRepository.getEventsByEventType(eventType, startDateTime, endDateTime);

        return ResponseEntity.ok(events);
    }
}

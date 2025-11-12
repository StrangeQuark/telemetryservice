package com.strangequark.telemetryservice.event;

import java.time.LocalDateTime;
import java.util.List;

public interface TelemetryEventRepositoryWrapper {
    List<TelemetryEvent> getEventsByEventType(String eventType, LocalDateTime start, LocalDateTime end);
}

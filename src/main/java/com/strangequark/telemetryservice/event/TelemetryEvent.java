package com.strangequark.telemetryservice.event;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Document(collection = "telemetry_events")
public class TelemetryEvent {
    @Id
    private UUID id = UUID.randomUUID();

    @NotNull
    private String serviceName;

    @NotNull
    private String eventType;

    @NotNull
    private LocalDateTime timestamp;

    private Map<String, Object> metadata;
    private String userId; // Integration line: Auth

    public TelemetryEvent() {

    }

    public TelemetryEvent(String serviceName, String eventType, LocalDateTime timestamp) {
        this.serviceName = serviceName;
        this.eventType = eventType;
        this.timestamp = timestamp;
    }

    public TelemetryEvent(String serviceName, String eventType, LocalDateTime timestamp, Map<String, Object> metadata) {
        this(serviceName, eventType, timestamp);
        this.metadata = metadata;
    }
    // Integration function start: Auth
    public TelemetryEvent(String serviceName, String eventType, LocalDateTime timestamp, String userId) {
        this(serviceName, eventType, timestamp);
        this.userId = userId;
    }

    public TelemetryEvent(String serviceName, String eventType, LocalDateTime timestamp, String userId, Map<String, Object> metadata) {
        this(serviceName, eventType, timestamp, userId);
        this.metadata = metadata;
    } // Integration function end: Auth

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    // Integration function start: Auth
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    } // Integration function end: Auth

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}

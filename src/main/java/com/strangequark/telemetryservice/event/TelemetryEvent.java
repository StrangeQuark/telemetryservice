package com.strangequark.telemetryservice.event;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Document(collection = "telemetry_events")
public class TelemetryEvent {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable=false)
    private String serviceName;

    @Column(nullable=false)
    private String eventType;

    @Column(nullable=false)
    private String userId;

    @Column(nullable=false)
    private LocalDateTime timestamp;

    private Map<String, Object> metadata;

    public TelemetryEvent() {

    }

    public TelemetryEvent(String serviceName, String eventType, String userId, LocalDateTime timestamp) {
        this.serviceName = serviceName;
        this.eventType = eventType;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    public TelemetryEvent(String serviceName, String eventType, String userId, LocalDateTime timestamp, Map<String, Object> metadata) {
        this(serviceName, eventType, userId, timestamp);
        this.metadata = metadata;
    }

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

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

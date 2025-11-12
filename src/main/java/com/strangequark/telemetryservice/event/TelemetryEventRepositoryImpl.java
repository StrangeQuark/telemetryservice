package com.strangequark.telemetryservice.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.util.List;

public class TelemetryEventRepositoryImpl implements TelemetryEventRepositoryWrapper {
    @Autowired
    private MongoTemplate mongoTemplate;

    private static final String COLLECTION = "telemetry_events";

    @Override
    public List<TelemetryEvent> getEventsByEventType(String eventType, LocalDateTime start, LocalDateTime end) {
        Criteria criteria = new Criteria();
        if (eventType != null)
            criteria.and("eventType").is(eventType);
        if (start != null && end != null) {
            criteria.and("timestamp").gte(start).lte(end);
        } else if (start != null) {
            criteria.and("timestamp").gte(start);
        } else if (end != null) {
            criteria.and("timestamp").lte(end);
        }

        Query query = new Query(criteria);
        query.with(Sort.by(Sort.Direction.DESC, "timestamp"));

        return mongoTemplate.find(query, TelemetryEvent.class, COLLECTION);
    }
}

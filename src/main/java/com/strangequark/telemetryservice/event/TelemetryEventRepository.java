package com.strangequark.telemetryservice.event;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelemetryEventRepository extends MongoRepository<TelemetryEvent, String> {
}

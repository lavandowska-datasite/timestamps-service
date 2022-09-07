package com.datasite.timestamps.kafka.model;

public class TimestampEvent {

    private String mongoId;
    private Long timestamp;

    public String getMongoId() {
        return mongoId;
    }

    public TimestampEvent setMongoId(String mongoId) {
        this.mongoId = mongoId;
        return this;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public TimestampEvent setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }
}

package com.datasite.timestamps.mongo.entity;

import org.springframework.data.mongodb.core.mapping.MongoId;

public class TimestampEntity {
    @MongoId
    private String id;
    private String contextKey;
    private String contextValue;
    private Long timestamp;

    public String getId() {
        return id;
    }

    public TimestampEntity setId(String id) {
        this.id = id;
        return this;
    }

    public Object getContextKey() {
        return contextKey;
    }

    public TimestampEntity setContextKey(String contextKey) {
        this.contextKey = contextKey;
        return this;
    }

    public String getContextValue() {
        return contextValue;
    }

    public TimestampEntity setContextValue(String contextValue) {
        this.contextValue = contextValue;
        return this;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public TimestampEntity setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }
}

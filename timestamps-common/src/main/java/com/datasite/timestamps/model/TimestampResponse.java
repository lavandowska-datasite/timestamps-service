package com.datasite.timestamps.model;

import java.time.Instant;

public class TimestampResponse {

    private Instant timestamp;

    public Instant getTimestamp() {
        return timestamp;
    }

    public TimestampResponse setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }
}

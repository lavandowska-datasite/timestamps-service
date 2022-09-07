package com.datasite.timestamps.kafka.publisher;

import com.datasite.timestamps.kafka.model.TimestampEvent;

public abstract class AbstractPublisher {

    protected AbstractPublisher() {
    }

    /**
     * The timestamp in the timestampEvent will be the value to set on the record
     * identified by the id - this is a MongoId (not a taskId, projectId, etc)
     * @param timestampEvent
     */
    public abstract void publishTimestampEvent(TimestampEvent timestampEvent);

}

package com.datasite.timestamps.kafka.publisher;

import com.datasite.timestamps.kafka.model.TimestampEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingPublisher extends AbstractPublisher{
    Logger logger = LoggerFactory.getLogger(LoggingPublisher.class);

    private final String topicName;

    public LoggingPublisher(String topicName) {
        super();
        this.topicName = topicName;
    }

    @Override
    public void publishTimestampEvent(TimestampEvent timestampEvent) {
        if (logger.isDebugEnabled()) {
            logger.debug("audit delete message for topic {} : {}", topicName, timestampEvent);
        }
    }
}

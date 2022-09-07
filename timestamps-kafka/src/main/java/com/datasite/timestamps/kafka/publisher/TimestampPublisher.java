package com.datasite.timestamps.kafka.publisher;

import com.datasite.timestamps.kafka.model.TimestampEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.SuccessCallback;

public class TimestampPublisher extends AbstractPublisher {
    Logger logger = LoggerFactory.getLogger(TimestampPublisher.class);

    private final String topicName;
    private final KafkaTemplate<String, Long> kafkaTemplate;

    SuccessCallback successCallback = new SuccessCallback() {
        @Override
        public void onSuccess(Object result) {
            logger.info("sent payload {}", result);
        }
    };

    FailureCallback failureCallback = new FailureCallback() {
        @Override
        public void onFailure(Throwable ex) {
            logger.error("unable to send payload {}", ex.getMessage());
        }
    };

    public TimestampPublisher(String topicName, KafkaTemplate<String, Long> kafkaTemplate) {
        super();
        this.topicName = topicName;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishTimestampEvent(TimestampEvent timestampEvent) {
        String documentId = timestampEvent.getMongoId();
        Long timestamp = timestampEvent.getTimestamp();
        logger.debug("publishing audit delete message to {} with key {} and value {}", topicName, documentId, timestamp);

        // set the message timestamp
        ProducerRecord producerRecord = new ProducerRecord<>(topicName, null, timestamp, documentId, timestamp);

        kafkaTemplate.send(producerRecord).addCallback(successCallback, failureCallback);
    }
}

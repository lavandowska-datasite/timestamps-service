package com.datasite.timestamps.kafka.publisher

import com.datasite.timestamps.kafka.model.TimestampEvent
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.LongSerializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.StreamsConfig
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.SendResult
import org.springframework.scheduling.annotation.AsyncResult
import spock.lang.Specification

import java.time.Instant

class TimestampPublisherSpec extends Specification {

    private ProducerFactory producerFactory

    private KafkaTemplate kafkaTemplate = Mock()

    String topic = "timestamps-topic"

    def setup() {
        producerFactory = new DefaultKafkaProducerFactory(Map.of(
            StreamsConfig.APPLICATION_ID_CONFIG, "TimestampPublisherTest",
            ProducerConfig.ACKS_CONFIG , "all",
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG , "localhost:9092",
            ProducerConfig.COMPRESSION_TYPE_CONFIG , "zstd",
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG , "true",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG , StringSerializer.class,
            ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION , "5",
            ProducerConfig.RETRIES_CONFIG , String.valueOf(Integer.MAX_VALUE),
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG , LongSerializer.class
        ));
    }

    def testPublishTimestampEvent() {
        given:
        TimestampEvent timestampEvent = new TimestampEvent().setMongoId("abc").setTimestamp(Instant.now().toEpochMilli())
        TimestampPublisher timestampPublisher = new TimestampPublisher(topic, kafkaTemplate)

        def timestamp = timestampEvent.getTimestamp()
        def mongoId = timestampEvent.getMongoId()
        ProducerRecord producerRecord = new ProducerRecord<>(topic, null, timestamp, mongoId, timestamp);

        when:
        timestampPublisher.publishTimestampEvent(timestampEvent)

        then:
        1 * kafkaTemplate.send(producerRecord) >> new AsyncResult<SendResult>(new SendResult<>(producerRecord, null))
        0 * _
    }
}

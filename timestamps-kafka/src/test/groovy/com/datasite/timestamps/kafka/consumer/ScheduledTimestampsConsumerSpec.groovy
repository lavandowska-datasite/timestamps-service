package com.datasite.timestamps.kafka.consumer


import com.datasite.timestamps.kafka.model.TimestampEvent
import com.datasite.timestamps.mongo.repository.TimestampRepository
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.PartitionInfo
import org.apache.kafka.common.TopicPartition
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import spock.lang.Specification

import java.time.Duration
import java.time.Instant

class ScheduledTimestampsConsumerSpec extends Specification {

    private String timestampTopic = "timestamps-topic"
    private KafkaTemplate kafkaTemplate = Mock()
    private ConsumerFactory consumerFactory = Mock()
    private KafkaConsumer kafkaConsumer = Mock()

    private TimestampRepository timestampRepository = Mock(TimestampRepository.class)
    private TimestampEvent timestampEvent = new TimestampEvent().setMongoId("mongoId").setTimestamp(Instant.now().toEpochMilli())

    private ScheduledTimestampsConsumer timestampsConsumer = new ScheduledTimestampsConsumer("0", timestampTopic, consumerFactory, timestampRepository)

    def cleanup() {
    }

    /**
     * confirm basic functionality (updating mongo record)
     */
    def testWhereTimestampIsNow() {
        given:

        when:
        timestampsConsumer.scheduledConsumer()

        then:
        1 * consumerFactory.createConsumer() >> kafkaConsumer
        1 * kafkaConsumer.subscribe(_ as Collection)
        1 * kafkaConsumer.seekToBeginning(_ as List)
        1 * kafkaConsumer.partitionsFor(timestampTopic) >> List.of(new PartitionInfo(timestampTopic, 0, null, null, null))
        1 * kafkaConsumer.poll(_ as Duration) >> new ConsumerRecords<String, Long>(
            Map.of(
                new TopicPartition(timestampTopic, 0),
                List.of(new ConsumerRecord<String, Long>(timestampTopic, 0, 0, timestampEvent.getMongoId(), timestampEvent.getTimestamp()))
            )
        )
        1 * timestampRepository.setById(timestampEvent.getMongoId(), timestampEvent.getTimestamp()) >> timestampEvent.getMongoId()
        1 * kafkaConsumer.commitSync(_ as Map)
        1 * kafkaConsumer.unsubscribe()
        1 * kafkaConsumer.close()
        0 * _
    }

    /**
     * confirms that messages with a future timestamp won't be processed
     */
    def testWhereTimestampIsFuture() {
        given:
        def thenNow = Instant.now().plusSeconds(60)

        when:
        timestampsConsumer.scheduledConsumer()

        then: "future event is not processed"
        1 * consumerFactory.createConsumer() >> kafkaConsumer
        1 * kafkaConsumer.subscribe(_ as Collection)
        1 * kafkaConsumer.seekToBeginning(_ as List)
        1 * kafkaConsumer.partitionsFor(timestampTopic) >> List.of(new PartitionInfo(timestampTopic, 0, null, null, null))
        1 * kafkaConsumer.poll(_ as Duration) >> new ConsumerRecords<String, Long>(
            Map.of(
                new TopicPartition(timestampTopic, 0),
                List.of(
                    new ConsumerRecord<String, Long>(timestampTopic, 0, 0, timestampEvent.getMongoId(), timestampEvent.getTimestamp()),
                    new ConsumerRecord<String, Long>(timestampTopic, 0, 0, timestampEvent.getMongoId(), thenNow.toEpochMilli())
                )
            )
        )
        1 * timestampRepository.setById(timestampEvent.getMongoId(), timestampEvent.getTimestamp()) >> timestampEvent.getMongoId()
        1 * kafkaConsumer.commitSync(_ as Map)
        1 * kafkaConsumer.unsubscribe()
        1 * kafkaConsumer.close()
        0 * _
    }

    /**
     * tests that older records are processed after downing of kafka
     */
    def testWhereTimestampIsPast() {
        given:
        timestampEvent.setTimestamp(Instant.now().minusSeconds(60).toEpochMilli())

        when:
        timestampsConsumer.scheduledConsumer()

        then: "past event is now processed"
        1 * consumerFactory.createConsumer() >> kafkaConsumer
        1 * kafkaConsumer.subscribe(_ as Collection)
        1 * kafkaConsumer.seekToBeginning(_ as List)
        1 * kafkaConsumer.partitionsFor(timestampTopic) >> List.of(new PartitionInfo(timestampTopic, 0, null, null, null))
        1 * kafkaConsumer.poll(_ as Duration) >> new ConsumerRecords<String, Long>(
            Map.of(
                new TopicPartition(timestampTopic, 0),
                List.of(
                    new ConsumerRecord<String, Long>(timestampTopic, 0, 0, timestampEvent.getMongoId(), timestampEvent.getTimestamp())
                )
            )
        )
        1 * timestampRepository.setById(timestampEvent.getMongoId(), timestampEvent.getTimestamp()) >> timestampEvent.getMongoId()
        1 * kafkaConsumer.commitSync(_ as Map)
        1 * kafkaConsumer.unsubscribe()
        1 * kafkaConsumer.close()
        0 * _

    }
}

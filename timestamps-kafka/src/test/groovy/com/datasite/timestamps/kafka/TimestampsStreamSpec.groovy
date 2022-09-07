package com.datasite.timestamps.kafka

import com.datasite.timestamps.kafka.model.TimestampEvent
import com.datasite.timestamps.mongo.repository.TimestampRepository
import org.apache.kafka.common.serialization.LongSerializer
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.TestInputTopic
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler
import org.apache.kafka.streams.test.TestRecord
import org.springframework.kafka.config.KafkaStreamsConfiguration
import spock.lang.Ignore
import spock.lang.Specification

import java.time.Duration
import java.time.Instant

@Ignore
class TimestampsStreamSpec extends Specification {

    private TimestampsStream timestampsStream

    private TopologyTestDriver topologyTestDriver

    private TestInputTopic<String, Long> inputTopic

    private KafkaStreamsConfiguration defaultKafkaStreamsConfig

    private TimestampRepository timestampRepository = Mock(TimestampRepository.class)

    TimestampEvent timestampEvent = new TimestampEvent().setMongoId("mongoId").setTimestamp(Instant.now().toEpochMilli())

    String topic = "timestamps-topic"

    def setup() {
        String appId = "TimestampsStreamTest"
        String bootstrapServer = "localhost:9092"
        defaultKafkaStreamsConfig = new KafkaStreamsConfiguration(
            Map.of(
                StreamsConfig.APPLICATION_ID_CONFIG, appId,
                StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer,
                StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0",
                StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, "1000",
                StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, new Serdes.StringSerde().getClass().getName(),
                StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, new Serdes.LongSerde().getClass().getName(),
                StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, new LogAndContinueExceptionHandler().getClass().getName(),
            )
        )

        StreamsBuilder builder = new StreamsBuilder()
        timestampsStream = new TimestampsStream(builder, topic, timestampRepository)
        // setting these values get the records processed immediately
        timestampsStream.WINDOW_SIZE = Duration.ofMillis(1)
        timestampsStream.GRACE_PERIOD = Duration.ofMillis(1)
        timestampsStream.MAX_RECORDS = 0

        Topology topology = timestampsStream.buildTopology()
        topologyTestDriver = new TopologyTestDriver(topology, defaultKafkaStreamsConfig.asProperties(), Instant.now())

        inputTopic = topologyTestDriver.createInputTopic(topic, new StringSerializer(), new LongSerializer())
    }

    def cleanup() {
        topologyTestDriver.close()
    }

    def testTopologyWhereTimestampIsNow() {
        given:

        when:
        inputTopic.pipeInput(timestampEvent.getMongoId(), timestampEvent.getTimestamp())

        then:
        1 * timestampRepository.setById(timestampEvent.getMongoId(), timestampEvent.getTimestamp()) >> timestampEvent.getMongoId()
        0 * _
    }

    def testTopologyWhereTimestampIsFuture() {
        given:
        def laterSeconds = 1
        def thenNow = Instant.now().plusSeconds(laterSeconds)
        timestampsStream.WINDOW_SIZE = Duration.ofSeconds(laterSeconds)
        timestampsStream.MAX_RECORDS = 1
        timestampEvent.setTimestamp(thenNow.toEpochMilli())
        // we can set the recordTime of the message this way too
        TestRecord testRecord = new TestRecord(timestampEvent.getMongoId(), timestampEvent.getTimestamp(), thenNow)

        when:
        inputTopic.pipeInput(testRecord)

        then: "future event is not processed"
        0 * timestampRepository.setById(timestampEvent.getMongoId(), timestampEvent.getTimestamp()) >> timestampEvent.getMongoId()
        0 * _

        sleep(timestampsStream.WINDOW_SIZE.toMillis())
        TestRecord testRecordLater = new TestRecord(timestampEvent.getMongoId()+"-Later", Instant.now().toEpochMilli(), Instant.now())

        when:
        inputTopic.pipeInput(testRecordLater)

        then: "process original event plus new event"
        1 * timestampRepository.setById(testRecordLater.key(), testRecordLater.value()) >> testRecordLater.key()
        1 * timestampRepository.setById(testRecord.key(), testRecord.value()) >> testRecord.key()
        0 * _
    }

    def testTopologyWhereTimestampIsPast() {
        given:
        timestampEvent.setTimestamp(Instant.now().minusSeconds(60).toEpochMilli())

        when:
        inputTopic.pipeInput(timestampEvent.getMongoId(), timestampEvent.getTimestamp())

        then:
        1 * timestampRepository.setById(timestampEvent.getMongoId(), timestampEvent.getTimestamp()) >> timestampEvent.getMongoId()
        0 * _
    }

    def testDuplicateMessages() {
        given:
        timestampsStream.WINDOW_SIZE = Duration.ofSeconds(1)
        timestampsStream.MAX_RECORDS = 10
        topologyTestDriver.close()
        Topology topology = timestampsStream.buildTopology()
        topologyTestDriver = new TopologyTestDriver(topology, defaultKafkaStreamsConfig.asProperties(), Instant.now())
        inputTopic = topologyTestDriver.createInputTopic(topic, new StringSerializer(), new LongSerializer())

        when:
        inputTopic.pipeInput(timestampEvent.getMongoId(), timestampEvent.getTimestamp())
        sleep((timestampsStream.WINDOW_SIZE.toMillis()/2).longValue()) // sleep less than window size
        timestampEvent.setTimestamp(Instant.now().toEpochMilli())
        inputTopic.pipeInput(timestampEvent.getMongoId(), timestampEvent.getTimestamp())

        then:
        // should only see the latest entry processed
        1 * timestampRepository.setById(timestampEvent.getMongoId(), _ as Long) >> timestampEvent.getMongoId()
        0 * _
    }

    def testTopologyWithMultipleMessages() {
        given:
        def now = Instant.now()
        TimestampEvent timestampEvent2 = new TimestampEvent().setMongoId("mongoId2").setTimestamp(now.minusMillis(200).toEpochMilli())
        TimestampEvent timestampEvent3 = new TimestampEvent().setMongoId("mongoId3").setTimestamp(now.minusMillis(300).toEpochMilli())
        TimestampEvent timestampEvent4 = new TimestampEvent().setMongoId("mongoId4").setTimestamp(now.minusMillis(400).toEpochMilli())
        TimestampEvent timestampEvent5 = new TimestampEvent().setMongoId("mongoId5").setTimestamp(now.minusMillis(500).toEpochMilli())

        when:
        inputTopic.pipeInput(timestampEvent.getMongoId(), timestampEvent.getTimestamp())
        inputTopic.pipeInput(timestampEvent2.getMongoId(), timestampEvent2.getTimestamp())
        inputTopic.pipeInput(timestampEvent3.getMongoId(), timestampEvent3.getTimestamp())
        inputTopic.pipeInput(timestampEvent4.getMongoId(), timestampEvent4.getTimestamp())
        inputTopic.pipeInput(timestampEvent5.getMongoId(), timestampEvent5.getTimestamp())

        then:
        5 * timestampRepository.setById(_ as String, _ as Long) >> "the same id"
        0 * _

        when:
        inputTopic.pipeInput(timestampEvent5.getMongoId(), timestampEvent5.getTimestamp()+100)

        then:
        1 * timestampRepository.setById(timestampEvent5.getMongoId(), timestampEvent5.getTimestamp()+100) >> timestampEvent5.getMongoId()
        0 * _
    }
}

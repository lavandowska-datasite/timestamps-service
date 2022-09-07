package com.datasite.timestamps.kafka;

import static org.apache.kafka.streams.kstream.Suppressed.BufferConfig.maxRecords;
import static org.apache.kafka.streams.kstream.Suppressed.untilTimeLimit;
import com.datasite.timestamps.mongo.repository.TimestampRepository;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.PostConstruct;
import java.time.Duration;

public class TimestampsStream {

    public Duration WINDOW_SIZE = Duration.ofHours(4); // how long to wait to reduce() records
    public Duration GRACE_PERIOD = Duration.ofHours(8); // how long to wait for late records
    public Integer MAX_RECORDS = 10000; // number of records to hold during WINDOW_SIZE before pushing release
    private final StreamsBuilder streamsBuilder;
    private final String timestampTopic;
    private final TimestampRepository timestampRepository;

    private final Logger logger = LoggerFactory.getLogger(TimestampsStream.class);

    public TimestampsStream(StreamsBuilder streamsBuilder, String topic, TimestampRepository timestampRepository) {
        this.streamsBuilder = streamsBuilder;
        this.timestampTopic = topic;
        this.timestampRepository = timestampRepository;
    }

    /**
     * This key of the topic is the MongoId of the timestamp to be updated,
     * the value is the time (in milliseconds) to set it to
     */
    @PostConstruct
    public Topology buildTopology() {

        KStream<String, Long> timestampStream = streamsBuilder.stream(timestampTopic, Consumed.with(new Serdes.StringSerde(), new Serdes.LongSerde()))
            .groupByKey()
            .windowedBy(TimeWindows.ofSizeAndGrace(WINDOW_SIZE, GRACE_PERIOD))
            .reduce((v1, v2) -> Long.max(v1, v2))
            .suppress(untilTimeLimit(WINDOW_SIZE, maxRecords(MAX_RECORDS)))
            //.suppress(untilWindowCloses(BufferConfig.unbounded()))
            .toStream()
            .map( (k, v) -> new KeyValue<>(k.key(), v.longValue()));

        timestampStream
            //.filter((id, timeLong) -> Instant.ofEpochMilli(timeLong).isBefore(Instant.now()))
            .peek((id, timeLong) -> logger.debug("updating {} to {}", id, timeLong))
            .mapValues((id, timeLong) -> setTimestamp(id, timeLong))
            .peek((key, value) -> logger.debug("returned mongoId {}, {}", key, value));

        return streamsBuilder.build();
    }

    private String setTimestamp(String id, Long newTimestamp) {
        return timestampRepository.setById(id, newTimestamp);
    }

}

package com.datasite.timestamps.kafka.consumer;

import com.datasite.timestamps.mongo.repository.TimestampRepository;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ScheduledTimestampsConsumer {

    protected final static Duration POLL_DURATION = Duration.ofSeconds(5);

    private final Logger logger = LoggerFactory.getLogger(ScheduledTimestampsConsumer.class);

    private final String consumerCron;
    private final ConsumerFactory consumerFactory;
    private final TimestampRepository timestampRepository;
    private final String topicName;

    public ScheduledTimestampsConsumer(String consumerCron, String topicName, ConsumerFactory consumerFactory, TimestampRepository timestampRepository) {
        this.consumerCron = consumerCron;
        this.topicName = topicName;
        this.consumerFactory = consumerFactory;
        this.timestampRepository = timestampRepository;
    }

    @Scheduled(cron = "${instance.consumerCron}")
    public void scheduledConsumer() {
        Consumer kafkaConsumer = this.consumerFactory.createConsumer();
        kafkaConsumer.subscribe(List.of(topicName));

        List<TopicPartition> topicPartitions = seekPartitionsToBeginning(kafkaConsumer);

        ConsumerRecords<String, Long> consumerRecords = kafkaConsumer.poll(POLL_DURATION);
        topicPartitions.forEach(topicPartition -> {
            updateCurrentTimestamps(kafkaConsumer, consumerRecords, topicPartition);
        });

        kafkaConsumer.unsubscribe();
        kafkaConsumer.close();
    }

    private void updateCurrentTimestamps(Consumer kafkaConsumer, ConsumerRecords<String, Long> consumerRecords, TopicPartition topicPartition) {
        Long lastOffset = 0L;
        Long now = Instant.now().toEpochMilli();
        Iterator<ConsumerRecord<String, Long>> iterator = consumerRecords.records(topicPartition).listIterator();
        while (iterator.hasNext()) {
            ConsumerRecord<String, Long> record = iterator.next();
            Long timestamp = record.value();
            if (timestamp < now) {
                String mongoId = record.key();
                String updatedId = timestampRepository.setById(mongoId, timestamp);
                logger.debug("returned mongoId {} for {}", mongoId, updatedId);
                lastOffset = timestamp;
            } else {
                logger.warn("message is for the future, exiting");
                break;
            }
        }
        commitLastOffset(kafkaConsumer, topicPartition, lastOffset);
    }

    private void commitLastOffset(Consumer kafkaConsumer, TopicPartition topicPartition, Long lastOffset) {
        kafkaConsumer.commitSync(Collections.singletonMap(
            topicPartition, new OffsetAndMetadata(lastOffset + 1)
        ));
    }

    private List<TopicPartition>  seekPartitionsToBeginning(Consumer kafkaConsumer) {
        List<PartitionInfo> partitions = kafkaConsumer.partitionsFor(topicName);
        List<TopicPartition> topicPartitions = new ArrayList<>();
        partitions.forEach(it -> {
            topicPartitions.add(new TopicPartition(topicName, it.partition()));
        });
        kafkaConsumer.seekToBeginning(topicPartitions);
        return topicPartitions;
    }
}

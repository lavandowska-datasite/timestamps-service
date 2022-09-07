package com.datasite.timestamps.kafka.consumer

import com.datasite.timestamps.kafka.model.TimestampEvent
import com.datasite.timestamps.kafka.publisher.TimestampPublisher
import com.datasite.timestamps.mongo.TimestampRepositoryConfig
import com.datasite.timestamps.mongo.repository.TimestampRepository
import com.mongodb.client.result.UpdateResult
import com.mrll.javelin.common.test.config.SingleEmbeddedMongoConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.context.annotation.DependsOn
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import spock.lang.Specification

import java.time.Instant

@SpringBootTest(
    classes = [TimestampsConsumerIntegrationApplication.class, SingleEmbeddedMongoConfiguration.class, TimestampRepositoryConfig.class],
    webEnvironment = WebEnvironment.NONE,
    properties = [
        "datasite.timestamps.kafka.enabled.enabled=true",
        "spring.kafka.security.enabled=false",
        "spring.data.mongodb.database=listsDB",
        "spring.application.name=integration-test-app",
    ])
@DirtiesContext
@DependsOn("embeddedMongoServer")
@EmbeddedKafka(partitions = 1, brokerProperties = ["listeners=PLAINTEXT://localhost:9092", "port=9092"])
class TimestampsConsumerIntegrationSpec extends Specification {

    @Autowired
    private ScheduledTimestampsConsumer timestampsConsumer

    @Autowired
    private TimestampPublisher timestampPublisher

    @Autowired
    private TimestampRepository timestampRepository

    def setup() {
        timestampRepository.deleteAll()
    }

    def testRecoveryAfterEncounteringFutureRecord() {
        given:
        long now = Instant.now().toEpochMilli()
        TimestampEvent timestampEvent1 = createTimestampEvent("before")
        timestampEvent1.setTimestamp(now)
        timestampPublisher.publishTimestampEvent(timestampEvent1)

        TimestampEvent timestampEvent2 = createTimestampEvent("later")
        // send message for future update
        timestampEvent2.setTimestamp(now+1000)
        timestampPublisher.publishTimestampEvent(timestampEvent2)

        when: "run the consumer immediately"
        timestampsConsumer.scheduledConsumer()

        then:
        assert timestampRepository.getById(timestampEvent1.mongoId).timestamp == timestampEvent1.timestamp
        assert timestampRepository.getById(timestampEvent2.mongoId).timestamp != timestampEvent2.timestamp

        when: "sleep for a second and execute the consumer again"
        sleep(1000)
        timestampsConsumer.scheduledConsumer()

        then:
        assert timestampRepository.getById(timestampEvent1.mongoId).timestamp == timestampEvent1.timestamp
        assert timestampRepository.getById(timestampEvent2.mongoId).timestamp == timestampEvent2.timestamp
    }

    private TimestampEvent createTimestampEvent(String suffix) {
        long before = Instant.now().toEpochMilli()-1000
        UpdateResult result = timestampRepository.upsertTimestampEntity("contextKey-"+suffix, "contextKey-"+suffix, before)
        return new TimestampEvent().setMongoId(result.getUpsertedId().asObjectId().getValue().toString())
    }

}

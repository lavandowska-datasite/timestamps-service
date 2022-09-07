package com.datasite.timestamps.kafka.consumer

import com.datasite.timestamps.kafka.config.TimestampsKafkaConfig
import com.datasite.timestamps.mongo.TimestampRepositoryConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import([TimestampsKafkaConfig.class,TimestampRepositoryConfig.class])
class TimestampsConsumerIntegrationApplication {
}

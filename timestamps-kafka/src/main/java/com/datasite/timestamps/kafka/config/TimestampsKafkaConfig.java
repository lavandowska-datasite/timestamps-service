package com.datasite.timestamps.kafka.config;

import com.datasite.timestamps.kafka.consumer.ScheduledTimestampsConsumer;
import com.datasite.timestamps.kafka.TimestampsStream;
import com.datasite.timestamps.kafka.publisher.AbstractPublisher;
import com.datasite.timestamps.kafka.publisher.LoggingPublisher;
import com.datasite.timestamps.kafka.publisher.TimestampPublisher;
import com.datasite.timestamps.mongo.TimestampRepositoryConfig;
import com.datasite.timestamps.mongo.repository.TimestampRepository;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyValueException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@EnableKafkaStreams
@EnableScheduling
@Import(TimestampRepositoryConfig.class)
public class TimestampsKafkaConfig {

    @Value("${datasite.timestamps.kafka.enabled:false}") private Boolean enabled; // primarily for local where kafka is likely not running
    @Value("${spring.application.name}") private String springApplicationName;
    @Value("${spring.kafka.bootstrap-servers:}") private String bootstrapServers;
    @Value("${spring.kafka.security.enabled:true}") private Boolean securityEnabled;
    @Value("${spring.kafka.security.sasl.username:}") private String saslUsername;
    @Value("${spring.kafka.security.sasl.password:}") private String saslPassword;
    @Value("${datasite.timestamps.kafka.cron}:0 */4 * * *") private String consumerCron; // run every 4 hours
    @Value("${datasite.timestamps.kafka.topic:timestamps-topic}") private String timestampTopic;

    @Bean
    public AbstractPublisher timestampEventPublisher(KafkaTemplate<String, Long> kafkaTemplate) {
        if (!enabled) {
            return new LoggingPublisher(timestampTopic);
        }

        validateKafkaConfiguration();
        return new TimestampPublisher(timestampTopic, kafkaTemplate);
    }

    @Bean
    @ConditionalOnProperty(name="datasite.timestamps.kafka.enabled", havingValue = "true")
    ScheduledTimestampsConsumer timestampsConsumer(ConsumerFactory consumerFactory, TimestampRepository timestampRepository) {
        return new ScheduledTimestampsConsumer(consumerCron, timestampTopic, consumerFactory, timestampRepository);
    }

    @Bean
    @ConditionalOnProperty(name="datasite.timestamps.kafka.enabled", havingValue = "true")
    public TimestampsStream timestampsStream(StreamsBuilder streamsBuilder, TimestampRepository timestampRepository) {
        return new TimestampsStream(streamsBuilder, timestampTopic, timestampRepository);
    }

    @Bean
    @ConditionalOnProperty(name="datasite.timestamps.kafka.enabled", havingValue = "true")
    public KafkaTemplate<String, Long> kafkaTemplate(ProducerFactory<String, Long> producerFactory) {
        return new KafkaTemplate(producerFactory);
    }

    @Bean
    @ConditionalOnProperty(name="datasite.timestamps.kafka.enabled", havingValue = "true")
    public ProducerFactory<String, Long> producerFactory() {
        Map kafkaConfig = new HashMap();
        if (securityEnabled) {
            kafkaConfig.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SASL_SSL.name);
            kafkaConfig.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
            kafkaConfig.put(SaslConfigs.SASL_JAAS_CONFIG,
                "org.apache.kafka.common.security.plain.PlainLoginModule required username='${saslUsername}' password='${saslPassword}';");
        }
        kafkaConfig.putAll(Map.of(
            ProducerConfig.ACKS_CONFIG , "all",
            ProducerConfig.CLIENT_ID_CONFIG, springApplicationName,
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG , bootstrapServers,
            ProducerConfig.COMPRESSION_TYPE_CONFIG , "zstd",
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG , "true",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG , StringSerializer.class,
            ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION , "5",
            ProducerConfig.RETRIES_CONFIG , String.valueOf(Integer.MAX_VALUE),
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG , LongSerializer.class
        ));
        return new DefaultKafkaProducerFactory(kafkaConfig);
    }

    @Bean
    @ConditionalOnProperty(name="datasite.timestamps.kafka.enabled", havingValue = "true")
    public ConsumerFactory consumerFactory() {
        Map consumerConfigs = Map.of(
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false,
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
            ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "1000",
            ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "1024000",
            ConsumerConfig.CLIENT_ID_CONFIG, springApplicationName,
            ConsumerConfig.GROUP_ID_CONFIG, springApplicationName,
            ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, "ScheduledTimestampsConsumer",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class
        );
        return new DefaultKafkaConsumerFactory<>(consumerConfigs, new StringDeserializer(), new LongDeserializer());
    }

    private void validateKafkaConfiguration() {
        if (bootstrapServers.isBlank()) {
            throw new InvalidConfigurationPropertyValueException(
                "spring.kafka.bootstrap-servers",
                bootstrapServers,
                "spring kafka bootstrap servers value must be configured if audit delete publishing is enabled"
            );
        }
        if (securityEnabled) {
            if (saslUsername.isBlank()) {
                throw new InvalidConfigurationPropertyValueException(
                    "spring.kafka.security.sasl.username",
                    saslUsername,
                    "username must be configured if kakfa security is enabled"
                );
            }
            if (saslPassword.isBlank()) {
                throw new InvalidConfigurationPropertyValueException(
                    "spring.kafka.security.sasl.password",
                    saslPassword,
                    "password must be configured if kakfa security is enabled"
                );
            }
        }
    }

}

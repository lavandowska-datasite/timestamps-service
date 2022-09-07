package com.datasite.timestamps.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ConsumeTimestampEventsTask {

    private final String consumerCron;
    private final ConsumerFactory consumerFactory;

    public ConsumeTimestampEventsTask(String consumerCron, ConsumerFactory consumerFactory) {
        this.consumerCron = consumerCron;
        this.consumerFactory = consumerFactory;
    }

    @Scheduled(cron = "${consumerCron}")
    public void scheduleTaskUsingCronExpression() {
        Consumer kafkaConsumer = this.consumerFactory.createConsumer();
    }
}

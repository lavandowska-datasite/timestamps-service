package com.datasite.timestamps.mongo;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackageClasses = TimestampRepositoryConfig.class)
public class TimestampRepositoryConfig {
}

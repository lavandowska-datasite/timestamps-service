package com.datasite.timestamps;

import com.datasite.timestamps.mongo.TimestampRepositoryConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(TimestampRepositoryConfig.class)
public class TimestampsServiceApplication {
}

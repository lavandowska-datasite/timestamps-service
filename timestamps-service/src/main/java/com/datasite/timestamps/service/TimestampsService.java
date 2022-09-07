package com.datasite.timestamps.service;

import com.datasite.timestamps.kafka.model.TimestampEvent;
import com.datasite.timestamps.kafka.publisher.TimestampPublisher;
import com.datasite.timestamps.model.RequestContext;
import com.datasite.timestamps.model.TimestampRequest;
import com.datasite.timestamps.model.TimestampResponse;
import com.datasite.timestamps.mongo.entity.TimestampEntity;
import com.datasite.timestamps.mongo.repository.TimestampRepository;
import java.time.Instant;

public class TimestampsService {

    private final TimestampPublisher timestampPublisher;
    private final TimestampRepository timestampRepository;

    public TimestampsService(TimestampPublisher timestampPublisher, TimestampRepository timestampRepository) {
        this.timestampPublisher = timestampPublisher;
        this.timestampRepository = timestampRepository;
    }

    public TimestampResponse search(TimestampRequest timestampRequest) {
        RequestContext requestContext = timestampRequest.getRequestContext();
        TimestampEntity timestampEntity = timestampRepository.search(requestContext);
        if (timestampEntity == null) {
            // default response is Epoch: Jan 1st 1970
            timestampEntity = new TimestampEntity().setTimestamp(0L);

            timestampRepository.set(requestContext, timestampEntity);
        }

        TimestampEvent timestampEvent = new TimestampEvent().setTimestamp(
            Instant.now().toEpochMilli() + timestampRequest.getContextType().timestampDelay
        ).setMongoId(
            timestampEntity.getId()
        );
        timestampPublisher.publishTimestampEvent(timestampEvent);

        return new TimestampResponse().setTimestamp(Instant.ofEpochMilli(timestampEntity.getTimestamp()));
    }
}

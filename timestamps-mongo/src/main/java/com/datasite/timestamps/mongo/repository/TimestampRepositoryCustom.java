package com.datasite.timestamps.mongo.repository;

import com.datasite.timestamps.model.RequestContext;
import com.datasite.timestamps.mongo.entity.TimestampEntity;
import com.mongodb.client.result.UpdateResult;

public interface TimestampRepositoryCustom {

    /**
     * Find a single record given the RequestContext, else return null
     * @param requestContext
     * @return A single TimestampEntity matching the RequestContext
     */
    TimestampEntity search(RequestContext requestContext);

    /**
     * Find a single record given the RequestContext, else return null
     * @param mongoId
     * @return A single TimestampEntity matching the mongoId
     */
    TimestampEntity getById(String mongoId);

    /**
     * Set a single record value, where the key is identified from the RequestContext
     * @param requestContext
     * @param timestampEntity
     * @return
     */
    TimestampEntity set(RequestContext requestContext, TimestampEntity timestampEntity);

    String setById(String mongoId, Long timestamp);

    UpdateResult upsertTimestampEntity(String contextKey, String contextValue, Long timestamp);
}

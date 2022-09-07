package com.datasite.timestamps.mongo.repository;

import com.datasite.timestamps.model.RequestContext;
import com.datasite.timestamps.mongo.entity.TimestampEntity;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class TimestampRepositoryCustomImpl implements TimestampRepositoryCustom {

    protected final MongoOperations mongoOperations;

    @Autowired
    public TimestampRepositoryCustomImpl(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    @Override
    public TimestampEntity search(RequestContext requestContext) {
        return getTimestampEntity(requestContext);
    }

    @Override
    public TimestampEntity getById(String mongoId) {
        return mongoOperations.findOne(new Query(Criteria.where("id").is(mongoId)), TimestampEntity.class);
    }

    private TimestampEntity getTimestampEntity(RequestContext requestContext) {
        return mongoOperations.findOne(getKeyQuery(requestContext), TimestampEntity.class);
    }

    private Query getKeyQuery(RequestContext requestContext) {
        return getKeyQuery(requestContext.asKey(), requestContext.asValue());
    }

    private Query getKeyQuery(String contextKey, String contextValue) {
        return new Query(
            Criteria.where("contextKey").is(contextKey)
                .and("contextValue").is(contextValue)
        );
    }

    @Override
    public TimestampEntity set(RequestContext requestContext, TimestampEntity timestampEntity) {
        UpdateResult result = upsertTimestampEntity(requestContext, timestampEntity);
        if (timestampEntity.getId() == null) {
            timestampEntity.setId(result.getUpsertedId().asObjectId().getValue().toString());
        }
        return timestampEntity;
    }

    @Override
    public String setById(String mongoId, Long timestamp) {
        UpdateResult result = mongoOperations.upsert(
            new Query(Criteria.where("id").is(mongoId)),
            new Update().set("timestamp", timestamp),
            TimestampEntity.class
        );
        return result.getUpsertedId().asObjectId().getValue().toString();
    }

    private UpdateResult upsertTimestampEntity(RequestContext requestContext, TimestampEntity timestampEntity) {
        return upsertTimestampEntity(requestContext.asKey(), requestContext.asValue(), timestampEntity.getTimestamp());
    }

    public UpdateResult upsertTimestampEntity(String contextKey, String contextValue, Long timestamp) {
        UpdateResult result = mongoOperations.upsert(
            getKeyQuery(contextKey, contextValue),
            new Update()
                .set("contextKey", contextKey)
                .set("contextValue", contextValue)
                .set("timestamp", timestamp),
            TimestampEntity.class
        );
        return result;
    }
}

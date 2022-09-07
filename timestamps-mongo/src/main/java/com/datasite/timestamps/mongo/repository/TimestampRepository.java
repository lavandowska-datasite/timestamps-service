package com.datasite.timestamps.mongo.repository;

import com.datasite.timestamps.mongo.entity.TimestampEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimestampRepository extends MongoRepository<TimestampEntity, String>, TimestampRepositoryCustom {

}

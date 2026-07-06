package com.innowise.paymentservice.db.migration;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import com.innowise.paymentservice.model.Payment;
import org.springframework.data.mongodb.core.MongoTemplate;

@ChangeUnit(
        id = "init-payments",
        order = "001",
        author = "admin"
)
public class InitCollectionChangeUnit {
    @Execution
    public void execution(MongoTemplate mongoTemplate) {
        if(mongoTemplate.collectionExists(Payment.class)) {
            mongoTemplate.createCollection(Payment.class);
        }
    }

    @RollbackExecution
    public void rollback(MongoTemplate mongoTemplate) {
        mongoTemplate.dropCollection(Payment.class);
    }
}

package com.innowise.paymentservice.db.migration;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import com.innowise.paymentservice.model.Payment;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

@ChangeUnit(
        id = "init-indexes",
        order = "002",
        author = "admin"
)
public class InitIndexesChangeUnit {
    @Execution
    public void execution(MongoTemplate mongoTemplate) {
        mongoTemplate.indexOps(Payment.class)
                .createIndex(new Index()
                        .on("order_id", Sort.Direction.ASC)
                        .unique());

        mongoTemplate.indexOps(Payment.class)
                .createIndex(new Index()
                        .on("user_id", Sort.Direction.ASC));

        mongoTemplate.indexOps(Payment.class)
                .createIndex(new Index()
                        .on("payment_status", Sort.Direction.ASC));
    }

    @RollbackExecution
    public void rollback(MongoTemplate mongoTemplate) {
        mongoTemplate.indexOps(Payment.class)
                .dropAllIndexes();
    }
}

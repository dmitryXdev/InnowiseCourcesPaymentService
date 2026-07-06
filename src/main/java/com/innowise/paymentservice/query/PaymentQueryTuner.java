package com.innowise.paymentservice.query;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class PaymentQueryTuner {
    public static void addCriteriaForField(Query query, String field, Object value) {
        if(value != null) {
            query.addCriteria(Criteria.where(field).is(value));
        }
    }

    public static void addCriteriaForFieldBetween(Query query, String field, Object from, Object to) {
        if(from != null && to != null) {
            query.addCriteria(Criteria.where(field).gte(from).lte(to));
        }
    }
}

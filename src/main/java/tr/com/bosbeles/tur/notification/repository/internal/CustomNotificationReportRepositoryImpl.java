package tr.com.bosbeles.tur.notification.repository.internal;

import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Mono;
import tr.com.bosbeles.tur.notification.model.NotificationReport;

import java.time.LocalDateTime;

public class CustomNotificationReportRepositoryImpl implements CustomNotificationReportRepository {

    @Autowired
    private ReactiveMongoOperations operations;


    @Override
    public Mono<UpdateResult> read(String userId, String notificationId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId).and("notificationId").is(notificationId).and("read").exists(false));

        Update update = new Update();
        update.set("read", LocalDateTime.now());
        return operations.upsert(query, update, NotificationReport.class).onErrorReturn(UpdateResult.unacknowledged());

    }

    @Override
    public Mono<UpdateResult> ack(String userId, String notificationId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId).and("notificationId").is(notificationId).and("ack").exists(false));
        Update update = new Update();
        update.set("ack", LocalDateTime.now());

        return operations.upsert(query, update, NotificationReport.class).onErrorReturn(UpdateResult.unacknowledged());
    }
}

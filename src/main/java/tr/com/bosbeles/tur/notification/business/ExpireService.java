package tr.com.bosbeles.tur.notification.business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import tr.com.bosbeles.tur.notification.model.Notification;
import tr.com.bosbeles.tur.notification.model.internal.State;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@Slf4j
public class ExpireService {


    @Autowired
    private MongoOperations operations;

    @Autowired
    private ChangeEmitter manager;

    //@Scheduled(fixedDelay = 5000)
    public void expireCheck() {
        Query query = new Query();

        LocalDateTime now = LocalDateTime.now();

        Criteria criteria = where("expireAt").lte(now).and("terminated").is(false);
        query.addCriteria(criteria);
        Update update = new Update();
        update.set("currentState", Notification.NotificationState.EXPIRED);
        update.push("states", new State(Notification.NotificationState.EXPIRED));
        update.set("terminated", true);
        update.set("modifiedAt", now);
        operations.updateMulti(query, update, Notification.class);

        query = new Query();
        query.addCriteria(where("currentState").is(Notification.NotificationState.EXPIRED).and("expireAt").lte(now).and("modifiedAt").is(now));

        List<Notification> notifications = operations.find(query, Notification.class);
        if (notifications.size() > 0) {
            log.info("{} notifications expired.", notifications.size());
        }

        for (Notification notification : notifications) {
            manager.emit(notification);
        }

    }
}

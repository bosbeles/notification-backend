package tr.com.bosbeles.tur.notification.repository.internal;

import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tr.com.bosbeles.tur.notification.model.Notification;
import tr.com.bosbeles.tur.notification.model.internal.State;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.springframework.data.mongodb.core.query.Criteria.where;

public class CustomNotificationRepositoryImpl implements CustomNotificationRepository {

    @Autowired
    private ReactiveMongoOperations operations;

    @Override
    public Flux<Notification> findByChannels(Collection<String> channels, LocalDateTime expireDate, Sort sort, boolean recursive) {
        Query query = new Query();

        query.addCriteria(
                where("terminated").is(false)
                        .andOperator(
                                new Criteria()
                                        .orOperator(
                                                where("expireAt").exists(false),
                                                where("expireAt").gt(expireDate)),
                                getChannelRegex(channels, recursive)));

        Flux<Notification> notifications = operations.find(query, Notification.class);

        return notifications;
    }


    private Criteria getChannelRegex(Collection<String> channels, boolean recursive) {
        Criteria[] inCriteria = new Criteria[recursive ? channels.size() + 1 : 1];
        inCriteria[0] = where("configuration.channel").in(channels);
        if (recursive) {
            int i = 1;
            for (String channel : channels) {
                inCriteria[i++] = where("configuration.channel").regex(channel.replaceAll("\\.", "\\\\.") + "\\.");
            }
        }

        return new Criteria().orOperator(inCriteria);
    }


    @Override
    public Mono<UpdateResult> incrementAck(Notification notification) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(notification.getId()));

        Update update = new Update();
        update.inc("configuration.acknowledgement.required", 1);
        return operations.updateFirst(query, update, Notification.class);
    }

    public void checkAckCount(Notification notification) {
        Criteria criteria = Criteria.where("id").is(notification.getId()).and("configuration.acknowledgement.required").gte(notification.getConfiguration().getAcknowledgement().getCount());
        criteria.and("terminated").is(false);


        Update update = new Update();
        update.push("states", new State(Notification.NotificationState.ACKED));
        update.set("currentState", Notification.NotificationState.ACKED);
        operations.updateFirst(new Query(Criteria.where("expireAt").orOperator(Criteria.where(""))), update, Notification.class);

    }

}

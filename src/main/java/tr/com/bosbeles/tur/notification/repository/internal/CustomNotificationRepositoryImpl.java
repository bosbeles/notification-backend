package tr.com.bosbeles.tur.notification.repository.internal;

import com.github.rutledgepaulv.qbuilders.builders.GeneralQueryBuilder;
import com.github.rutledgepaulv.qbuilders.conditions.Condition;
import com.github.rutledgepaulv.qbuilders.visitors.MongoVisitor;
import com.github.rutledgepaulv.rqe.pipes.QueryConversionPipeline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tr.com.bosbeles.tur.notification.model.Notification;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.springframework.data.mongodb.core.query.Criteria.where;

public class CustomNotificationRepositoryImpl implements CustomNotificationRepository {

    @Autowired
    private ReactiveMongoOperations operations;


    @Override
    public Flux<Notification> findByChannels(Collection<String> channels, LocalDateTime expireDate, String rsql, PageRequest pageRequest, Sort sort, boolean recursive) {
        Query query = new Query();
        query.addCriteria(
                where("terminated").is(false)
                        .andOperator(
                                new Criteria()
                                        .orOperator(
                                                where("expireAt").exists(false),
                                                where("expireAt").gt(expireDate)),
                                getChannelRegex(channels, recursive)));

        if (rsql != null) {
            QueryConversionPipeline pipeline = QueryConversionPipeline.defaultPipeline();
            Condition<GeneralQueryBuilder> condition = pipeline.apply(rsql, Notification.class);
            Criteria criteria = condition.query(new MongoVisitor());
            query.addCriteria(criteria);
        }

        if (sort != null) {
            query.with(sort);
        }

        if (pageRequest != null) {
            query.with(pageRequest);
        }


        Flux<Notification> notifications = operations.find(query, Notification.class).log();

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
    public Mono<Void> incrementAck(Notification notification) {
        Query query = new Query();

        QueryConversionPipeline pipeline = QueryConversionPipeline.defaultPipeline();
        Condition<GeneralQueryBuilder> condition = pipeline.apply("id==" + notification.getId(), Notification.class);
        Criteria criteria = condition.query(new MongoVisitor());

        query.addCriteria(criteria);
        //query.addCriteria(Criteria.where("id").is(notification.getId()));

        Update update = new Update();
        update.inc("configuration.acknowledgement.count", 1);


        return operations.updateFirst(query, update, Notification.class)
                .flatMap(updateResult -> operations.findById(notification.getId(), Notification.class))
                .flatMap(n -> checkAckCount(n)).then();
    }

    @Override
    public Mono<Notification> checkAckCount(Notification notification) {
        if (notification.advanceState()) {
            return operations.save(notification);
        } else {
            return Mono.just(notification);
        }

    }

}

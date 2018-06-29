package tr.com.bosbeles.tur.notification.repository.internal;

import com.mongodb.client.result.UpdateResult;
import reactor.core.publisher.Mono;

public interface CustomNotificationReportRepository {

    Mono<UpdateResult> read(String userId, String notificationId);

    Mono<UpdateResult> ack(String userId, String notificationId);
}

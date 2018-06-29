package tr.com.bosbeles.tur.notification.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import tr.com.bosbeles.tur.notification.model.NotificationReport;
import tr.com.bosbeles.tur.notification.repository.internal.CustomNotificationReportRepository;

public interface NotificationReportRepository extends ReactiveCrudRepository<NotificationReport, String>, CustomNotificationReportRepository {

    Mono<NotificationReport> findNotificationReportByNotificationIdAndUserId(String notificationId, String userId);

}

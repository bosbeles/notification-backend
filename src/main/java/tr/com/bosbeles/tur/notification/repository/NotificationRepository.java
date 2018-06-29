package tr.com.bosbeles.tur.notification.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import tr.com.bosbeles.tur.notification.model.Notification;
import tr.com.bosbeles.tur.notification.repository.internal.CustomNotificationRepository;

public interface NotificationRepository extends ReactiveCrudRepository<Notification, String>, CustomNotificationRepository {


}

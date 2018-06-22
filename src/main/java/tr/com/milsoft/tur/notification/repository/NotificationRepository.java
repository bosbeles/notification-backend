package tr.com.milsoft.tur.notification.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import tr.com.milsoft.tur.notification.model.Notification;

public interface NotificationRepository  extends ReactiveCrudRepository<Notification, String> {
}

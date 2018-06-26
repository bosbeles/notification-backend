package tr.com.bosbeles.tur.notification.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import tr.com.bosbeles.tur.notification.model.NotificationTemplate;

public interface NotificationTemplateRepository extends ReactiveCrudRepository<NotificationTemplate, String> {
}

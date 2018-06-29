package tr.com.bosbeles.tur.notification.repository.internal;

import com.mongodb.client.result.UpdateResult;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tr.com.bosbeles.tur.notification.model.Notification;

import java.time.LocalDateTime;
import java.util.Collection;

public interface CustomNotificationRepository {
    Flux<Notification> findByChannels(Collection<String> channels, LocalDateTime expireDate, Sort sort, boolean recursive);

    Mono<UpdateResult> incrementAck(Notification notification);
}

package tr.com.bosbeles.tur.notification.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import tr.com.bosbeles.tur.notification.model.internal.Configuration;
import tr.com.bosbeles.tur.notification.model.internal.State;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(of = {"id"})
@Document(collection = "notifications")
public class Notification {

    public enum NotificationType {SIMPLE, ACKED, ACTIONED}

    public enum NotificationState {CREATED, UPDATED, ACKED, ASSIGNED, HANDLED, EXPIRED, CANCELLED}

    ;

    @Id
    private String id;
    private NotificationTemplate template;
    private boolean terminated;

    private Configuration configuration;
    private Map<String, Object> data;

    private List<State> states;
    private NotificationState currentState;

    @Version
    private Long version;


    public NotificationType getNotificationType() {
        NotificationType type = NotificationType.SIMPLE;
        if (configuration.getAction() != null && configuration.getAction().getRequired() > 0) {
            type = NotificationType.ACTIONED;
        } else if (configuration.getAcknowledgement() != null && configuration.getAcknowledgement().getRequired() > 0) {
            type = NotificationType.ACKED;
        }

        return type;
    }


}
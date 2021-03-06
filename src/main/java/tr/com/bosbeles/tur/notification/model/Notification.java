package tr.com.bosbeles.tur.notification.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import tr.com.bosbeles.tur.notification.model.internal.Configuration;
import tr.com.bosbeles.tur.notification.model.internal.State;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(of = {"id"})
@Document(collection = "notifications")
public class Notification {

    public enum NotificationType {SIMPLE, ACKED, ACTIONED}

    public enum NotificationState {CREATED, UPDATED, ACKED, ASSIGNED, HANDLED, EXPIRED, CANCELLED}

    @Id
    private String id;
    private NotificationTemplate template;
    private boolean terminated;

    private Configuration configuration;
    private Map<String, Object> data;

    private List<State> states;
    private NotificationState currentState;

    private Instant expireAt;

    @LastModifiedDate
    private Instant modifiedAt;

    @Version
    private Long version;


    public Notification() {

    }


    public Notification(Notification copy) {
        setData(copy.getData());
        setConfiguration(copy.getConfiguration());
    }

    public NotificationType getNotificationType() {
        NotificationType type = NotificationType.SIMPLE;
        if (configuration.getAction() != null && configuration.getAction().getRequired() > 0) {
            type = NotificationType.ACTIONED;
        } else if (configuration.getAcknowledgement() != null && configuration.getAcknowledgement().getRequired() > 0) {
            type = NotificationType.ACKED;
        }

        return type;
    }

    public boolean advanceState() {
        if (isTerminated()) {
            return false;
        }
        switch (currentState) {
            case HANDLED:
            case CANCELLED:
            case EXPIRED:
                terminated = true;
                break;
            case ACKED:
                if (getConfiguration().getAction().getRequired() <= 0) {
                    terminated = true;
                }
                break;
            default:
                return otherCases();
        }
        return terminated;
    }


    public void fill() {
        if (configuration == null) {
            configuration = new Configuration();
        }
        if (states == null) {
            states = new ArrayList<>();
        }
        configuration.fill();
    }

    public void clear() {
        terminated = false;
        states = null;
        version = 0l;

    }

    private boolean otherCases() {
        int required = getConfiguration().getAcknowledgement().getRequired();
        if (required > 0 && required <= getConfiguration().getAcknowledgement().getCount()) {
            currentState = NotificationState.ACKED;
            states.add(new State(currentState));
            if (getConfiguration().getAction().getRequired() <= 0) {
                terminated = true;
            }
        } else if (expireAt != null && Instant.now().isAfter(expireAt)) {
            currentState = NotificationState.EXPIRED;
            states.add(new State(currentState));
            terminated = true;
        } else {
            return false;
        }
        return true;
    }


}
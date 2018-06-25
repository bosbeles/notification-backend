package tr.com.bosbeles.tur.notification.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;
import tr.com.bosbeles.tur.notification.model.Notification;
import tr.com.bosbeles.tur.notification.model.NotificationReport;
import tr.com.bosbeles.tur.notification.model.NotificationTemplate;
import tr.com.bosbeles.tur.notification.model.internal.Assignee;
import tr.com.bosbeles.tur.notification.model.internal.Configuration;
import tr.com.bosbeles.tur.notification.model.internal.State;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationManager {

    @Autowired
    private MongoOperations mongo;


    public void create(Notification notification) {
        if (notification.getTemplate() != null) {
            String templateId = notification.getTemplate().getId();
            NotificationTemplate template = null; // Fetch from store
            if (template != null) {
                Configuration configuration = template.getConfiguration();
                if (notification.getConfiguration() == null) {
                    notification.setConfiguration(configuration);
                } else {
                    notification.getConfiguration().merge(template);
                }
                if (notification.getData() == null) {
                    notification.setData(new HashMap<>());
                }
                Map<String, Object> templateMap = template.getData();
                templateMap.forEach((k, v) -> {
                    notification.getData().putIfAbsent(k, v);
                });
            }
        }

        notification.setCurrentState(Notification.NotificationState.CREATED);
        notification.getStates().add(new State(Notification.NotificationState.CREATED));
        // persist
    }

    public void read(String user, String notificationId) {
        NotificationReport report = new NotificationReport();
        report.setRead(LocalDateTime.now());
        // if not exists then put

    }

    public void ack(String user, String notificationId) {
        // Upsert
        // Old da ack yoksa
        // Ack yi bir arttir
        // Ack > 0 && Ack == AckCount ise set state to ack if state not timeout, handled, cancelled
        // emit channel
        Notification notification = null;
        Notification.NotificationType type = notification.getNotificationType();
        if (type == Notification.NotificationType.SIMPLE) {

            return;
        }
        Notification.NotificationState currentState = notification.getCurrentState();
        if (currentState != Notification.NotificationState.CREATED || currentState != Notification.NotificationState.UPDATED) {

            return;
        }
    }

    public void handle(String user, String notificationId) {
        // if acknowledged,
        Notification notification = null;
        Notification.NotificationType type = notification.getNotificationType();
        if (type != Notification.NotificationType.ACTIONED) {

            return;
        }
        Notification.NotificationState currentState = notification.getCurrentState();
        if (currentState != Notification.NotificationState.ASSIGNED) {

            return;
        }
    }

    public void changeAssignee( String notificationId, Assignee assignee) {
        // if acknowledged,
        Notification notification = null;
        Notification.NotificationType type = notification.getNotificationType();
        if (type != Notification.NotificationType.ACTIONED) {

            return;
        }
        Notification.NotificationState currentState = notification.getCurrentState();
        if (currentState != Notification.NotificationState.ASSIGNED || currentState != Notification.NotificationState.ACKED) {

            return;
        }
        notification.getConfiguration().getAction().setAssignee(assignee);
        State state = new State(Notification.NotificationState.ASSIGNED);
        state.getData().put("assignee", assignee);
        notification.getStates().add(state);
        notification.setCurrentState(state.getState());
    }


}

package tr.com.bosbeles.tur.notification.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import tr.com.bosbeles.tur.notification.model.Notification;
import tr.com.bosbeles.tur.notification.model.NotificationReport;
import tr.com.bosbeles.tur.notification.model.internal.Assignee;
import tr.com.bosbeles.tur.notification.model.internal.Configuration;
import tr.com.bosbeles.tur.notification.model.internal.State;
import tr.com.bosbeles.tur.notification.repository.NotificationReportRepository;
import tr.com.bosbeles.tur.notification.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class NotificationManager {

    @Autowired
    private ReactiveMongoOperations mongo;


    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationReportRepository notificationReportRepository;

    @Autowired
    private NotificationTemplateManager templateManager;


    public Mono<Notification> create(Notification notification) {
        return decorate(notification).flatMap(aVoid -> {
            notification.setCurrentState(Notification.NotificationState.CREATED);
            notification.getStates().add(new State(Notification.NotificationState.CREATED));

            notification.setId(null);
            return notificationRepository.save(notification);
        });
    }

    public Mono<Notification> update(Notification notification) {
        Function<? super Notification, ? extends Mono<? extends Notification>> mergeWithOldOne = old -> {
            // Merge notification with old one. Updating some fields may be forbidden.
            updateNotification(old, notification);
            return notificationRepository.save(notification);
        };
        return notificationRepository.findById(notification.getId()).flatMap(mergeWithOldOne);
    }

    private void updateNotification(Notification old, Notification notification) {
        // terminated ise update yaptirma

    }

    private Mono<Void> decorate(final Notification notification) {
        if (notification.getTemplate() == null) {
            return Mono.empty();
        }

        String templateId = notification.getTemplate().getId();
        return templateManager.get(templateId).onErrorReturn(null).flatMap(template -> {
            if (template != null) {
                Configuration configuration = template.getConfiguration();
                if (notification.getConfiguration() == null) {
                    notification.setConfiguration(configuration);
                } else {
                    notification.getConfiguration().merge(template.getConfiguration());
                }
                if (notification.getData() == null) {
                    notification.setData(new HashMap<>());
                }
                Map<String, Object> templateMap = template.getData();
                templateMap.forEach((k, v) -> {
                    notification.getData().putIfAbsent(k, v);
                });
            }
            return Mono.empty();
        });


    }

    public void read(String user, String notificationId) {
        notificationReportRepository.findNotificationReportByNotificationIdAndUserId(notificationId, user).flatMap(report -> {
            return null;
        });
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

    public void changeAssignee(String notificationId, Assignee assignee) {
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

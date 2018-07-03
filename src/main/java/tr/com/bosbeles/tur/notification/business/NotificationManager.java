package tr.com.bosbeles.tur.notification.business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tr.com.bosbeles.tur.notification.model.Notification;
import tr.com.bosbeles.tur.notification.model.internal.Assignee;
import tr.com.bosbeles.tur.notification.model.internal.Configuration;
import tr.com.bosbeles.tur.notification.model.internal.State;
import tr.com.bosbeles.tur.notification.repository.NotificationReportRepository;
import tr.com.bosbeles.tur.notification.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationManager {

    public static final Pattern CHANNEL_PATTERN = Pattern.compile("^\\w+(\\.\\w+)*$");

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationReportRepository notificationReportRepository;

    @Autowired
    private NotificationTemplateManager templateManager;

    @Autowired
    private SubscriptionManager subscriptionManager;

    @Autowired
    private ChangeEmitter changeEmitter;


    public Mono<Notification> find(String notificationId) {
        return notificationRepository.findById(notificationId);
    }

    public Flux<Notification> findAll(String[] channels, boolean recursive) {
        final Set<String> validChannels = Arrays.stream(channels).filter(c -> CHANNEL_PATTERN.matcher(c).matches()).collect(Collectors.toSet());
        if (validChannels.isEmpty()) {
            return Flux.empty();
        }
        //TODO ttl sorgusundan elenen notificationlar modified olmamis olacak.
        return notificationRepository.findByChannels(validChannels, LocalDateTime.now(), new Sort(Sort.Direction.DESC, "modifiedAt"), recursive);
    }


    public Mono<Notification> create(Notification notification) {
        notification = new Notification(notification);
        return decorate(notification).then(createAfterDecoration(notification));
    }

    private Mono<Notification> createAfterDecoration(Notification notification) {
        notification.fill();
        notification.setCurrentState(Notification.NotificationState.CREATED);
        notification.getStates().add(new State(Notification.NotificationState.CREATED));

        notification.setId(null);
        if (notification.getConfiguration().getTimeout() > 0) {
            notification.setExpireAt(LocalDateTime.now().plusSeconds(notification.getConfiguration().getTimeout()));
        }
        return notificationRepository.save(notification).doOnSuccess(this::emit);
    }

    public Mono<Notification> update(Notification notification) {
        Function<? super Notification, ? extends Mono<? extends Notification>> mergeWithOldOne = old -> {
            // Merge notification with old one. Updating some fields may be forbidden.
            if (updateNotification(old, notification)) {
                return notificationRepository.save(notification);
            } else {
                return Mono.error(new Exception("Notification couldn't updated probably because it is terminated or acked."));
            }

        };
        return notificationRepository.findById(notification.getId()).flatMap(mergeWithOldOne).doOnSuccess(this::emit);
    }


    public Mono<Void> read(String user, String notificationId) {
        return notificationReportRepository.read(user, notificationId).then();
        // if not exists then put

    }

    public Mono<Void> ack(String user, String notificationId) {

        // Ack yap
        // Başarılı ise count++
        // Notification cek
        // advance state
        // notification save
        // hata cikarsa duyur yine de emit yap.
        // emit

        return notificationRepository.findById(notificationId)
                .flatMap(notification -> {
                    Notification.NotificationType type = notification.getNotificationType();
                    if (type == Notification.NotificationType.SIMPLE) {
                        return Mono.error(new Exception("This type of notification is not acknowledgable."));
                    }
                    Notification.NotificationState currentState = notification.getCurrentState();
                    if (currentState != Notification.NotificationState.CREATED && currentState != Notification.NotificationState.UPDATED) {
                        return Mono.error(new Exception("Notification is already acknowledged or terminated."));
                    }
                    return notificationReportRepository.ack(user, notificationId).log("ack").flatMap(updateResult -> {
                        if (updateResult != null && updateResult.wasAcknowledged()) {
                            return notificationRepository.incrementAck(notification).then().doFinally(signal -> emit(notification));
                        } else {
                            return notificationRepository.checkAckCount(notification)
                                    .then()
                                    .doFinally(signal -> emit(notification))
                                    .onErrorResume(t -> Mono.error(new Exception("Notification is already acknowledged or terminated.")));
                        }
                    }).log("cak");
                });
    }

    public Mono<Void> cancel(String notificationId) {
        // Notification cek
        // advance state
        // terminated degilse cancel yap
        // notification save
        // hata cikarsa duyur yine de emit yap.
        // emit

        return notificationRepository.findById(notificationId)
                .flatMap(notification -> advanceToState(notification, Notification.NotificationState.CANCELLED))
                .then();
    }

    public Mono<Void> handle(String user, String notificationId) {

        // Notification cek
        // advance
        // terminated degilse handle yap.
        // save'le.
        // hata cikarsa duyur yine de emit yap.
        // emit.

        return notificationRepository.findById(notificationId)
                .flatMap(notification -> {
                    Notification.NotificationType type = notification.getNotificationType();
                    if (type != Notification.NotificationType.ACTIONED) {
                        return Mono.error(new Exception("Notification is not an actioned notification."));
                    }
                    return advanceToState(notification, Notification.NotificationState.HANDLED);
                }).then();
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


    public Flux<List<String>> subscribe(String user, String[] channels) {
        return subscriptionManager.subscribe(user, channels);
    }


    private void emit(Notification notification) {
        changeEmitter.emit(notification);
    }


    private void copy(Notification source, Notification destination) {
        //
    }

    private boolean updateNotification(Notification old, Notification notification) {
        notification = new Notification(notification);
        // terminated ise update yaptirma
        old.advanceState();
        if (old.isTerminated()) {
            return false;
        }
        Notification.NotificationState currentState = Notification.NotificationState.UPDATED;
        if (old.getCurrentState() == Notification.NotificationState.CREATED || old.getCurrentState() == currentState) {
            old.setCurrentState(currentState);
            old.getStates().add(new State(currentState));
        }

        notification.setId(old.getId());
        notification.setStates(old.getStates());
        notification.setCurrentState(old.getCurrentState());
        notification.setVersion(old.getVersion());
        decorate(notification).block();
        notification.fill();

        notification.setId(old.getId());
        if (notification.getConfiguration().getTimeout() > 0) {
            notification.setExpireAt(LocalDateTime.now().plusSeconds(notification.getConfiguration().getTimeout()));
        }
        return true;
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
        }).then();
    }


    private Mono<?> advanceToState(Notification notification, Notification.NotificationState state) {
        if (notification.isTerminated()) {
            // already terminated diye hata don.
            return Mono.error(new Exception("Notification is already terminated."));
        }
        notification.advanceState();
        if (notification.isTerminated()) {
            // do nothing
            // save.
            // already terminated diye hata don.
            // her turlu emit.
            return notificationRepository.save(notification)
                    .flatMap(n -> Mono.error(new Exception("Notification is already terminated.")))
                    .doFinally(signal -> emit(notification));
        }

        notification.setCurrentState(state);
        notification.getStates().add(new State(notification.getCurrentState()));
        notification.advanceState();
        return notificationRepository.save(notification)
                .doFinally(signal -> emit(notification));
    }

    public static void main(String[] args) {
        NotificationManager manager = new NotificationManager();
        Notification notification = new Notification();

        manager.decorate(notification).doFinally(signalType -> System.out.println("success1")).then(other(notification)).doOnSuccess(signalType -> System.out.println("success2")).block();


    }

    static Mono<Integer> other(Notification n) {
        System.out.println("Other");
        return Mono.just(7);
    }


}

package tr.com.bosbeles.tur.notification.business;

import com.google.common.collect.SetMultimap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tr.com.bosbeles.tur.notification.model.Notification;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ChangeEmitter {

    @Autowired
    private SubscriptionManager subscriptionManager;

    private Set<String> channelsToBeNotified;

    public ChangeEmitter() {
        channelsToBeNotified = ConcurrentHashMap.newKeySet();
    }


    public void onClusterNotified(Object message) {
        if (message instanceof Set) {
            channelsToBeNotified.addAll((Set<String>) message);
        }
    }

    public void emit(Notification notification) {
        String channel = notification.getConfiguration().getChannel();
        if (!StringUtils.isEmpty(channel)) {
            channelsToBeNotified.add(channel);
        }
    }

    private void notifyClusters(Object channelList) {
        // send to clusters
    }

    @Scheduled(fixedDelay = 500)
    private void emitter() {
        Instant now = Instant.now();
        Set<String> channels = new HashSet<>(channelsToBeNotified);
        channelsToBeNotified.removeAll(channels);
        notifyClusters(channels);
        emit(channels);
        long time = Duration.between(now, Instant.now()).toMillis();
        if (time > 1000) {
            log.info("Emit job took {} ms.", time);
        }

    }

    private void emit(Set<String> channels) {

        Set<String> allPossibleChannels = getAllPossibleChannels(channels);
        Set<Subscription> allEmitters = new HashSet<>();
        SetMultimap<String, Subscription> subscriptions = subscriptionManager.getSubscriptions();
        synchronized (subscriptions) {
            for (String channelPart : allPossibleChannels) {
                log.info("Querying channel: {}", channelPart);
                Set<Subscription> subscriptionsToChannel = subscriptions.get(channelPart);
                allEmitters.addAll(subscriptionsToChannel);
            }
        }

        for (Subscription subscription : allEmitters) {
            try {
                List<String> message = channels.stream().filter(subscription::belongsTo).collect(Collectors.toList());
                subscription.getEmitter().getProcessor().onNext(message);
            } catch (Exception e) {
                log.trace("SSE peer disconnected.", e);
                subscription.close();
            }
        }
    }

    private Set<String> getAllPossibleChannels(Set<String> channels) {
        // All possible channel subscriptions to be notified.
        // e.g. {com.milsoft.turkuaz, com.milsoft.otc} will produce "{com, com.milsoft, com.milsoft.turkuaz, com.milsoft.otc}"
        return channels.stream().collect(HashSet::new, (set, channel) -> {
            List<Integer> positions = new ArrayList<>();
            for (int i = 0; i < channel.length(); i++) {
                if (channel.charAt(i) == '.') {
                    positions.add(i);
                }
            }
            positions.add(channel.length());
            for (Integer position : positions) {
                set.add(channel.substring(0, position));
            }
        }, Collection::addAll);
    }


}

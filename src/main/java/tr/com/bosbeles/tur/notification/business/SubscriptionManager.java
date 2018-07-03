package tr.com.bosbeles.tur.notification.business;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import tr.com.bosbeles.tur.notification.util.ReactiveSse;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SubscriptionManager {

    private SetMultimap<String, Subscription> subscriptions;


    public SubscriptionManager() {
        subscriptions = Multimaps.synchronizedSetMultimap(MultimapBuilder.hashKeys().hashSetValues().build());

    }


    public Flux<List<String>> subscribe(String user, String[] channels) {
        ReactiveSse<List<String>> reactiveSse = new ReactiveSse<>(user);

        final Set<String> validChannels = Arrays.stream(channels).filter(c -> NotificationManager.CHANNEL_PATTERN.matcher(c).matches()).collect(Collectors.toSet());
        final Subscription subscription = new Subscription(reactiveSse, validChannels);
        validChannels.stream().forEach(c -> subscriptions.put(c, subscription));
        subscription.onClose(() -> {
            log.info("On termination of user: {} with {} ", user, subscription);
            subscription.getChannels().stream().forEach(c -> subscriptions.remove(c, subscription));
        });

        return reactiveSse.getFlux().doOnCancel(()->{
            subscription.close();
        });
    }

    public SetMultimap<String, Subscription> getSubscriptions() {
        return subscriptions;
    }
}

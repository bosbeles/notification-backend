package tr.com.bosbeles.tur.notification.business;

import lombok.Getter;
import tr.com.bosbeles.tur.notification.util.ReactiveSse;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Subscription {

    @Getter
    private ReactiveSse<List<String>> emitter;

    @Getter
    private Set<String> channels;

    private Runnable onCloseCallback;

    public Subscription(ReactiveSse<List<String>> emitter, Set<String> channels) {
        this.emitter = emitter;
        this.channels = Collections.unmodifiableSet(channels);

    }

    public void onClose(Runnable callback) {
        this.onCloseCallback = callback;
        emitter.getProcessor()
                .doOnError(throwable ->{
                    System.out.println("DoOnError");
                    close();})
                .doOnComplete(onCloseCallback).doOnCancel(this::close);
    }

    public void close() {
        System.out.println("Closing...");
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
        emitter.getProcessor().onComplete();
    }

    public boolean belongsTo(String channel) {
        for (String regex : channels) {
            if (channel.equals(regex) || channel.startsWith(regex + ".")) {
                return true;
            }
        }
        return false;
    }

}

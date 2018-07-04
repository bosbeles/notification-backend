package tr.com.bosbeles.tur.notification.util;

import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;

import java.time.Instant;


public class ReactiveSse<T> {


    private final String user;
    private final Instant creationTime;
    private DirectProcessor<T> processor;
    private Flux<T> flux;


    public ReactiveSse(String user) {
        this.user = user;
        this.creationTime = Instant.now();
        processor = DirectProcessor.create();
        flux = processor;
    }

    public DirectProcessor<T> getProcessor() {
        return processor;
    }

    public Flux<T> getFlux() {
        return flux;
    }

    @Override
    public String toString() {
        return "ReactiveSse{" +
                "user='" + user + '\'' +
                ", creationTime=" + creationTime +
                ", processor=" + processor +
                ", flux=" + flux +
                '}';
    }

    public static void main(String[] args) {
        ReactiveSse<String> sse = new ReactiveSse<>("");

        sse.getFlux().subscribe(s -> System.out.println("::" + s));

        sse.getProcessor().onNext("7");
        sse.getProcessor().onNext("8");
        sse.getProcessor().onError(new Exception(""));
        sse.getProcessor().onNext("9");
    }


}

package tr.com.bosbeles.tur.notification.util;

import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class ReactiveSse<T> {


    private DirectProcessor<T> processor;

    private Flux<T> flux;


    public ReactiveSse() {
        processor = DirectProcessor.create();
        flux = processor.subscribeOn(Schedulers.immediate());
    }

    public DirectProcessor<T> getProcessor() {
        return processor;
    }

    public Flux<T> getFlux() {
        return flux;
    }

    public static void main(String[] args) {
        ReactiveSse<String> sse = new ReactiveSse<>();

        sse.getFlux().subscribe(s -> System.out.println("::" + s));

        sse.getProcessor().onNext("7");
        sse.getProcessor().onNext("8");
        sse.getProcessor().onError(new Exception(""));
        sse.getProcessor().onNext("9");
    }


}

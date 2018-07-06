package tr.com.bosbeles.tur.notification.test;

import org.springframework.util.StopWatch;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tr.com.bosbeles.tur.notification.model.Notification;
import tr.com.bosbeles.tur.notification.model.internal.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainTest {

    public static void main(String[] args) throws InterruptedException {
        WebClient client = WebClient.create("http://localhost:8080/notifications");
        Notification notification = createNotification();

        StopWatch watch = new StopWatch();
        watch.start();


        List<Mono<Notification>> monos = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            System.out.println("......" + i + "......");
            Mono<Notification> mono = client.post()
                    .body(Mono.just(notification), Notification.class)
                    .retrieve()
                    .bodyToMono(Notification.class);
            //mono.block();
            monos.add(mono);
        }
        Double time = Mono.zip(monos, arr -> {
            watch.stop();
            return watch.getTotalTimeSeconds();
        }).block();
        System.out.println(time);
        if (watch.isRunning()) {
            watch.stop();
        }

        System.out.println(watch.getTotalTimeSeconds());
    }

    private static Notification createNotification() {
        Notification notification = new Notification();
        Map<String, Object> map = new HashMap<>();
        map.put("category", "cat-1");
        notification.setData(map);
        Configuration configuration = new Configuration();
        configuration.fill();
        configuration.getAcknowledgement().setRequired(1);
        configuration.setChannel("deneme");
        notification.setConfiguration(configuration);
        return notification;
    }
}

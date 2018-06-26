package tr.com.bosbeles.tur.notification.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import tr.com.bosbeles.tur.notification.model.Notification;
import tr.com.bosbeles.tur.notification.repository.NotificationRepository;

import java.time.Duration;

@RestController
public class NotificationController {

    @Autowired
    private NotificationRepository repository;


    @GetMapping("/sse/string")
    Flux<String> string() {
        Notification notification = new Notification();

        return Flux
                .interval(Duration.ofSeconds(1))
                .map(l -> "foo " + l);

    }
}

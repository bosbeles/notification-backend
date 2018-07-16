package tr.com.bosbeles.tur.notification.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tr.com.bosbeles.tur.notification.business.NotificationManager;
import tr.com.bosbeles.tur.notification.model.Notification;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("notifications")
public class NotificationController {

    @Autowired
    private NotificationManager notificationManager;


    @PostMapping()
    public Mono<Notification> create(@RequestBody Notification notification) {
        return notificationManager.create(notification);
    }

    @GetMapping("{id}")
    public Mono<Notification> get(@PathVariable String id) {


        return notificationManager.find(id);
    }


    @PostMapping("{id}")
    public Mono<Notification> update(@PathVariable String id, @RequestBody Notification notification) {
        notification.setId(id);
        return notificationManager.update(notification);
    }

    @PostMapping("{id}/cancel")
    public Mono<Void> cancel(@PathVariable String id) {
        return notificationManager.cancel(id);
    }

    @PostMapping("{id}/read")
    public Mono<Void> read(@PathVariable String id, @RequestParam(name = "ApiKey") final String apiKey) {
        return notificationManager.read(apiKey, id);
    }

    @PostMapping("{id}/ack")
    public Mono<Void> ack(@PathVariable String id, @RequestParam(name = "ApiKey") final String apiKey) {
        return notificationManager.ack(apiKey, id);
    }

    @PostMapping("{id}/handle")
    public Mono<Void> handle(@PathVariable String id, @RequestParam(name = "ApiKey") final String apiKey) {
        return notificationManager.handle(apiKey, id);
    }


    //TODO Channel listesi icin tek query calistirilabilir ya da
    //ayri query'lerin sonuclari birlestirilebilir (ya da kesişmeyen query'lerin sonuclari birlestirilir)
    @GetMapping
    public Mono<Map<String, List<Notification>>> getNotifications(@RequestParam("channel") String[] channels, @RequestParam(required = false) boolean recursive, @RequestParam(required = false) String where, @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "1000") Integer size, Sort sort) {
        System.out.println("sort:" + sort);
        System.out.println("where: " + where);
        System.out.println("page: " + page);
        System.out.println("size: " + size);
        Flux<Notification> notifications = notificationManager.findAll(channels, recursive, where, PageRequest.of(page, size), sort);
        notifications.subscribe(n -> System.out.println(n));
        Mono<Map<String, List<Notification>>> groupedByChannel = notifications.collect(Collectors.groupingBy(n -> n.getConfiguration().getChannel()));
        return groupedByChannel;
    }


    @GetMapping(value = "/subscribe", produces = "text/event-stream")
    public Flux<List<String>> subscribe(@RequestParam(name = "ApiKey") final String apiKey, final @RequestParam(name = "channel") String[] channels) {
        log.error("Connected: {}", apiKey);
        List<String> list = Collections.emptyList();
        // list.add("xxx");
        return notificationManager.subscribe(apiKey, channels)
                .startWith(list)
                .log("category");

    }

}

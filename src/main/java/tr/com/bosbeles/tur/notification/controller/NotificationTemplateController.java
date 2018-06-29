package tr.com.bosbeles.tur.notification.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tr.com.bosbeles.tur.notification.business.NotificationTemplateManager;
import tr.com.bosbeles.tur.notification.model.NotificationTemplate;

@RestController
@RequestMapping("/templates")
public class NotificationTemplateController {

    @Autowired
    private NotificationTemplateManager templateManager;

    @GetMapping
    public Flux<NotificationTemplate> list() {
        return templateManager.list();
    }

    @GetMapping("{templateId}")
    public Mono<NotificationTemplate> get(@PathVariable String templateId) {
        return templateManager.get(templateId);
    }


    @PostMapping
    public Mono<NotificationTemplate> create(NotificationTemplate template) {
        return templateManager.create(template);
    }

    @PostMapping("{templateId}")
    public Mono<NotificationTemplate> update(NotificationTemplate template) {
        return templateManager.update(template);
    }


}

package tr.com.bosbeles.tur.notification.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tr.com.bosbeles.tur.notification.model.NotificationTemplate;
import tr.com.bosbeles.tur.notification.repository.NotificationTemplateRepository;

import java.util.function.Function;

@Service
public class NotificationTemplateManager {

    @Autowired
    private NotificationTemplateRepository templateRepository;

    public Mono<NotificationTemplate> create(NotificationTemplate template) {
        // Creates or updates a notification template.
        template.setId(null);
        return templateRepository.save(template);
    }

    public Mono<NotificationTemplate> get(String templateId) {
        return templateRepository.findById(templateId).doOnSuccess(template -> {
            // Censor some fields.
        });
    }

    public Flux<NotificationTemplate> list() {
        return templateRepository.findAll();
    }

    public Mono<NotificationTemplate> update(final NotificationTemplate template) {
        Function<NotificationTemplate, Mono<? extends NotificationTemplate>> mergeWithOldOne = nt -> {
            // Merge template and nt according to the rules
            return templateRepository.save(template);
        };

        return templateRepository.findById(template.getId()).flatMap(mergeWithOldOne);

    }

    public Mono<Void> delete(String id) {
        return templateRepository.deleteById(id);
    }
}

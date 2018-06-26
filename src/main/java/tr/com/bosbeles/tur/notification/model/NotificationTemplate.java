package tr.com.bosbeles.tur.notification.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import tr.com.bosbeles.tur.notification.model.internal.Configuration;

import java.util.Map;

@Data
@EqualsAndHashCode(of = {"id"})
@Document(collection = "templates")
public class NotificationTemplate {

    @Id
    private String id;

    private Configuration configuration;
    private Map<String, Object> data;
}

package tr.com.bosbeles.tur.notification.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;

import java.util.Map;

@Data
@EqualsAndHashCode(of = {"id"})
public class NotificationTemplate {

    @Id
    private String id;

    private String channel;

    private int ackRequired;

    private int actionRequired;

    private int timeout;

    private Map<String, Object> data;
}

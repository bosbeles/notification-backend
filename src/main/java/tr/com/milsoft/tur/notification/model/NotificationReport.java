package tr.com.milsoft.tur.notification.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of = {"alertId", "user"})
public class NotificationReport {

    private String alertId;
    private String user;

    private LocalDateTime read;
    private LocalDateTime ack;

}

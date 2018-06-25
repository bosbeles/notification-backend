package tr.com.bosbeles.tur.notification.model.internal;

import lombok.Data;
import lombok.NoArgsConstructor;
import tr.com.bosbeles.tur.notification.model.Notification;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
public class State {
    private Notification.NotificationState state;
    private LocalDateTime date;
    private Map<String, Object> data;

    public State(Notification.NotificationState state) {
        this.state = state;
        date = LocalDateTime.now();
    }
}

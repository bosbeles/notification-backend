package tr.com.bosbeles.tur.notification.model.internal;

import lombok.Data;
import lombok.NoArgsConstructor;
import tr.com.bosbeles.tur.notification.model.Notification;

import java.util.Date;
import java.util.Map;

@Data
@NoArgsConstructor
public class State {
    private Notification.NotificationState state;
    private Date date;
    private Map<String, Object> data;

    public State(Notification.NotificationState state) {
        this.state = state;
        date = new Date();
    }
}

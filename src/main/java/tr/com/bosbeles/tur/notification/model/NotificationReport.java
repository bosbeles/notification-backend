package tr.com.bosbeles.tur.notification.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of = {"notificationId", "userId"})
@CompoundIndexes({
        @CompoundIndex(name = "n_u",
                unique = true,
                def = "{'notificationId' : 1, 'userId' : 1}")
})
public class NotificationReport {

    private String notificationId;
    private String userId;

    private LocalDateTime read;
    private LocalDateTime ack;

}

package tr.com.bosbeles.tur.notification.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@EqualsAndHashCode(of = {"notificationId", "userId"})
@CompoundIndexes({
        @CompoundIndex(name = "n_u",
                unique = true,
                def = "{'notificationId' : 1, 'userId' : 1}")
})
@Document(collection = "notificationReports ")
public class NotificationReport {

    private String notificationId;
    private String userId;

    private Instant read;
    private Instant ack;

}

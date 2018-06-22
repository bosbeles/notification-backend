package tr.com.bosbeles.tur.notification.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;
import java.util.Map;

@Data
@EqualsAndHashCode(of = {"id"})
public class Notification {

    public static final int CREATED = 0;
    public static final int UPDATED = 1;
    public static final int ACKED = 2;
    public static final int HANDLED = 3;
    public static final int CANCELLED = 4;
    public static final int EXPIRED = 5;

    @Id
    private String id;

    private String channel;

    private int ackRequired;

    private int actionRequired;

    private int ackCount;

    private int timeout;

    private Map<String, Object> data;

    private Date created;

    private Date expired;

    @LastModifiedDate
    private Date modified;

    private int state;

}
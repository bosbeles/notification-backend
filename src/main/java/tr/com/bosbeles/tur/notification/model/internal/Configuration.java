package tr.com.bosbeles.tur.notification.model.internal;


import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class Configuration {
    private String channel;
    private Acknowledgement acknowledgement;
    private Action action;
    private int timeout = -1;


    public void merge(Configuration template) {
        if(StringUtils.isEmpty(channel)) {
            channel = template.getChannel();
        }
        if(acknowledgement == null) {
            acknowledgement = template.getAcknowledgement();
        }
        if(action == null) {
            action = template.getAction();
        }
        if(timeout < 0) {
            timeout = template.getTimeout();
        }
    }

}

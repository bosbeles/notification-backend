package tr.com.bosbeles.tur.notification.model.internal;

import lombok.Data;

import java.util.List;

@Data
public class Acknowledgement {
    private int required;
    private int count;
    private List<String> allowed;

}

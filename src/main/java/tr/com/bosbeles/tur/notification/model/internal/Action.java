package tr.com.bosbeles.tur.notification.model.internal;

import lombok.Data;

import java.util.List;


@Data
public class Action {
    private int required;
    private List<String> allowed;
    private Assignee assignee;
}

package tr.com.bosbeles.tur.notification.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;

@Service
public class NotificationManager {

    @Autowired
    private MongoOperations mongo;


    public void read(String user, String notificationId){

    }

    public void ack(String user, String notificationId) {
        // Upsert
        // Old da ack yoksa
        // Ack yi bir arttir
        // Ack > 0 && Ack == AckCount ise set state to ack if state not timeout, handled, cancelled
        // emit channel
    }

    public void handle(String user, String notificationId) {
        // if acknowledged,
    }


}

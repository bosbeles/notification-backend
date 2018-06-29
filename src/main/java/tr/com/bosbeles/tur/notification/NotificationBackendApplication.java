package tr.com.bosbeles.tur.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableReactiveMongoRepositories
@EnableMongoAuditing
@EnableScheduling
@EnableAutoConfiguration
@ComponentScan
public class NotificationBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationBackendApplication.class, args);
    }
}

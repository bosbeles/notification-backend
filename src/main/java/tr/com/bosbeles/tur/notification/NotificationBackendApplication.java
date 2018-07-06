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
        final SpringApplication application = new SpringApplication(NotificationBackendApplication.class);
        // Swagger works only in servlet mode. see https://github.com/springfox/springfox/issues/1773

        // application.setWebApplicationType(WebApplicationType.);
        application.run(args);
    }
}

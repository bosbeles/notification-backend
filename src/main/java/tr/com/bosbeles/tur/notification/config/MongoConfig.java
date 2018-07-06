package tr.com.bosbeles.tur.notification.config;

import com.mongodb.MongoClientOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoClientFactoryBean;

@Configuration
public class MongoConfig {


    /*
     * Factory bean that creates the com.mongodb.reactivestreams.client.MongoClient instance
     */
    public @Bean
    ReactiveMongoClientFactoryBean mongoClient() {

        ReactiveMongoClientFactoryBean clientFactory = new ReactiveMongoClientFactoryBean();


        clientFactory.setHost("localhost");

        return clientFactory;
    }

    @Bean
    public MongoClientOptions mongoOptions() {

        return MongoClientOptions.builder().connectionsPerHost(1000).threadsAllowedToBlockForConnectionMultiplier(10).build();
    }
}

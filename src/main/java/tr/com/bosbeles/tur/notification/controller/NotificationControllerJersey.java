package tr.com.bosbeles.tur.notification.controller;

import reactor.core.publisher.Flux;
import tr.com.bosbeles.tur.notification.business.NotificationManager;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import java.time.Duration;
import java.util.stream.Stream;

@Path("/api")
public class NotificationControllerJersey {

    @Inject
    private NotificationManager notificationManager;

    static int i;


    @GET
    @Path("/notification")
    @Produces(MediaType.APPLICATION_JSON)
    public void all(@Suspended final AsyncResponse async) {
        async.resume("deneme");
    }


    @GET
    @Path("/notifications")
    @Produces("text/event-stream")
    public void allStockExchange(@Suspended final AsyncResponse async) {
        Flux.fromStream(Stream.generate(() -> i++)).delayElements(Duration.ofSeconds(1)).subscribe(async::resume, async::resume);
    }


}

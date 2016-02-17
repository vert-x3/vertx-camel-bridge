package io.vertx.ext.camel.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.stomp.StompClient;
import io.vertx.ext.stomp.StompClientConnection;
import org.apache.camel.main.Main;
import org.apache.camel.*;

import org.apache.camel.spi.EndpointRegistry;

public class MainVerticle extends AbstractVerticle {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(StompVerticle.class.getName());
    vertx.deployVerticle(MainVerticle.class.getName());
  }

  @Override
  public void start() {
    Main main = new Main();
    CamelContext context = main.getOrCreateCamelContext();

    try {
//      context.addRoutes(new RouteBuilder() {
//        @Override
//        public void configure() throws Exception {
//          from("vertx:data")
//              .to("stream:out");
//        }
//      });


      Component stream = context.getComponent("stream");
      Component stomp = context.getComponent("stomp");
      context.addEndpoint("stream:out", stream.createEndpoint("stream:out"));
      Endpoint endpoint = stomp.createEndpoint("stomp:queue:test");
      context.addEndpoint("stomp:queue:test", endpoint);


//      context.addRoutes(new RouteBuilder() {
//        @Override
//        public void configure() throws Exception {
//          from("stomp:queue:test").to("stream:error");
//        }
//      });

      EndpointRegistry<String> registry = context.getEndpointRegistry();
      for (Object k : registry.keySet()) {
        Endpoint ep = registry.get(k);
        System.out.println("Endpoint : " + k + " - " + ep);

        if (ep.toString().contains("stream")) {
          vertx.eventBus().consumer(k.toString(), message -> {
            ProducerTemplate template = context.createProducerTemplate();
            template.sendBodyAndHeaders(ep, message.body(), MultiMapHelper.toMap(message.headers()));
          });
        } else {
          Consumer consumer = ep.createConsumer(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
              Message in = exchange.getIn();
              System.out.println(in);
              String uri = exchange.getFromEndpoint().getEndpointUri();
              vertx.eventBus().send(uri, in.getBody());
            }
          });
          System.out.println("Consumer : " + consumer);
          consumer.start();
        }
      }

//      vertx.eventBus().consumer("data", message -> {
//        ProducerTemplate template = context.createProducerTemplate();
//        template.sendBody(endpoint, message.body());
//      });

      context.start();

    } catch (Exception e) {
      e.printStackTrace();
    }

    vertx.createHttpServer()
        .requestHandler(req -> req.response().end("Hello World!"))
        .listen(8080);

    StompClient.create(vertx).connect(ar -> {
      StompClientConnection result = ar.result();
      System.out.println("connection to stomp server : " + result);
      vertx.setPeriodic(5000, l -> {
        System.out.println("Sending stomp frame...");
        result.send("/queue/test", Buffer.buffer("hello from stomp"));
      });
    });

    vertx.setPeriodic(1000, l -> {
      vertx.eventBus().send("stream://out", "hello");
      vertx.eventBus().send("stream://error", "oh no");
    });
  }
}
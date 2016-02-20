package examples;

import io.vertx.core.Vertx;
import io.vertx.ext.camel.CamelBridge;
import io.vertx.ext.camel.CamelBridgeOptions;
import io.vertx.ext.camel.InboundMapping;
import io.vertx.ext.camel.OutboundMapping;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Examples {

  public void example1(Vertx vertx) {
    CamelContext camel = new DefaultCamelContext();
    CamelBridge.create(vertx,
        new CamelBridgeOptions(camel)
            .addInboundMapping(new InboundMapping().setUri("direct:stuff").setAddress("eventbus-address"))
            .addOutboundMapping(new OutboundMapping().setAddress("eventbus-address").setUri("stream:out"))
    ).start();
  }

  public void example2(Vertx vertx, CamelContext camel) {
    Endpoint endpoint = camel.getEndpoint("direct:foo");

    CamelBridge.create(vertx,
        new CamelBridgeOptions(camel)
            .addInboundMapping(new InboundMapping().setUri("direct:stuff").setAddress("eventbus-address"))
            .addInboundMapping(new InboundMapping().setEndpoint(endpoint).setAddress("eventbus-address"))
            .addInboundMapping(new InboundMapping().setEndpoint(endpoint).setAddress("eventbus-address")
                .setHeadersCopy(false))
            .addInboundMapping(new InboundMapping().setEndpoint(endpoint).setAddress("eventbus-address")
                .setPublish(true))
            .addInboundMapping(new InboundMapping().setEndpoint(endpoint).setAddress("eventbus-address")
                .setBodyType(String.class))
    );
  }

  public void example3(Vertx vertx, CamelContext camel) {
    Endpoint endpoint = camel.getEndpoint("stream:out");

    CamelBridge.create(vertx,
        new CamelBridgeOptions(camel)
            .addOutboundMapping(new OutboundMapping().setAddress("eventbus-address").setUri("stream:out"))
            .addOutboundMapping(new OutboundMapping().setAddress("eventbus-address").setEndpoint(endpoint))
            .addOutboundMapping(new OutboundMapping().setAddress("eventbus-address").setEndpoint(endpoint)
                .setHeadersCopy(false))
            .addOutboundMapping(new OutboundMapping().setAddress("eventbus-address").setEndpoint(endpoint))
    );
  }

  public void example4(Vertx vertx, CamelContext camel) throws Exception {
    camel.addRoutes(new RouteBuilder() {
      @Override
      public void configure() throws Exception {
        from("direct:start")
            .transform(constant("OK"));
      }
    });

    CamelBridge bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
        .addOutboundMapping(new OutboundMapping().setAddress("test").setUri("direct:start")));

    camel.start();
    bridge.start();


    vertx.eventBus().send("test", "hello", reply -> {
      // Reply from the route (here it will be "OK"
    });
  }

  public void example5(Vertx vertx, CamelContext camel) throws Exception {
    camel.addRoutes(new RouteBuilder() {
      @Override
      public void configure() throws Exception {
        from("direct:my-route")
            .to("http://localhost:8080");
      }
    });

    CamelBridge bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
        .addOutboundMapping(new OutboundMapping().setAddress("camel-route").setUri("direct:my-route")));

    camel.start();
    bridge.start();

    vertx.eventBus().send("camel-route", "hello", reply -> {
      if (reply.succeeded()) {
        Object theResponse = reply.result().body();
      } else {
        Throwable theCause = reply.cause();
      }
    });
  }
}

package examples;

import io.vertx.core.Vertx;
import io.vertx.ext.camel.CamelBridge;
import io.vertx.ext.camel.CamelBridgeOptions;
import io.vertx.ext.camel.InboundMapping;
import io.vertx.ext.camel.OutboundMapping;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import java.util.concurrent.Future;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Examples {

  public void example1(Vertx vertx) {
    CamelContext camel = new DefaultCamelContext();
    CamelBridge.create(vertx,
        new CamelBridgeOptions(camel)
            .addInboundMapping(InboundMapping.fromCamel("direct:stuff").toVertx("eventbus-address"))
            .addOutboundMapping(OutboundMapping.fromVertx("eventbus-address").toCamel("stream:out"))
    ).start();
  }

  public void example2(Vertx vertx, CamelContext camel) {
    Endpoint endpoint = camel.getEndpoint("direct:foo");

    CamelBridge.create(vertx,
        new CamelBridgeOptions(camel)
            .addInboundMapping(InboundMapping.fromCamel("direct:stuff").toVertx("eventbus-address"))
            .addInboundMapping(InboundMapping.fromCamel(endpoint).toVertx("eventbus-address"))
            .addInboundMapping(InboundMapping.fromCamel(endpoint).toVertx("eventbus-address")
                .withoutHeadersCopy())
            .addInboundMapping(InboundMapping.fromCamel(endpoint).toVertx("eventbus-address")
                .usePublish())
            .addInboundMapping(InboundMapping.fromCamel(endpoint).toVertx("eventbus-address")
                .withBodyType(String.class))
    );
  }

  public void example3(Vertx vertx, CamelContext camel) {
    Endpoint endpoint = camel.getEndpoint("stream:out");

    CamelBridge.create(vertx,
        new CamelBridgeOptions(camel)
            .addOutboundMapping(OutboundMapping.fromVertx("eventbus-address").toCamel("stream:out"))
            .addOutboundMapping(OutboundMapping.fromVertx("eventbus-address").toCamel(endpoint))
            .addOutboundMapping(OutboundMapping.fromVertx("eventbus-address").toCamel(endpoint)
                .withoutHeadersCopy())
            .addOutboundMapping(OutboundMapping.fromVertx("eventbus-address").toCamel(endpoint))
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
        .addOutboundMapping(OutboundMapping.fromVertx("test").toCamel("direct:start")));

    camel.start();
    bridge.start();


    vertx.eventBus().send("test", "hello", reply -> {
      // Reply from the route (here it's "OK")
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
        .addOutboundMapping(OutboundMapping.fromVertx("camel-route").toCamel("direct:my-route")));

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

  public void example6(Vertx vertx, CamelContext camel) throws Exception {
    Endpoint endpoint = camel.getEndpoint("direct:stuff");

    CamelBridge bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
        .addInboundMapping(new InboundMapping().setAddress("test-reply").setEndpoint(endpoint)));

    vertx.eventBus().consumer("with-reply", message -> {
      message.reply("How are you ?");
    });

    camel.start();
    bridge.start();

    ProducerTemplate template = camel.createProducerTemplate();
    Future<Object> future = template.asyncRequestBody(endpoint, "hello");
    String response = template.extractFutureBody(future, String.class);
    // response == How are you ?
  }
}

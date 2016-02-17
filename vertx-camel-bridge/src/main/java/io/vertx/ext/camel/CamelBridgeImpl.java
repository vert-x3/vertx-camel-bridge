package io.vertx.ext.camel;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import org.apache.camel.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class CamelBridgeImpl implements CamelBridge {

  private final Vertx vertx;
  private final CamelBridgeOptions options;
  private final CamelContext camel;
  private final List<Consumer> camelConsumers = new ArrayList<>();

  public CamelBridgeImpl(Vertx vertx, CamelBridgeOptions options) {
    Objects.requireNonNull(vertx);
    Objects.requireNonNull(options);
    this.vertx = vertx;
    this.options = options;
    this.camel = options.getCamelContext();
    Objects.requireNonNull(camel);

    for (InboundMapping inbound : options.getInboundMappings()) {
      // camel -> vert.x
      Endpoint endpoint = validate(inbound);

      try {
        camelConsumers.add(endpoint.createConsumer(exchange -> {
          Message in = exchange.getIn();
          Object body = in.getBody();
          DeliveryOptions delivery = new DeliveryOptions();
          if (inbound.isHeadersCopy()  && in.hasHeaders()) {
            in.getHeaders().entrySet().stream().forEach(entry ->
                delivery.addHeader(entry.getKey(), entry.getValue().toString()));
          }

          if (inbound.isPublish()) {
            vertx.eventBus().publish(inbound.getAddress(), body, delivery);
          } else {
            vertx.eventBus().send(inbound.getAddress(), body, delivery);
          }

        }));
      } catch (Exception e) {
        throw new IllegalStateException("The endpoint " + inbound.getUri() + " does not support consumers");
      }
    }

    for (OutboundMapping outbound : options.getOutboundMappings()) {
      // vert.x -> camel
      Endpoint endpoint = validate(outbound);
      vertx.eventBus().consumer(outbound.getAddress(), message -> {
           ProducerTemplate template = camel.createProducerTemplate();
           template.asyncSend(endpoint.getEndpointUri(), exchange -> {
             Message out = exchange.getOut();
             out.setBody(message.body());
             if (outbound.isCopyHeaders()) {
               out.setHeaders(MultiMapHelper.toMap(message.headers()));
             }
           });
      });
    }
  }

  private Endpoint validate(OutboundMapping mapping) {
    Objects.requireNonNull(mapping.getAddress(), "The vert.x event bus address must not be `null`");
    Objects.requireNonNull(mapping.getUri(), "The endpoint uri must not be `null`");
    Endpoint endpoint = camel.getEndpoint(mapping.getUri());
    Objects.requireNonNull(endpoint, "Cannot find the endpoint " + mapping.getUri() + " in the camel context");
    return endpoint;
  }

  private Endpoint validate(InboundMapping mapping) {
    Objects.requireNonNull(mapping.getAddress(), "The vert.x event bus address must not be `null`");
    Objects.requireNonNull(mapping.getUri(), "The endpoint uri must not be `null`");
    Endpoint endpoint = camel.getEndpoint(mapping.getUri());
    Objects.requireNonNull(endpoint, "Cannot find the endpoint " + mapping.getUri() + " in the camel context");
    return endpoint;
  }

  public void start() {
    camelConsumers.stream().forEach(c -> {
      try {
        c.start();
      } catch (Exception e) {
        throw new IllegalStateException("Cannot start consumer", e);
      }
    });
  }

  public void stop() {
    camelConsumers.stream().forEach(c -> {
      try {
        c.stop();
      } catch (Exception e) {
        throw new IllegalStateException("Cannot stop consumer", e);
      }
    });
  }


}

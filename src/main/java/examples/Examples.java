/*
 *  Copyright (c) 2011-2015 The original author or authors
 *  ------------------------------------------------------
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *       The Eclipse Public License is available at
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *       The Apache License v2.0 is available at
 *       http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package examples;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.camel.CamelBridge;
import io.vertx.camel.CamelBridgeOptions;
import io.vertx.camel.InboundMapping;
import io.vertx.camel.OutboundMapping;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import java.util.concurrent.Future;

/**
 * Code snippets used in the documentation
 *
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


    vertx.eventBus().request("test", "hello", reply -> {
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

    vertx.eventBus().request("camel-route", "hello", reply -> {
      if (reply.succeeded()) {
        Object theResponse = reply.result().body();
      } else {
        Throwable theCause = reply.cause();
      }
    });
  }

  public void example51(Vertx vertx, CamelContext camel) throws Exception {
    camel.addRoutes(new RouteBuilder() {
      @Override
      public void configure() throws Exception {
        from("direct:my-route")
          .process(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
              // Do something blocking...
            }
          })
          .to("http://localhost:8080");
      }
    });

    CamelBridge bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
      .addOutboundMapping(OutboundMapping.fromVertx("camel-route").toCamel("direct:my-route").setBlocking(true)));

    camel.start();
    bridge.start();

    vertx.eventBus().request("camel-route", "hello", reply -> {
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

  public void registerCodec(Vertx vertx, MessageCodec<Person, Person> codec) {
    vertx.eventBus().registerDefaultCodec(Person.class, codec);
  }

  private class Person {

  }
}

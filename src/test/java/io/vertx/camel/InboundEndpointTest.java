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
package io.vertx.camel;

import com.jayway.awaitility.Duration;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.stomp.StompClient;
import io.vertx.ext.stomp.StompServer;
import io.vertx.ext.stomp.StompServerHandler;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spi.Synchronization;
import org.apache.camel.support.SynchronizationAdapter;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.jayway.awaitility.Awaitility.await;
import static io.vertx.camel.InboundMapping.fromCamel;

/**
 * Tests that Camel exchanges are propagated to the event bus
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class InboundEndpointTest {

  private static final Duration DEFAULT_TIMEOUT = Duration.TEN_SECONDS;

  private Vertx vertx;
  private DefaultCamelContext camel;

  private StompServer stomp;
  private CamelBridge bridge;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    camel = new DefaultCamelContext();
  }

  @After
  public void tearDown(TestContext tc) throws Exception {
    if (bridge != null) {
      BridgeHelper.stopBlocking(bridge);
    }
    if (camel != null) {
      camel.stop();
    }

    if (stomp != null) {
      stomp.close(null);
    }
    vertx.close(tc.asyncAssertSuccess());
  }

  @Test
  public void testWithDirectEndpoint(TestContext context) throws Exception {
    Async async = context.async();
    Endpoint endpoint = camel.getEndpoint("direct:foo");

    bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
        .addInboundMapping(fromCamel("direct:foo").toVertx("test")));

    vertx.eventBus().consumer("test", message -> {
      context.assertEquals("hello", message.body());
      async.complete();
    });

    camel.start();
    BridgeHelper.startBlocking(bridge);

    ProducerTemplate producer = camel.createProducerTemplate();
    producer.asyncSendBody(endpoint, "hello");
  }

  @Test
  public void testWithDirectEndpointAndCustomType(TestContext context) throws Exception {
    Async async = context.async();
    Endpoint endpoint = camel.getEndpoint("direct:foo");

    vertx.eventBus().registerDefaultCodec(Person.class, new PersonCodec());

    bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
        .addInboundMapping(fromCamel("direct:foo").toVertx("test")));

    vertx.eventBus().<Person>consumer("test", message -> {
      context.assertEquals("bob", message.body().getName());
      async.complete();
    });

    camel.start();
    BridgeHelper.startBlocking(bridge);

    ProducerTemplate producer = camel.createProducerTemplate();
    producer.asyncSendBody(endpoint, new Person().setName("bob"));
  }

  @Test
  public void testWithDirectEndpointAndCustomTypeMissingCodec(TestContext context) throws Exception {
    Async async = context.async();
    Endpoint endpoint = camel.getEndpoint("direct:foo");

    bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
        .addInboundMapping(fromCamel("direct:foo").toVertx("test")));

    camel.start();
    BridgeHelper.startBlocking(bridge);

    ProducerTemplate producer = camel.createProducerTemplate();
    producer.asyncSend(endpoint, exchange -> {
      Message message = exchange.getIn();
      message.setBody(new Person().setName("bob"));
      exchange.addOnCompletion(new SynchronizationAdapter() {
        @Override
        public void onFailure(Exchange exchange) {
          context.assertTrue(exchange.getException().getMessage().contains("No message codec"));
          async.complete();
        }
      });
    });
  }


  @Test
  public void testWithDirectEndpointWithHeaderCopy(TestContext context) throws Exception {
    Async async = context.async();
    Endpoint endpoint = camel.getEndpoint("direct:foo");

    bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
        .addInboundMapping(fromCamel("direct:foo").toVertx("test")));

    vertx.eventBus().consumer("test", message -> {
      context.assertEquals("hello", message.body());
      context.assertEquals(message.headers().get("key"), "value");
      async.complete();
    });

    camel.start();
    BridgeHelper.startBlocking(bridge);

    ProducerTemplate producer = camel.createProducerTemplate();
    producer.asyncSend(endpoint, exchange -> {
      Message message = exchange.getIn();
      message.setBody("hello");
      message.setHeader("key", "value");
    });
  }

  @Test
  public void testWithDirectEndpointWithoutHeaderCopy(TestContext context) throws Exception {
    Async async = context.async();
    Endpoint endpoint = camel.getEndpoint("direct:foo");

    bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
        .addInboundMapping(fromCamel("direct:foo").toVertx("test").withoutHeadersCopy()));

    vertx.eventBus().consumer("test", message -> {
      context.assertEquals("hello", message.body());
      context.assertNull(message.headers().get("key"));
      async.complete();
    });

    camel.start();
    BridgeHelper.startBlocking(bridge);

    ProducerTemplate producer = camel.createProducerTemplate();
    producer.asyncSend(endpoint, exchange -> {
      Message message = exchange.getIn();
      message.setBody("hello");
      message.setHeader("key", "value");
    });
  }

  @Test
  public void testWithDirectEndpoint2(TestContext context) throws Exception {
    Async async = context.async();
    Endpoint endpoint = camel.getEndpoint("direct:foo");

    bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
        .addInboundMapping(fromCamel(endpoint).toVertx("test")));

    vertx.eventBus().consumer("test", message -> {
      context.assertEquals("hello", message.body());
      async.complete();
    });

    camel.start();
    BridgeHelper.startBlocking(bridge);

    ProducerTemplate producer = camel.createProducerTemplate();
    producer.asyncSendBody(endpoint, "hello");
  }

  @Test
  public void testWithDirectEndpointWithPublish(TestContext context) throws Exception {
    Async async = context.async();
    Async async2 = context.async();
    Endpoint endpoint = camel.getEndpoint("direct:foo");

    bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
        .addInboundMapping(fromCamel(endpoint).toVertx("test").usePublish()));

    vertx.eventBus().consumer("test", message -> {
      context.assertEquals("hello", message.body());
      async.complete();
    });

    vertx.eventBus().consumer("test", message -> {
      context.assertEquals("hello", message.body());
      async2.complete();
    });

    camel.start();
    BridgeHelper.startBlocking(bridge);

    ProducerTemplate producer = camel.createProducerTemplate();
    producer.asyncSendBody(endpoint, "hello");
  }

  @Test
  public void testWithDirectEndpointWithPublishAndCustomType(TestContext context) throws Exception {
    Async async = context.async();
    Async async2 = context.async();
    Endpoint endpoint = camel.getEndpoint("direct:foo");

    vertx.eventBus().registerDefaultCodec(Person.class, new PersonCodec());

    bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
        .addInboundMapping(fromCamel(endpoint).toVertx("test").usePublish()));

    vertx.eventBus().<Person>consumer("test", message -> {
      context.assertEquals("bob", message.body().getName());
      async.complete();
    });

    vertx.eventBus().<Person>consumer("test", message -> {
      context.assertEquals("bob", message.body().getName());
      async2.complete();
    });

    camel.start();
    BridgeHelper.startBlocking(bridge);

    ProducerTemplate producer = camel.createProducerTemplate();
    producer.asyncSendBody(endpoint, new Person().setName("bob"));
  }

  @Test
  public void testWithDirectEndpointWithPublishAndCustomTypeNoCodec(TestContext context) throws Exception {
    Async async = context.async();
    Endpoint endpoint = camel.getEndpoint("direct:foo");

    bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
        .addInboundMapping(fromCamel(endpoint).toVertx("test").usePublish()));


    camel.start();
    BridgeHelper.startBlocking(bridge);

    ProducerTemplate producer = camel.createProducerTemplate();
    producer.asyncSend(endpoint, exchange -> {
      Message message = exchange.getIn();
      message.setBody(new Person().setName("bob"));
      exchange.addOnCompletion(new SynchronizationAdapter() {
        @Override
        public void onFailure(Exchange exchange) {
          context.assertTrue(exchange.getException().getMessage().contains("No message codec"));
          async.complete();
        }
      });
    });
  }

  @Test
  public void testWithStomp(TestContext context) throws Exception {
    StompServerHandler serverHandler = StompServerHandler.create(vertx);
    StompServer.create(vertx).handler(serverHandler).listen(ar -> {
      stomp = ar.result();
    });
    await().atMost(DEFAULT_TIMEOUT).until(() -> stomp != null);

    Async async = context.async();

    Endpoint endpoint = camel.getEndpoint("stomp:queue");

    bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
        .addInboundMapping(fromCamel(endpoint).toVertx("test")));

    vertx.eventBus().consumer("test", message -> {
      // We get a buffer.
      context.assertEquals("hello", message.body().toString());
      async.complete();
    });

    camel.start();
    BridgeHelper.startBlocking(bridge);

    StompClient.create(vertx).connect(connection -> {
      connection.result().send("queue", Buffer.buffer("hello"));
      connection.result().close();
    });
  }

  @Test
  @Ignore
  public void testWithStompAndJson(TestContext context) throws Exception {
    StompServerHandler serverHandler = StompServerHandler.create(vertx);
    StompServer.create(vertx).handler(serverHandler).listen(ar -> {
      stomp = ar.result();
    });
    await().atMost(DEFAULT_TIMEOUT).until(() -> stomp != null);

    Async async = context.async();

    Endpoint endpoint = camel.getEndpoint("stomp:queue");

    bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
        .addInboundMapping(fromCamel(endpoint).toVertx("test")));

    vertx.eventBus().<Buffer>consumer("test", message -> {
      // We get a buffer.
      JsonObject json = message.body().toJsonObject();
      context.assertEquals("bar", json.getString("foo"));
      async.complete();
    });

    camel.start();
    BridgeHelper.startBlocking(bridge);

    StompClient.create(vertx).connect(connection -> {
      connection.result().send("queue", Buffer.buffer(new JsonObject().put("foo", "bar").encode()));
      connection.result().close();
    });
  }

  /**
   * Reproducer for https://github.com/vert-x3/vertx-camel-bridge/issues/27
   */
  @Test
  public void testReplyTimeout(TestContext tc) throws Exception {
    Async async = tc.async();

    Endpoint endpoint = camel.getEndpoint("direct:foo");

    bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
      .addInboundMapping(fromCamel(endpoint).toVertx("test").setTimeout(5000)));

    camel.start();
    BridgeHelper.startBlocking(bridge);

    vertx.eventBus().consumer("test", message -> {
      // Simulate a timeout, so do not reply.
    });

    ProducerTemplate producer = camel.createProducerTemplate();
    long begin = System.currentTimeMillis();
    producer.asyncCallbackRequestBody(endpoint, "ping", new Synchronization() {
      @Override
      public void onComplete(Exchange exchange) {
        tc.fail("The interaction should fail");
      }

      @Override
      public void onFailure(Exchange exchange) {
        tc.assertTrue(exchange.getException().getMessage().contains("Timed out"));
        tc.assertTrue(exchange.getException().getMessage().contains("5000"));
        long end = System.currentTimeMillis();
        tc.assertTrue((end - begin) < 20000);
        async.complete();
      }
    });
  }

  @Test
  public void testNoReceiver(TestContext tc) throws Exception {
    Async async = tc.async();

    Endpoint endpoint = camel.getEndpoint("direct:foo");

    bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
      .addInboundMapping(fromCamel(endpoint).toVertx("test").setTimeout(5000)));

    camel.start();
    BridgeHelper.startBlocking(bridge);

    // Unlike the previous test, we don't register a consumer.

    ProducerTemplate producer = camel.createProducerTemplate();
    producer.asyncCallbackRequestBody(endpoint, "ping", new Synchronization() {
      @Override
      public void onComplete(Exchange exchange) {
        tc.fail("The interaction should fail");
      }

      @Override
      public void onFailure(Exchange exchange) {
        tc.assertTrue(exchange.getException().getMessage().contains("No handlers for address test"));
        async.complete();
      }
    });
  }
}

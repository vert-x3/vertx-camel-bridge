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
package io.vertx.ext.camel;

import com.jayway.awaitility.Duration;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.stomp.StompClient;
import io.vertx.ext.stomp.StompServer;
import io.vertx.ext.stomp.StompServerHandler;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.camel.Endpoint;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.jayway.awaitility.Awaitility.await;

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
  public void tearDown(TestContext context) throws Exception {
    bridge.stop();
    camel.stop();
    if (stomp != null) {
      stomp.close(context.asyncAssertSuccess());
    }

    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public void testWithDirectEndpoint(TestContext context) throws Exception {
    Async async = context.async();
    Endpoint endpoint = camel.getEndpoint("direct:foo");

    bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
        .addInboundMapping(new InboundMapping().setAddress("test").setUri("direct:foo")));

    vertx.eventBus().consumer("test", message -> {
      context.assertEquals("hello", message.body());
      async.complete();
    });

    camel.start();
    bridge.start();

    ProducerTemplate producer = camel.createProducerTemplate();
    producer.asyncSendBody(endpoint, "hello");
  }

  @Test
  public void testWithDirectEndpointWithHeaderCopy(TestContext context) throws Exception {
    Async async = context.async();
    Endpoint endpoint = camel.getEndpoint("direct:foo");

    bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
        .addInboundMapping(new InboundMapping().setAddress("test").setUri("direct:foo")));

    vertx.eventBus().consumer("test", message -> {
      context.assertEquals("hello", message.body());
      context.assertEquals(message.headers().get("key"), "value");
      async.complete();
    });

    camel.start();
    bridge.start();

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
        .addInboundMapping(new InboundMapping().setAddress("test").setUri("direct:foo").setHeadersCopy(false)));

    vertx.eventBus().consumer("test", message -> {
      context.assertEquals("hello", message.body());
      context.assertNull(message.headers().get("key"));
      async.complete();
    });

    camel.start();
    bridge.start();

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
        .addInboundMapping(new InboundMapping().setAddress("test").setEndpoint(endpoint)));

    vertx.eventBus().consumer("test", message -> {
      context.assertEquals("hello", message.body());
      async.complete();
    });

    camel.start();
    bridge.start();

    ProducerTemplate producer = camel.createProducerTemplate();
    producer.asyncSendBody(endpoint, "hello");
  }

  @Test
  public void testWithDirectEndpointWithPublish(TestContext context) throws Exception {
    Async async = context.async();
    Async async2 = context.async();
    Endpoint endpoint = camel.getEndpoint("direct:foo");

    bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
        .addInboundMapping(new InboundMapping().setAddress("test").setUri("direct:foo").setPublish(true)));

    vertx.eventBus().consumer("test", message -> {
      context.assertEquals("hello", message.body());
      async.complete();
    });

    vertx.eventBus().consumer("test", message -> {
      context.assertEquals("hello", message.body());
      async2.complete();
    });

    camel.start();
    bridge.start();

    ProducerTemplate producer = camel.createProducerTemplate();
    producer.asyncSendBody(endpoint, "hello");
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
        .addInboundMapping(new InboundMapping().setAddress("test").setEndpoint(endpoint)));

    vertx.eventBus().consumer("test", message -> {
      context.assertEquals("hello", message.body());
      async.complete();
    });

    camel.start();
    bridge.start();

    StompClient.create(vertx).connect(connection -> {
      // /queue, don't ask why they added a /
      connection.result().send("/queue", Buffer.buffer("hello"));
      connection.result().close();
    });
  }
}

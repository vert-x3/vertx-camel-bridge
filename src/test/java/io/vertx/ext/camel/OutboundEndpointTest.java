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
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Random;

import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

/**
 * Tests that event bus messages are propagated to Camel
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class OutboundEndpointTest {

  private static final Duration DEFAULT_TIMEOUT = Duration.TEN_SECONDS;

  private Vertx vertx;
  private DefaultCamelContext camel;


  //TODO Test with Vert.x (yes it's stupid, so what)
  //TODO Test with HTTP (get)


  @Before
  public void setUp(TestContext context) {
    vertx = Vertx.vertx();
    camel = new DefaultCamelContext();
  }

  @After
  public void tearDown(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public void testWithMockWithASingleMessage() throws Exception {
    MockEndpoint endpoint = (MockEndpoint) camel.getComponent("mock").createEndpoint("mock:foo");
    camel.addEndpoint("output", endpoint);

    CamelBridge bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
        .addOutboundMapping(new OutboundMapping().setAddress("test").setUri("output")));

    camel.start();
    bridge.start();

    vertx.eventBus().send("test", "hello");

    await().atMost(DEFAULT_TIMEOUT).until(() -> !endpoint.getExchanges().isEmpty());
    endpoint.expectedBodiesReceived("hello");

    Exchange exchange = endpoint.getExchanges().get(0);
    assertThat(exchange.getIn().getBody()).isEqualTo("hello");
    assertThat(exchange.getIn().getHeaders()).hasSize(1);
  }

  @Test
  public void testWithMockWithASingleMessageUsingByteArray() throws Exception {
    byte[] bytes = getRandomBytes();
    MockEndpoint endpoint = (MockEndpoint) camel.getComponent("mock").createEndpoint("mock:foo");
    camel.addEndpoint("output", endpoint);

    CamelBridge bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
        .addOutboundMapping(new OutboundMapping().setAddress("test").setUri("output")));

    camel.start();
    bridge.start();

    vertx.eventBus().send("test", bytes);

    await().atMost(DEFAULT_TIMEOUT).until(() -> !endpoint.getExchanges().isEmpty());
    Exchange exchange = endpoint.getExchanges().get(0);
    assertThat((byte[]) exchange.getIn().getBody()).isEqualTo(bytes);
  }

  @Test
  public void testWithMockWithASingleMessageUsingBuffer() throws Exception {
    byte[] bytes = getRandomBytes();
    MockEndpoint endpoint = (MockEndpoint) camel.getComponent("mock").createEndpoint("mock:foo");
    camel.addEndpoint("output", endpoint);

    CamelBridge bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
        .addOutboundMapping(new OutboundMapping().setAddress("test").setUri("output")));

    camel.start();
    bridge.start();

    vertx.eventBus().send("test", Buffer.buffer(bytes));

    await().atMost(DEFAULT_TIMEOUT).until(() -> !endpoint.getExchanges().isEmpty());
    Exchange exchange = endpoint.getExchanges().get(0);
    assertThat(exchange.getIn().getBody()).isEqualTo(Buffer.buffer(bytes));
  }


  private byte[] getRandomBytes() {
    Random random = new Random();
    byte[] bytes = new byte[1024];
    random.nextBytes(bytes);
    return bytes;
  }

  @Test
  public void testWithMockWithASingleMessageHeadersNotCopied() throws Exception {
    MockEndpoint endpoint = (MockEndpoint) camel.getComponent("mock").createEndpoint("mock:foo");
    camel.addEndpoint("output", endpoint);

    CamelBridge bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
        .addOutboundMapping(new OutboundMapping().setAddress("test").setUri("output").setHeadersCopy(false)));

    camel.start();
    bridge.start();

    vertx.eventBus().send("test", "hello", new DeliveryOptions().addHeader("key", "value"));

    await().atMost(DEFAULT_TIMEOUT).until(() -> !endpoint.getExchanges().isEmpty());
    endpoint.expectedBodiesReceived("hello");

    Exchange exchange = endpoint.getExchanges().get(0);
    assertThat(exchange.getIn().getBody()).isEqualTo("hello");
    assertThat(exchange.getIn().getHeaders()).doesNotContainKey("key").hasSize(1);
  }

  @Test
  public void testWithMockWithASingleMessageHeadersCopied() throws Exception {
    MockEndpoint endpoint = (MockEndpoint) camel.getComponent("mock").createEndpoint("mock:foo");
    camel.addEndpoint("output", endpoint);

    CamelBridge bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
        .addOutboundMapping(new OutboundMapping().setAddress("test").setUri("output")));

    camel.start();
    bridge.start();

    vertx.eventBus().send("test", "hello", new DeliveryOptions().addHeader("key", "value"));

    await().atMost(DEFAULT_TIMEOUT).until(() -> !endpoint.getExchanges().isEmpty());
    endpoint.expectedBodiesReceived("hello");

    Exchange exchange = endpoint.getExchanges().get(0);
    assertThat(exchange.getIn().getBody()).isEqualTo("hello");
    assertThat(exchange.getIn().getHeaders()).contains(entry("key", "value")).hasSize(2);
  }

  @Test
  public void testWithMockWithMultipleMessages() throws Exception {
    MockEndpoint endpoint = (MockEndpoint) camel.getComponent("mock").createEndpoint("mock:foo");
    camel.addEndpoint("output", endpoint);

    CamelBridge bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
        .addOutboundMapping(new OutboundMapping().setAddress("test").setUri("output")));

    camel.start();
    bridge.start();

    vertx.eventBus().send("test", "hello");
    vertx.eventBus().send("test", "hello2");

    await().atMost(DEFAULT_TIMEOUT).until(() -> endpoint.getExchanges().size() == 2);
    endpoint.expectedBodiesReceived("hello", "hello2");
  }

  @Test
  public void testWithMockUsingOptions() throws Exception {
    MockEndpoint endpoint = (MockEndpoint) camel.getComponent("mock").createEndpoint("mock:foo?retainLast=2");
    camel.addEndpoint("output", endpoint);

    CamelBridge bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
        .addOutboundMapping(new OutboundMapping().setAddress("test").setUri("output")));

    camel.start();
    bridge.start();

    vertx.eventBus().send("test", "hello");
    vertx.eventBus().send("test", "hello2");

    await().atMost(DEFAULT_TIMEOUT).until(() -> endpoint.getExchanges().size() == 2);
  }

  @Test
  public void testWithSeveralEndpoints() throws Exception {
    MockEndpoint endpoint = (MockEndpoint) camel.getComponent("mock").createEndpoint("mock:foo");
    MockEndpoint endpoint2 = (MockEndpoint) camel.getEndpoint("mock:foo2");

    camel.addEndpoint("output", endpoint);
    camel.addEndpoint("output2", endpoint2);

    CamelBridge bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
        .addOutboundMapping(new OutboundMapping().setAddress("test").setUri("output"))
        .addOutboundMapping(new OutboundMapping().setAddress("test").setEndpoint(endpoint2))
    );

    camel.start();
    bridge.start();

    vertx.eventBus().publish("test", "hello");
    vertx.eventBus().publish("test", "hello2");

    await().atMost(DEFAULT_TIMEOUT).until(() -> endpoint.getExchanges().size() == 2);
    await().atMost(DEFAULT_TIMEOUT).until(() -> endpoint2.getExchanges().size() == 2);

    endpoint.expectedBodiesReceived("hello", "hello2");
    endpoint2.expectedBodiesReceived("hello", "hello2");
  }

  @Test
  public void testWithStreams() throws Exception {
    File root = new File("target/junk");
    File file = new File(root, "foo.txt");
    if (file.exists()) {
      file.delete();
    }
    root.mkdirs();

    Endpoint endpoint = camel.getEndpoint("stream:file?fileName=target/junk/foo.txt");
    camel.addEndpoint("output", endpoint);

    CamelBridge bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
        .addOutboundMapping(new OutboundMapping().setAddress("test").setUri("output")));

    camel.start();
    bridge.start();

    long date = System.currentTimeMillis();
    vertx.eventBus().send("test", date);

    await().atMost(DEFAULT_TIMEOUT).until(() -> file.isFile() && FileUtils.readFileToString(file).length() > 0);
    String string = FileUtils.readFileToString(file);
    assertThat(string).contains(Long.toString(date));

    long date2 = System.currentTimeMillis();
    vertx.eventBus().send("test", date2);

    await().atMost(DEFAULT_TIMEOUT).until(() -> FileUtils.readFileToString(file).length() > string.length());
    assertThat(FileUtils.readFileToString(file)).containsSequence(Long.toString(date), Long.toString(date2));
  }

  @Test
  public void testReply(TestContext context) throws Exception {
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

    Async async = context.async();
    vertx.eventBus().send("test", "hello", reply -> {
      context.assertEquals("OK", reply.result().body());
      async.complete();
    });
  }

}

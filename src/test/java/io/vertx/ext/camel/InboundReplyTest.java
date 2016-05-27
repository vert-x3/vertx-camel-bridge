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
import io.vertx.ext.stomp.StompServer;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.camel.Endpoint;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test inbound mapping replies.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class InboundReplyTest {

  private static final Duration DEFAULT_TIMEOUT = Duration.TEN_SECONDS;

  private Vertx vertx;
  private DefaultCamelContext camel;


  private CamelBridge bridge;


  @Before
  public void setUp(TestContext context) {
    vertx = Vertx.vertx();
    camel = new DefaultCamelContext();
  }

  @After
  public void tearDown(TestContext context) throws Exception {
    bridge.stop();
    camel.stop();
    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public void testReply() throws Exception {
    Endpoint endpoint = camel.getEndpoint("direct:stuff");

    bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
        .addInboundMapping(new InboundMapping().setAddress("test-reply").setEndpoint(endpoint)));

    vertx.eventBus().consumer("test-reply", message -> {
      message.reply("How are you ?");
    });

    camel.start();
    BridgeHelper.startBlocking(bridge);

    ProducerTemplate template = camel.createProducerTemplate();
    Future<Object> future = template.asyncRequestBody(endpoint, "hello");
    String response = template.extractFutureBody(future, String.class);
    assertThat(response).isEqualTo("How are you ?");
  }

  @Test
  public void testReplyWithCustomType() throws Exception {
    Endpoint endpoint = camel.getEndpoint("direct:stuff");

    vertx.eventBus().registerDefaultCodec(Person.class, new PersonCodec());

    bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
        .addInboundMapping(new InboundMapping().setAddress("test-reply").setEndpoint(endpoint)));

    vertx.eventBus().consumer("test-reply", message -> {
      message.reply(new Person().setName("alice"));
    });

    camel.start();
    BridgeHelper.startBlocking(bridge);

    ProducerTemplate template = camel.createProducerTemplate();
    Future<Object> future = template.asyncRequestBody(endpoint, new Person().setName("bob"));
    Person response = template.extractFutureBody(future, Person.class);
    assertThat(response.getName()).isEqualTo("alice");
  }


}

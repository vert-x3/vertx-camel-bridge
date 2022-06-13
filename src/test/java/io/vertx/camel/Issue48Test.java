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

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.Message;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class Issue48Test {

  private static final Logger LOG = LoggerFactory.getLogger(Issue48Test.class);

  @Test
  public void testCamelBridge() throws Exception {
    CamelContext camelContext = new DefaultCamelContext();
    camelContext.addRoutes(new RouteBuilder() {
      @Override
      public void configure() {
        from("direct:start").process(e -> {
        }).transform(constant("OK"));
      }
    });

    VertxOptions vertxOptions = new VertxOptions();
    vertxOptions.setEventLoopPoolSize(4);
    vertxOptions.setWorkerPoolSize(4);
    Vertx vertx = Vertx.vertx(vertxOptions);

    CamelBridge bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camelContext)
      .addOutboundMapping(OutboundMapping.fromVertx("test").toCamel("direct:start")));

    camelContext.start();
    BridgeHelper.startBlocking(bridge);

    Future<Message<Object>> fut = vertx.eventBus().request("test", "hello");
    Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(fut::isComplete);

    Assertions.assertThat(fut.isComplete()).isEqualTo(true);
    Object out = fut.result().body();
    Assertions.assertThat(out).isEqualTo("OK");
  }

}

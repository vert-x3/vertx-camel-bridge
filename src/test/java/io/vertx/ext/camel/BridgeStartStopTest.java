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

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.vertx.ext.camel.InboundMapping.fromCamel;

/**
 * Check start-stop sequences.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class BridgeStartStopTest {

  private Vertx vertx;
  private DefaultCamelContext camel;

  private CamelBridge bridge;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    camel = new DefaultCamelContext();
  }

  @After
  public void tearDown(TestContext context) throws Exception {
    camel.stop();
    vertx.close(context.asyncAssertSuccess());
  }


  @Test
  public void startStopSequence() throws InterruptedException {
    bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
        .addInboundMapping(fromCamel("direct:foo").toVertx("test")));

    bridge.start();

    Thread.sleep(10);

    bridge.stop();
  }

  @Test
  public void startStopSequenceAsync() throws InterruptedException {
    bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
        .addInboundMapping(fromCamel("direct:foo").toVertx("test")));

    BridgeHelper.startBlocking(bridge);
    BridgeHelper.stopBlocking(bridge);
  }

}

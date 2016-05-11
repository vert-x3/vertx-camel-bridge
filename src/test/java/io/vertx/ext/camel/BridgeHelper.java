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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.core.Is.is;

/**
 * Two methods to wait until the Camel bridge has been started / stopped.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class BridgeHelper {

  public static void startBlocking(CamelBridge bridge) {
    AtomicReference<Boolean> spy = new AtomicReference<>();
    bridge.start(v -> {
      if (v.failed()) {
        v.cause().printStackTrace();
      }
      spy.set(v.succeeded());
    });
    await().atMost(2, TimeUnit.SECONDS).untilAtomic(spy, is(true));
  }

  public static void stopBlocking(CamelBridge bridge) {
    AtomicReference<Boolean> spy = new AtomicReference<>();
    bridge.stop(v -> {
      if (v.failed()) {
        v.cause().printStackTrace();
      }
      spy.set(v.succeeded());
    });
    await().atMost(2, TimeUnit.SECONDS).untilAtomic(spy, is(true));
  }
}

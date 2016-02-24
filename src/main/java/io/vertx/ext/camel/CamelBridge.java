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
import io.vertx.ext.camel.impl.CamelBridgeImpl;

/**
 * Camel Bridge facade.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public interface CamelBridge {

  /**
   * Creates a bridge between Camel endpoints and Vert.x
   *
   * @param vertx         the vert.x instance
   * @param bridgeOptions the bridge configuration
   * @return the created {@link CamelBridge}. It must be started explicitly.
   */
  static CamelBridge create(Vertx vertx, CamelBridgeOptions bridgeOptions) {
    return new CamelBridgeImpl(vertx, bridgeOptions);
  }

  /**
   * Starts the bridges.
   *
   * @return the current {@link CamelBridge}
   */
  CamelBridge start();


  /**
   * Stops the bridge
   *
   * @return the current {@link CamelBridge}
   */
  CamelBridge stop();

}

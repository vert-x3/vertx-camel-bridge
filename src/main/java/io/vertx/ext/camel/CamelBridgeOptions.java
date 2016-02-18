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

import org.apache.camel.CamelContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Camel bridge configuration.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class CamelBridgeOptions {

  private List<InboundMapping> inbound = new ArrayList<>();
  private List<OutboundMapping> outbound = new ArrayList<>();

  private final CamelContext context;

  public CamelBridgeOptions(CamelContext context) {
    Objects.requireNonNull(context);
    this.context = context;
  }

  /**
   * Adds an inbound mapping (Camel to Vert.x).
   *
   * @param mapping the mapping, must not be {@code null}
   * @return the current {@link CamelBridgeOptions}
   */
  public CamelBridgeOptions addInboundMapping(InboundMapping mapping) {
    Objects.requireNonNull(mapping);
    this.inbound.add(mapping);
    return this;
  }

  /**
   * Adds an outbound mapping (Vert.x to Camel).
   *
   * @param mapping the mapping, must not be {@code null}
   * @return the current {@link CamelBridgeOptions}
   */
  public CamelBridgeOptions addOutboundMapping(OutboundMapping mapping) {
    this.outbound.add(mapping);
    return this;
  }

  public CamelContext getCamelContext() {
    return context;
  }

  public List<InboundMapping> getInboundMappings() {
    return inbound;
  }

  public List<OutboundMapping> getOutboundMappings() {
    return outbound;
  }
}

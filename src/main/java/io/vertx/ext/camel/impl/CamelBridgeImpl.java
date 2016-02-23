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
package io.vertx.ext.camel.impl;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.camel.*;
import org.apache.camel.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The implementation of the camel bridge.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class CamelBridgeImpl implements CamelBridge {

  private final CamelContext camel;
  private final List<Consumer> camelConsumers = new ArrayList<>();
  private final List<Producer> camelProducers = new ArrayList<>();
  private final List<MessageConsumer> vertxConsumers = new ArrayList<>();

  private static final Logger LOGGER = LoggerFactory.getLogger(CamelBridgeImpl.class);


  public CamelBridgeImpl(Vertx vertx, CamelBridgeOptions options) {
    Objects.requireNonNull(vertx);
    Objects.requireNonNull(options);
    this.camel = options.getCamelContext();
    Objects.requireNonNull(camel);

    for (InboundMapping inbound : options.getInboundMappings()) {
      // camel -> vert.x
      createInboundBridge(vertx, inbound);
    }

    for (OutboundMapping outbound : options.getOutboundMappings()) {
      // vert.x -> camel
      createOutboundBridge(vertx, outbound);
    }
  }

  private void createOutboundBridge(Vertx vertx, OutboundMapping outbound) {
    Endpoint endpoint = validate(outbound);

    Producer producer;
    try {
      producer = endpoint.createProducer();
      camelProducers.add(producer);
    } catch (Exception e) {
      throw new IllegalStateException("The endpoint " + outbound.getUri() + " does not support producers");
    }

    LOGGER.info("Creating Vert.x message consumer for " + outbound.getUri() + " receiving messages from " + outbound
        .getAddress());

    vertxConsumers.add(vertx.eventBus().consumer(outbound.getAddress(), new FromVertxToCamelProducer(producer, outbound)));
  }

  private void createInboundBridge(Vertx vertx, InboundMapping inbound) {
    Endpoint endpoint = validate(inbound);

    try {
      LOGGER.info("Creating camel consumer for " + inbound.getUri() + " sending messages to " + inbound.getAddress());
      camelConsumers.add(endpoint.createConsumer(new CamelToVertxProcessor(vertx, inbound)));
    } catch (Exception e) {
      throw new IllegalStateException("The endpoint " + inbound.getUri() + " does not support consumers");
    }
  }

  private Endpoint validate(CamelMapping mapping) {
    Objects.requireNonNull(mapping.getAddress(), "The vert.x event bus address must not be `null`");
    Objects.requireNonNull(mapping.getUri(), "The endpoint uri must not be `null`");
    Endpoint endpoint = camel.getEndpoint(mapping.getUri());
    Objects.requireNonNull(endpoint, "Cannot find the endpoint " + mapping.getUri() + " in the camel context");
    return endpoint;
  }

  @Override
  public CamelBridge start() {
    camelConsumers.stream().forEach(c -> {
      try {
        c.start();
      } catch (Exception e) {
        throw new IllegalStateException("Cannot start consumer", e);
      }
    });
    camelProducers.stream().forEach(c -> {
      try {
        c.start();
      } catch (Exception e) {
        throw new IllegalStateException("Cannot start producer", e);
      }
    });
    return this;
  }

  @Override
  public CamelBridge stop() {
    camelConsumers.stream().forEach(c -> {
      try {
        c.stop();
      } catch (Exception e) {
        throw new IllegalStateException("Cannot stop consumer", e);
      }
    });
    camelProducers.stream().forEach(c -> {
      try {
        c.stop();
      } catch (Exception e) {
        throw new IllegalStateException("Cannot stop producer", e);
      }
    });
    vertxConsumers.stream().forEach(MessageConsumer::unregister);
    return this;
  }


}

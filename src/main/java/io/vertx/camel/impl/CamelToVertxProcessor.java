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
package io.vertx.camel.impl;

import io.vertx.camel.InboundMapping;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.util.AsyncProcessorHelper;
import org.apache.camel.util.ExchangeHelper;

/**
 * Processor to send messages from Camel to Vert.x (inbound).
 */
public class CamelToVertxProcessor implements AsyncProcessor {

  private final Vertx vertx;
  private final InboundMapping inbound;

  /**
   * Creates a new instance of processor.
   *
   * @param vertx   the Vert.x instance, must not be {@code null}
   * @param inbound the inbound mapping (configuration), must not be {@code null}
   */
  public CamelToVertxProcessor(Vertx vertx, InboundMapping inbound) {
    this.vertx = vertx;
    this.inbound = inbound;
  }

  @Override
  public void process(Exchange exchange) throws Exception {
    AsyncProcessorHelper.process(this, exchange);
  }

  @Override
  public boolean process(Exchange exchange, AsyncCallback callback) {
    Message in = exchange.getIn();

    Object body = CamelHelper.convert(inbound, in);

    DeliveryOptions delivery = CamelHelper.getDeliveryOptions(in, inbound.isHeadersCopy());

    try {
      if (inbound.isPublish()) {
        vertx.eventBus().publish(inbound.getAddress(), body, delivery);
      } else {
        if (ExchangeHelper.isOutCapable(exchange)) {
          vertx.eventBus().send(inbound.getAddress(), body, delivery, reply -> {
            Message out = exchange.getOut();
            if (reply.succeeded()) {
              out.setBody(reply.result().body());
              out.setHeaders(MultiMapHelper.toMap(reply.result().headers()));
            } else {
              exchange.setException(reply.cause());
            }
            // continue callback
            callback.done(false);
          });
          // being routed async so return false
          return false;
        } else {
          // No reply expected.
          vertx.eventBus().send(inbound.getAddress(), body, delivery);
        }
      }
    } catch (Throwable e) {
      // Mark the exchange as "failed".
      exchange.setException(e);
    }

    callback.done(true);
    return true;
  }

}

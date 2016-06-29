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

import io.vertx.camel.OutboundMapping;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.ReplyFailure;
import org.apache.camel.*;
import org.apache.camel.util.AsyncProcessorConverterHelper;

/**
 * Handles the transfer from Vert.x message to Camel (outbound).
 */
public class FromVertxToCamelProducer implements Handler<io.vertx.core.eventbus.Message<Object>> {

  private final Endpoint endpoint;
  private final AsyncProcessor producer;
  private final OutboundMapping outbound;

  /**
   * Creates a new instance of producer.
   *
   * @param producer the underlying producer, must not be {@code null}
   * @param outbound the outbound configuration, must not be {@code null}
   */
  public FromVertxToCamelProducer(Producer producer, OutboundMapping outbound) {
    this.endpoint = producer.getEndpoint();
    this.producer = AsyncProcessorConverterHelper.convert(producer);
    this.outbound = outbound;
  }

  @Override
  public void handle(io.vertx.core.eventbus.Message<Object> vertxMessage) {
    ExchangePattern mep = vertxMessage.replyAddress() != null ? ExchangePattern.InOut : ExchangePattern.InOnly;
    Exchange exchange = endpoint.createExchange(mep);

    Message in = exchange.getIn();
    in.setBody(vertxMessage.body());
    if (outbound.isHeadersCopy()) {
      MultiMapHelper.toMap(vertxMessage.headers(), in.getHeaders());
    }

    // It's an async producer so won't block.
    producer.process(exchange, new CamelProducerCallback(exchange, vertxMessage));
  }

  private static final class CamelProducerCallback implements AsyncCallback {

    private final Exchange exchange;
    private final io.vertx.core.eventbus.Message<Object> vertxMessage;

    public CamelProducerCallback(Exchange exchange, io.vertx.core.eventbus.Message<Object> vertxMessage) {
      this.exchange = exchange;
      this.vertxMessage = vertxMessage;
    }

    @Override
    public void done(boolean done) {
      // Method called in a Camel thread.

      // when we are done then send back reply to vertx if we are supposed to
      if (vertxMessage.replyAddress() != null) {
        // if the exchange failed with an exception then fail
        if (exchange.getException() != null) {
          vertxMessage.fail(ReplyFailure.RECIPIENT_FAILURE.toInt(), exchange.getException().getMessage());
        } else {
          Message msg = exchange.hasOut() ? exchange.getOut() : exchange.getIn();
          Object body = msg.getBody();
          DeliveryOptions delivery = CamelHelper.getDeliveryOptions(msg, true);
          vertxMessage.reply(body, delivery);
        }
      }
    }
  }

}

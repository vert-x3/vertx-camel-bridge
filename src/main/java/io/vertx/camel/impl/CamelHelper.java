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
import io.vertx.core.eventbus.DeliveryOptions;
import org.apache.camel.Message;
import org.fusesource.hawtbuf.Buffer;

/**
 * A set of helpers methods, used in both inbound and outbound conversion.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
class CamelHelper {

  private CamelHelper() {
    // Avoid direct instantiation.
  }

  /**
   * Creates {@link DeliveryOptions} from the given {@code message}. It simply copies the headers if the {@code
   * headerCopy} parameter is set to {@code true}.
   *
   * @param msg        the message from Camel, must not be {@code null}
   * @param headerCopy whether or not the headers need to be copied
   * @return the created {@link DeliveryOptions}
   */
  static DeliveryOptions getDeliveryOptions(Message msg, boolean headerCopy) {
    DeliveryOptions delivery = new DeliveryOptions();
    if (headerCopy && msg.hasHeaders()) {
      msg.getHeaders().entrySet().stream().forEach(entry -> {
        if (entry.getValue() != null) {
          delivery.addHeader(entry.getKey(), entry.getValue().toString());
        }
      });
    }
    return delivery;
  }

  /**
   * Converts the body of the given message into the specified type. If the type is not specified in the
   * {@link InboundMapping}, it returns the body as it is. However, if this payload is a Hawtbuf Buffer an automatic
   * conversion to Vert.x buffer is done.
   *
   * @param inbound the inbound mapping configuration
   * @param msg     the message from camel
   * @return the body to be sent on the event bus, the type of the returned object depends on the mapping
   * configuration.
   */
  static Object convert(InboundMapping inbound, Message msg) {
    if (inbound.getBodyType() != null) {
      return msg.getBody(inbound.getBodyType());
    } else {
      Object body = msg.getBody();
      if (body instanceof org.fusesource.hawtbuf.Buffer) {
        // Map to Vert.x buffers.
        return io.vertx.core.buffer.Buffer.buffer(((Buffer) body).toByteArray());
      }
      return body;
    }
  }
}

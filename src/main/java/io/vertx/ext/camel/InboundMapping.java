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

import org.apache.camel.Endpoint;

/**
 * Represents a mapping between a Camel endpoint address and a Vert.x address on the event bus.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class InboundMapping extends CamelMapping {

  public static final boolean DEFAULT_PUBLISH = false;

  private boolean publish = DEFAULT_PUBLISH;

  private Class bodyType;

  /**
   * @return whether or not {@code publish} is used instead of {@code send}, when a message is sent on the event bus.
   * {@code send} is used by default.
   */
  public boolean isPublish() {
    return publish;
  }

  /**
   * Sets whether or not {@code publish} is used instead of {@code send}, when a message is sent on the event bus.
   * {@code send} is used by default.
   *
   * @param publish {@code true} to use {@code publish}
   * @return the current {@link InboundMapping}
   */
  public InboundMapping setPublish(boolean publish) {
    this.publish = publish;
    return this;
  }

  /**
   * @return the type of the body of the message that are sent on the event bus. A Camel converter from the Camel
   * message payload to the given type is used for conversion. If not set, no conversions are applied.
   */
  public Class getBodyType() {
    return bodyType;
  }

  /**
   * Sets the type of the body of the event bus message. A Camel converter from the Camel
   * message payload to the given type is used for conversion. If not set, no conversions are applied.
   *
   * @param bodyType the body type.
   * @return the current {@link InboundMapping}
   */
  public InboundMapping setBodyType(Class bodyType) {
    this.bodyType = bodyType;
    return this;
  }

  @Override
  public InboundMapping setAddress(String address) {
    super.setAddress(address);
    return this;
  }

  @Override
  public InboundMapping setHeadersCopy(boolean headersCopy) {
    super.setHeadersCopy(headersCopy);
    return this;
  }

  @Override
  public InboundMapping setUri(String uri) {
    super.setUri(uri);
    return this;
  }

  @Override
  public InboundMapping setEndpoint(Endpoint endpoint) {
    super.setEndpoint(endpoint);
    return this;
  }
}

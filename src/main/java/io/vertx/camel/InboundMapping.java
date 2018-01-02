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

import org.apache.camel.Endpoint;

import java.util.Objects;

/**
 * Represents a mapping between a Camel endpoint address and a Vert.x address on the event bus.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class InboundMapping extends CamelMapping {

  /**
   * The default value of the "publish" property. By default, the bridge uses {@code send}.
   */
  public static final boolean DEFAULT_PUBLISH = false;

  private boolean publish = DEFAULT_PUBLISH;

  private Class bodyType;

  private int timeout;

  /**
   * Creates an {@link InboundMapping} from the given Camel endpoint.
   *
   * @param uri the uri - must not be {@code null}
   * @return the created {@link InboundMapping}
   */
  public static InboundMapping fromCamel(String uri) {
    Objects.requireNonNull(uri);
    return new InboundMapping().setUri(uri);
  }

  /**
   * Creates an {@link InboundMapping} from the given Camel endpoint.
   *
   * @param endpoint the endpoint - must not be {@code null}
   * @return the created {@link InboundMapping}
   */
  public static InboundMapping fromCamel(Endpoint endpoint) {
    Objects.requireNonNull(endpoint);
    return new InboundMapping().setEndpoint(endpoint);
  }

  /**
   * @return whether or not {@code publish} is used instead of {@code send}, when a message is sent on the event bus.
   * {@code send} is used by default.
   */
  public boolean isPublish() {
    return publish;
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
  public InboundMapping withBodyType(Class bodyType) {
    this.bodyType = bodyType;
    return this;
  }

  @Override
  public InboundMapping setAddress(String address) {
    super.setAddress(address);
    return this;
  }

  /**
   * Fluent version of {@link #setAddress(String)}.
   *
   * @param address the Vert.x event bus address
   * @return the current {@link InboundMapping} instance
   * @see #setAddress(String)
   */
  public InboundMapping toVertx(String address) {
    return setAddress(address);
  }

  /**
   * Fluent version of {@link #setHeadersCopy(boolean)} to disable the headers copy (so the parameter is {@code false}).
   *
   * @return the current {@link InboundMapping} instance
   * @see #setHeadersCopy(boolean)
   */
  public InboundMapping withoutHeadersCopy() {
    return setHeadersCopy(false);
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

  /**
   * Sets whether or not {@code publish} is used instead of {@code send}, when a message is sent on the event bus.
   * {@code send} is used by default, so calling this method instructs the bridge to use {@code publish}.
   *
   * @return the current {@link InboundMapping}
   */
  public InboundMapping usePublish() {
    publish = true;
    return this;
  }

  /**
   * Sets the timeout in milliseconds.
   * @param timeout the time out, must be strictly positive
   *
   * @return the current {@link InboundMapping}
   */
  public InboundMapping setTimeout(int timeout) {
    if (timeout <= 0) {
      throw new IllegalArgumentException("Invalid timeout value, it must be > 0");
    }
    this.timeout = timeout;
    return this;
  }

  public int getTimeout() {
    return timeout;
  }
}

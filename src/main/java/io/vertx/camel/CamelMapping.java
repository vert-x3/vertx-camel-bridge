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

/**
 * Parent class for mapping between Vert.x event bus and Camel endpoints.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class CamelMapping {

  public static final boolean DEFAULT_HEADERS_COPY = true;
  private boolean headersCopy = DEFAULT_HEADERS_COPY;

  private String uri;
  private String address;

  /**
   * @return the Camel endpoint URI.
   */
  public String getUri() {
    return uri;
  }

  /**
   * Sets the Camel endpoint URI.
   *
   * @param uri the uri
   * @return the current {@link CamelMapping}
   */
  public CamelMapping setUri(String uri) {
    this.uri = uri;
    return this;
  }

  /**
   * Sets the Camel endpoint. If used, {@link #setUri(String)} does not need to be called.
   *
   * @param endpoint the endpoint
   * @return the current {@link CamelMapping}
   */
  public CamelMapping setEndpoint(Endpoint endpoint) {
    this.uri = endpoint.getEndpointUri();
    return this;
  }

  /**
   * @return whether or not the headers of the input message are copied in the output message.
   */
  public boolean isHeadersCopy() {
    return headersCopy;
  }

  /**
   * Sets whether or not the headers of the input message are copied in the output message.
   *
   * @param headersCopy {@code true} to copy the headers
   * @return the current {@link CamelMapping}
   */
  public CamelMapping setHeadersCopy(boolean headersCopy) {
    this.headersCopy = headersCopy;
    return this;
  }

  /**
   * @return the event bus address.
   */
  public String getAddress() {
    return address;
  }

  /**
   * Sets the event bus address.
   *
   * @param address the address
   * @return the current {@link CamelMapping}
   */
  public CamelMapping setAddress(String address) {
    this.address = address;
    return this;
  }

}

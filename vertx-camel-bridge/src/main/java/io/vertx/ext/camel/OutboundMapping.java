package io.vertx.ext.camel;

import org.apache.camel.Endpoint;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class OutboundMapping {

  private String address;

  private String uri;

  private boolean copyHeaders;

  public String getAddress() {
    return address;
  }

  public OutboundMapping setAddress(String address) {
    this.address = address;
    return this;
  }

  public boolean isCopyHeaders() {
    return copyHeaders;
  }

  public OutboundMapping setCopyHeaders(boolean copyHeaders) {
    this.copyHeaders = copyHeaders;
    return this;
  }

  public String getUri() {
    return uri;
  }

  public OutboundMapping setUri(String uri) {
    this.uri = uri;
    return this;
  }

  public OutboundMapping setEndpoint(Endpoint endpoint) {
    this.uri = endpoint.getEndpointUri();
    return this;
  }
}

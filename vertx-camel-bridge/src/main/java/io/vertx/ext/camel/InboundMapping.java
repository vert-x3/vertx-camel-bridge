package io.vertx.ext.camel;

import org.apache.camel.Endpoint;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class InboundMapping {

  private String address;

  private String uri;

  private boolean publish = false;

  private boolean headersCopy;


  public String getAddress() {
    return address;
  }

  public InboundMapping setAddress(String address) {
    this.address = address;
    return this;
  }

  public boolean isHeadersCopy() {
    return headersCopy;
  }

  public InboundMapping setHeadersCopy(boolean headersCopy) {
    this.headersCopy = headersCopy;
    return this;
  }

  public boolean isPublish() {
    return publish;
  }

  public InboundMapping setPublish(boolean publish) {
    this.publish = publish;
    return this;
  }

  public String getUri() {
    return uri;
  }

  public InboundMapping setUri(String uri) {
    this.uri = uri;
    return this;
  }

  public InboundMapping setEndpoint(Endpoint endpoint) {
    this.uri = endpoint.getEndpointUri();
    return this;
  }
}

package io.vertx.ext.camel;

import org.apache.camel.CamelContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class CamelBridgeOptions {

  private List<InboundMapping> inbound = new ArrayList<>();
  private List<OutboundMapping> outbound = new ArrayList<>();
  private CamelContext context;

  public CamelBridgeOptions addInboundMapping(InboundMapping mapping) {
    this.inbound.add(mapping);
    return this;
  }

  public CamelBridgeOptions addOutboundMapping(OutboundMapping mapping) {
    this.outbound.add(mapping);
    return this;
  }

  public CamelBridgeOptions setCamelContext(CamelContext context) {
    this.context = context;
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

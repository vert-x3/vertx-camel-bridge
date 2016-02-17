package io.vertx.ext.camel;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.ext.bridge.BridgeOptions;
import org.apache.camel.CamelContext;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@VertxGen
public interface CamelBridge {

  @GenIgnore
  static CamelBridge create(Vertx vertx, CamelBridgeOptions bridgeOptions) {
    return new CamelBridgeImpl(vertx, bridgeOptions);
  }

  @Fluent
  CamelBridge start();


  @Fluent
  CamelBridge stop();



}

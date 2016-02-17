package io.vertx.ext.camel.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.stomp.Frame;
import io.vertx.ext.stomp.StompServer;
import io.vertx.ext.stomp.StompServerHandler;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class StompVerticle extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    StompServer.create(vertx)
        .handler(StompServerHandler.create(vertx))
        .listen();
  }
}

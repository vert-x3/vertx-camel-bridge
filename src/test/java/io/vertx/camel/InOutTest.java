package io.vertx.camel;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.Is.is;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class InOutTest {

  private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

  private Vertx vertx;
  private DefaultCamelContext camel;
  private CamelBridge bridge;

  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx();
    camel = new DefaultCamelContext();
    vertx.exceptionHandler(tc.exceptionHandler());
  }

  @After
  public void tearDown(TestContext context) throws Exception {
    bridge.stop();
    camel.stop();
    vertx.close().onComplete(context.asyncAssertSuccess());
  }

  @Test
  public void testInOut() throws Exception {
    AtomicBoolean headersReceived = new AtomicBoolean();
    AtomicBoolean responseReceived = new AtomicBoolean();

    camel.addRoutes(new RouteBuilder() {
      @Override
      public void configure() throws Exception {
        from("direct:input")
          .inOut("direct:code-generator")
          .process()
          .body((o, h) -> {
            assertThat(h).contains(entry("foo", "bar"));
            headersReceived.set(true);
          });
      }
    });

    bridge = CamelBridge.create(vertx, new CamelBridgeOptions(camel)
      .addInboundMapping(InboundMapping.fromCamel("direct:code-generator").toVertx("code-generator")));

    vertx.eventBus().consumer("code-generator", msg -> {
      assertThat(msg.headers().names()).hasSize(2)
        .contains("headerA", "headerB");

      msg.reply("OK !", new DeliveryOptions().addHeader("foo", "bar"));

    });

    camel.start();
    BridgeHelper.startBlocking(bridge);

    ProducerTemplate producer = camel.createProducerTemplate();
    Map<String, Object> map = new HashMap<>();
    map.putIfAbsent("headerA", "A");
    map.putIfAbsent("headerB", "B");
    CompletableFuture<Object> hello = producer.asyncRequestBodyAndHeaders("direct:input", "hello", map);
    hello.thenAccept(x -> {
      assertThat(x).isEqualTo("OK !");
      responseReceived.set(true);
    });

    await().atMost(DEFAULT_TIMEOUT).untilAtomic(headersReceived, is(true));
    await().atMost(DEFAULT_TIMEOUT).untilAtomic(responseReceived, is(true));
  }
}

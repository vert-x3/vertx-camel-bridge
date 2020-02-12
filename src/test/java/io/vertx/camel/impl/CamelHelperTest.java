package io.vertx.camel.impl;

import io.vertx.core.eventbus.DeliveryOptions;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultMessage;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check the behavior of the {@link CamelHelper}
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class CamelHelperTest {

  @Test
  public void testTheCopyOfHeaders() {
    Message msg = new DefaultMessage(new DefaultCamelContext());
    msg.setHeader("CamelRedelivered", false);
    msg.setHeader("CamelRedeliveryCounter", 0);
    msg.setHeader("JMSCorrelationID", "");
    msg.setHeader("JMSDestination", "queue://dev.msy.queue.log.fwd");
    msg.setHeader("JMSReplyTo", null);

    DeliveryOptions options = CamelHelper.getDeliveryOptions(msg, true);

    assertThat(options.getHeaders().get("CamelRedelivered")).isEqualToIgnoringCase("false");
    assertThat(options.getHeaders().get("CamelRedeliveryCounter")).isEqualToIgnoringCase("0");
    assertThat(options.getHeaders().get("JMSCorrelationID")).isEqualToIgnoringCase("");
    assertThat(options.getHeaders().get("JMSDestination")).isEqualToIgnoringCase("queue://dev.msy.queue.log.fwd");
    assertThat(options.getHeaders().get("JMSReplyTo")).isNull();

  }

}

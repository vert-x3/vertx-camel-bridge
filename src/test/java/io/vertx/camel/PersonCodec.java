package io.vertx.camel;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class PersonCodec implements MessageCodec<Person, Person> {
  @Override
  public void encodeToWire(Buffer buffer, Person person) {
    String encoded = Json.encode(person);
    buffer.appendInt(encoded.length());
    buffer.appendString(encoded);
  }

  @Override
  public Person decodeFromWire(int pos, Buffer buffer) {
    int i = (int) buffer.getByte(pos);
    Buffer extracted = buffer.getBuffer(pos + 1, pos + i);
    return Json.decodeValue(extracted.toString(), Person.class);
  }

  @Override
  public Person transform(Person person) {
    return person;
  }

  @Override
  public String name() {
    return "person";
  }

  @Override
  public byte systemCodecID() {
    return -1;
  }
}

package io.vertx.ext.camel;

/**
 * Just some dummy object.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Person {

  private String name;

  public String getName() {
    return name;
  }

  public Person setName(String name) {
    this.name = name;
    return this;
  }
}

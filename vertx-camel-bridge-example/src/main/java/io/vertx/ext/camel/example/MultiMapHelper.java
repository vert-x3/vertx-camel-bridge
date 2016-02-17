package io.vertx.ext.camel.example;

import io.vertx.core.MultiMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class MultiMapHelper {

  public static Map<String, Object> toMap(MultiMap multiMap) {
    Map<String, Object> map = new LinkedHashMap<>();
    multiMap.names().stream().forEach(key -> {
      List<String> list = multiMap.getAll(key);
      map.put(key, list.size() == 1 ? list.get(0) : list);
    });
    return map;
  }

}

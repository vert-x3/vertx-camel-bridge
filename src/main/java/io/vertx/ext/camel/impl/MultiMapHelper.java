/*
 *  Copyright (c) 2011-2015 The original author or authors
 *  ------------------------------------------------------
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *       The Eclipse Public License is available at
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *       The Apache License v2.0 is available at
 *       http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.ext.camel.impl;

import io.vertx.core.MultiMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A helper class to transform multi-maps into maps
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class MultiMapHelper {

  /**
   * Transforms the given multimap to a map.
   * Scalar entries are put as they are in the resulting map.
   * Multiple entries are put a list in the resulting map.
   *
   * @param multiMap the multi-map, must not be {@code null}
   * @return the created map
   */
  public static Map<String, Object> toMap(MultiMap multiMap) {
    Map<String, Object> map = new LinkedHashMap<>();
    multiMap.names().stream().forEach(key -> {
      List<String> list = multiMap.getAll(key);
      map.put(key, list.size() == 1 ? list.get(0) : list);
    });
    return map;
  }

}

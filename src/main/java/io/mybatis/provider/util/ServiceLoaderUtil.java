/*
 * Copyright 2020-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.mybatis.provider.util;

import io.mybatis.provider.Order;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * SPI 工具类
 *
 * @author liuzh
 */
public class ServiceLoaderUtil {

  /**
   * 获取实现
   *
   * @param clazz
   * @param <T>
   * @return
   */
  public static <T> List<T> getInstances(Class<T> clazz) {
    //SPI获取所有实现类
    ServiceLoader<T> serviceLoader = ServiceLoader.load(clazz);
    List<T> instances = new ArrayList<>();
    for (T factory : serviceLoader) {
      instances.add(factory);
    }
    if (instances.size() > 1 && Order.class.isAssignableFrom(clazz)) {
      instances.sort(Comparator.comparing(f -> ((Order) f).getOrder()).reversed());
    }
    return instances;
  }

}

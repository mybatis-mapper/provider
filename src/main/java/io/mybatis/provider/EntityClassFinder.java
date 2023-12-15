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

package io.mybatis.provider;

import io.mybatis.provider.util.ServiceLoaderUtil;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 根据类型和方法等信息获取实体类类型，可以通过 SPI 方式替换默认实现
 *
 * @author liuzh
 */
public interface EntityClassFinder extends Order {
  /**
   * 缓存，避免方法执行时每次都查找
   */
  Map<MapperTypeMethod, Optional<Class<?>>> ENTITY_CLASS_MAP = new ConcurrentHashMap<>();

  /**
   * Mapper 接口和方法，用作缓存 Key
   */
  class MapperTypeMethod {
    private final Class<?> mapperType;
    private final Method   mapperMethod;

    public MapperTypeMethod(Class<?> mapperType, Method mapperMethod) {
      this.mapperType = mapperType;
      this.mapperMethod = mapperMethod;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      MapperTypeMethod that = (MapperTypeMethod) o;
      return Objects.equals(mapperType, that.mapperType) && Objects.equals(mapperMethod, that.mapperMethod);
    }

    @Override
    public int hashCode() {
      return Objects.hash(mapperType, mapperMethod);
    }

    @Override
    public String toString() {
      return (mapperType != null ? mapperType.getSimpleName() + "." : "") + (mapperMethod != null ? mapperMethod.getName() : "");
    }
  }

  /**
   * 查找当前方法对应的实体类
   *
   * @param mapperType   Mapper 接口，不能为空
   * @param mapperMethod Mapper 接口方法，可以为空
   * @return
   */
  static Optional<Class<?>> find(Class<?> mapperType, Method mapperMethod) {
    Objects.requireNonNull(mapperType);
    return ENTITY_CLASS_MAP.computeIfAbsent(new MapperTypeMethod(mapperType, mapperMethod),
        mapperTypeMethod -> {
          for (EntityClassFinder instance : EntityClassFinderInstance.getInstances()) {
            Optional<Class<?>> optionalClass = instance.findEntityClass(mapperType, mapperMethod);
            if (optionalClass.isPresent()) {
              return optionalClass;
            }
          }
          return Optional.empty();
        });
  }

  /**
   * 查找当前方法对应的实体类
   *
   * @param mapperType   Mapper 接口，不能为空
   * @param mapperMethod Mapper 接口方法，可以为空
   * @return 实体类类型
   */
  Optional<Class<?>> findEntityClass(Class<?> mapperType, Method mapperMethod);

  /**
   * 指定的类型是否为定义的实体类类型
   *
   * @param clazz 类型
   * @return 是否为实体类类型
   */
  boolean isEntityClass(Class<?> clazz);

  /**
   * 实例
   */
  class EntityClassFinderInstance {
    private static volatile List<EntityClassFinder> INSTANCES;

    /**
     * 通过 SPI 获取扩展的实现或使用默认实现
     *
     * @return 实例
     */
    public static List<EntityClassFinder> getInstances() {
      if (INSTANCES == null) {
        synchronized (EntityClassFinder.class) {
          if (INSTANCES == null) {
            INSTANCES = ServiceLoaderUtil.getInstances(EntityClassFinder.class);
          }
        }
      }
      return INSTANCES;
    }
  }

}

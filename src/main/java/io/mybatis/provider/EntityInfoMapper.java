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

import io.mybatis.provider.defaults.GenericTypeResolver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实体类信息接口，继承当前接口即可在接口中方便的获取当前接口对应的实体类类型 {@link Class}和实体表的信息 {@link EntityTable}
 *
 * @param <T> 实体类泛型
 * @author liuzh
 */
public interface EntityInfoMapper<T> {

  /**
   * 获取当前接口对应的实体类类型
   *
   * @return 当前接口对应的实体类类型
   */
  default Class<T> entityClass() {
    return (Class<T>) CachingEntityClass.getEntityClass(getClass());
  }

  /**
   * 获取当前接口对应的实体表信息
   *
   * @return 当前接口对应的实体表信息
   */
  default EntityTable entityTable() {
    return EntityFactory.create(entityClass());
  }

  /**
   * 缓存实体类类型
   */
  class CachingEntityClass {
    static Map<Class<?>, Class<?>> entityClassMap = new ConcurrentHashMap<>();

    /**
     * 获取接口对应的实体类类型
     *
     * @param clazz 继承的子接口
     * @return 实体类类型
     */
    private static Class<?> getEntityClass(Class<?> clazz) {
      if (!entityClassMap.containsKey(clazz)) {
        entityClassMap.put(clazz, (Class<?>) GenericTypeResolver.resolveType(
            EntityInfoMapper.class.getTypeParameters()[0], clazz, EntityInfoMapper.class));
      }
      return entityClassMap.get(clazz);
    }
  }

}

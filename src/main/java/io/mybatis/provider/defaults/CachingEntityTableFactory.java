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

package io.mybatis.provider.defaults;

import io.mybatis.provider.EntityTable;
import io.mybatis.provider.EntityTableFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支持缓存实体类信息的方法
 *
 * @author liuzh
 */
public class CachingEntityTableFactory implements EntityTableFactory {
  /**
   * 缓存实体类信息
   */
  private final Map<Class<?>, EntityTable> ENTITY_CLASS_MAP = new ConcurrentHashMap<>();

  @Override
  public EntityTable createEntityTable(Class<?> entityClass, Chain chain) {
    if (ENTITY_CLASS_MAP.get(entityClass) == null) {
      synchronized (entityClass) {
        if (ENTITY_CLASS_MAP.get(entityClass) == null) {
          EntityTable entityTable = chain.createEntityTable(entityClass);
          if (entityTable != null) {
            ENTITY_CLASS_MAP.put(entityClass, entityTable);
          } else {
            return null;
          }
        }
      }
    }
    return ENTITY_CLASS_MAP.get(entityClass);
  }

  @Override
  public int getOrder() {
    return Integer.MAX_VALUE;
  }
}

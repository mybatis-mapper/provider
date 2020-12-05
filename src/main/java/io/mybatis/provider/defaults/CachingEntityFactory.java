/*
 * Copyright 2020 the original author or authors.
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

import io.mybatis.provider.EntityColumn;
import io.mybatis.provider.EntityFactory;
import io.mybatis.provider.EntityField;
import io.mybatis.provider.EntityTable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支持缓存实体类信息的方法
 *
 * @author liuzh
 */
public class CachingEntityFactory extends EntityFactory {
  /**
   * 缓存实体类信息
   */
  private final Map<Class<?>, EntityTable> ENTITY_CLASS_MAP = new ConcurrentHashMap<>();

  public CachingEntityFactory(EntityFactory factory) {
    setNext(factory);
  }

  @Override
  public void assembleEntityColumns(EntityTable entityTable) {
    Class<?> entityClass = entityTable.entityClass();
    if (ENTITY_CLASS_MAP.get(entityClass) == null) {
      synchronized (entityClass) {
        if (ENTITY_CLASS_MAP.get(entityClass) == null) {
          next().assembleEntityColumns(entityTable);
          ENTITY_CLASS_MAP.put(entityClass, entityTable);
        }
      }
    }
  }

  @Override
  public EntityTable createEntityTable(Class<?> entityClass) {
    if (ENTITY_CLASS_MAP.get(entityClass) == null) {
      return next().createEntityTable(entityClass);
    }
    return ENTITY_CLASS_MAP.get(entityClass);
  }

  @Override
  public Optional<List<EntityColumn>> createEntityColumn(EntityField field) {
    return next().createEntityColumn(field);
  }

}

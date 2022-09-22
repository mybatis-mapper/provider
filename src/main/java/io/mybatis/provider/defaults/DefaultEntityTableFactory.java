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

import io.mybatis.provider.Entity;
import io.mybatis.provider.EntityTable;
import io.mybatis.provider.EntityTableFactory;
import io.mybatis.provider.Style;

/**
 * 默认实现，针对 {@link Entity.Table} 注解实现
 *
 * @author liuzh
 */
public class DefaultEntityTableFactory implements EntityTableFactory {

  @Override
  public EntityTable createEntityTable(Class<?> entityClass, Chain chain) {
    if (entityClass.isAnnotationPresent(Entity.Table.class)) {
      Entity.Table table = entityClass.getAnnotation(Entity.Table.class);
      EntityTable entityTable = EntityTable.of(entityClass)
          .table(table.value().isEmpty() ? Style.getStyle(table.style()).tableName(entityClass) : table.value())
          .style(table.style())
          .resultMap(table.resultMap())
          .autoResultMap(table.autoResultMap());
      for (Entity.Prop prop : table.props()) {
        entityTable.setProp(prop);
      }
      return entityTable;
    }
    return null;
  }

}

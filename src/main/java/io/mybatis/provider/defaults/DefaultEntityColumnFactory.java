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

import io.mybatis.provider.*;
import org.apache.ibatis.type.JdbcType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 默认实现，针对 {@link Entity.Column} 注解实现
 *
 * @author liuzh
 */
public class DefaultEntityColumnFactory implements EntityColumnFactory {

  @Override
  public Optional<List<EntityColumn>> createEntityColumn(EntityTable entityTable, EntityField field, Chain chain) {
    if (field.isAnnotationPresent(Entity.Column.class)) {
      Entity.Column column = field.getAnnotation(Entity.Column.class);
      EntityColumn entityColumn = EntityColumn.of(field)
          .column(column.value().isEmpty() ? Style.getStyle(entityTable.style()).columnName(entityTable, field) : column.value())
          .id(column.id())
          .orderBy(column.orderBy())
          .orderByPriority(column.orderByPriority())
          .selectable(column.selectable())
          .insertable(column.insertable())
          .updatable(column.updatable())
          .jdbcType(column.jdbcType())
          .typeHandler(column.typeHandler())
          .numericScale(column.numericScale());
      for (Entity.Prop prop : column.props()) {
        entityColumn.setProp(prop);
      }
      return Optional.of(Arrays.asList(entityColumn));
    } else if (!field.isAnnotationPresent(Entity.Transient.class)) {
      return Optional.of(Arrays.asList(EntityColumn.of(field)
          .column(Style.getStyle(entityTable.style()).columnName(entityTable, field))
          .numericScale("")
          .jdbcType(JdbcType.UNDEFINED)));
    }
    return Optional.empty();
  }

}

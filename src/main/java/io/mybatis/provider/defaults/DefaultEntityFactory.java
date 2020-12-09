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

import io.mybatis.provider.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 默认实现，针对 {@link Entity} 注解实现
 *
 * @author liuzh
 */
public class DefaultEntityFactory extends EntityFactory {

  @Override
  public EntityTable createEntityTable(Class<?> entityClass) {
    return new EntityTable(tableName(entityClass), entityClass);
  }

  @Override
  public void assembleEntityColumns(EntityTable entityTable) {
    Class<?> entityClass = entityTable.entityClass();
    Class<?> declaredClass = entityClass;
    boolean isSuperclass = false;
    while (declaredClass != null && declaredClass != Object.class) {
      Field[] declaredFields = declaredClass.getDeclaredFields();
      if (isSuperclass) {
        reverse(declaredFields);
      }
      for (Field field : declaredFields) {
        EntityField entityField = new EntityField(entityClass, field);
        Optional<List<EntityColumn>> optionalEntityColumns = getWrapper().createEntityColumn(entityField);
        optionalEntityColumns.ifPresent(columns -> columns.forEach(entityTable::addColumn));
      }
      //迭代获取父类
      declaredClass = declaredClass.getSuperclass();
      isSuperclass = true;
    }
  }

  /**
   * 获取实体对应的表名
   *
   * @param entityClass 实体类类型
   * @return 表名
   */
  private String tableName(Class<?> entityClass) {
    String tableName = null;
    if (entityClass.isAnnotationPresent(Entity.Table.class)) {
      Entity.Table name = entityClass.getAnnotation(Entity.Table.class);
      tableName = name.value();
    }
    if (tableName == null || tableName.isEmpty()) {
      tableName = entityClass.getSimpleName();
    }
    return tableName;
  }

  /**
   * 反转排序
   *
   * @param array 数组
   */
  private void reverse(Object[] array) {
    for (int i = 0; i < array.length / 2; i++) {
      Object temp = array[i];
      array[i] = array[array.length - i - 1];
      array[array.length - i - 1] = temp;
    }
  }

  @Override
  public Optional<List<EntityColumn>> createEntityColumn(EntityField field) {
    if (field.isAnnotationPresent(Entity.Column.class)) {
      Entity.Column column = field.getAnnotation(Entity.Column.class);
      String columnName = column.value();
      if (columnName.isEmpty()) {
        columnName = field.getName();
      }
      return Optional.of(Arrays.asList(new EntityColumn(field, columnName, column.id())));
    }
    return Optional.empty();
  }

}

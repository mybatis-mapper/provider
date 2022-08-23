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

import io.mybatis.provider.defaults.DefaultEntityColumnFactoryChain;
import io.mybatis.provider.defaults.DefaultEntityTableFactoryChain;
import io.mybatis.provider.util.ServiceLoaderUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;

/**
 * 实体类信息工厂
 *
 * @author liuzh
 */
public abstract class EntityFactory {

  /**
   * 获取类型对应的实体信息
   *
   * @param mapperType   接口
   * @param mapperMethod 方法
   * @return 实体类信息
   */
  public static EntityTable create(Class<?> mapperType, Method mapperMethod) {
    Optional<Class<?>> optionalClass = EntityClassFinder.find(mapperType, mapperMethod);
    if (optionalClass.isPresent()) {
      return create(optionalClass.get());
    }
    throw new RuntimeException("Can't obtain " + (mapperMethod != null ?
        mapperMethod.getName() + " method" : mapperType.getSimpleName() + " interface") + " corresponding entity class");
  }

  /**
   * 获取类型对应的实体信息
   *
   * @param entityClass 实体类类型
   * @return 实体类信息
   */
  public static EntityTable create(Class<?> entityClass) {
    //处理EntityTable
    EntityTableFactory.Chain entityTableFactoryChain = Instance.getEntityTableFactoryChain();
    //创建 EntityTable，不处理列（字段），此时返回的 EntityTable 已经经过了所有处理链的加工
    EntityTable entityTable = entityTableFactoryChain.createEntityTable(entityClass);
    if (entityTable == null) {
      throw new NullPointerException("Unable to get " + entityClass.getName() + " entity class information");
    }
    //如果实体表已经处理好，直接返回
    if (!entityTable.ready()) {
      synchronized (entityClass) {
        if (!entityTable.ready()) {
          //处理EntityColumn
          EntityColumnFactory.Chain entityColumnFactoryChain = Instance.getEntityColumnFactoryChain();
          //未处理的需要获取字段
          Class<?> declaredClass = entityClass;
          boolean isSuperclass = false;
          while (declaredClass != null && declaredClass != Object.class) {
            Field[] declaredFields = declaredClass.getDeclaredFields();
            if (isSuperclass) {
              reverse(declaredFields);
            }
            for (Field field : declaredFields) {
              int modifiers = field.getModifiers();
              //排除 static 和 transient 修饰的字段
              if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
                EntityField entityField = new EntityField(entityClass, field);
                Optional<List<EntityColumn>> optionalEntityColumns = entityColumnFactoryChain.createEntityColumn(entityTable, entityField);
                optionalEntityColumns.ifPresent(columns -> columns.forEach(entityTable::addColumn));
              }
            }
            //迭代获取父类
            declaredClass = declaredClass.getSuperclass();
            isSuperclass = true;
          }
          //标记处理完成
          entityTable.ready(true);
        }
      }
    }
    return entityTable;
  }

  /**
   * 反转排序
   *
   * @param array 数组
   */
  protected static void reverse(Object[] array) {
    for (int i = 0; i < array.length / 2; i++) {
      Object temp = array[i];
      array[i] = array[array.length - i - 1];
      array[array.length - i - 1] = temp;
    }
  }

  /**
   * 实例
   */
  static class Instance {
    private static volatile EntityTableFactory.Chain  entityTableFactoryChain;
    private static volatile EntityColumnFactory.Chain entityColumnFactoryChain;

    /**
     * 获取处理实体的工厂链
     *
     * @return 实例
     */
    public static EntityTableFactory.Chain getEntityTableFactoryChain() {
      if (entityTableFactoryChain == null) {
        synchronized (EntityFactory.class) {
          if (entityTableFactoryChain == null) {
            List<EntityTableFactory> entityTableFactories = ServiceLoaderUtil.getInstances(EntityTableFactory.class);
            entityTableFactoryChain = new DefaultEntityTableFactoryChain(entityTableFactories);
          }
        }
      }
      return entityTableFactoryChain;
    }

    /**
     * 获取处理列的工厂链
     *
     * @return 实例
     */
    public static EntityColumnFactory.Chain getEntityColumnFactoryChain() {
      if (entityColumnFactoryChain == null) {
        synchronized (EntityFactory.class) {
          if (entityColumnFactoryChain == null) {
            List<EntityColumnFactory> entityColumnFactories = ServiceLoaderUtil.getInstances(EntityColumnFactory.class);
            entityColumnFactoryChain = new DefaultEntityColumnFactoryChain(entityColumnFactories);
          }
        }
      }
      return entityColumnFactoryChain;
    }
  }

}

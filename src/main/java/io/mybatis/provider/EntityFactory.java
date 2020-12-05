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

package io.mybatis.provider;

import io.mybatis.provider.defaults.CachingEntityFactory;
import io.mybatis.provider.defaults.DefaultEntityFactory;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 实体类信息工厂，可以通过 SPI 加入处理链
 */
public abstract class EntityFactory {
  /**
   * 默认顺序，最低优先级（最后执行）
   */
  public static int           DEFAULT_ORDER = 0;
  /**
   * 下一个执行的工厂方法（责任链模式）
   */
  private       EntityFactory next;

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
    throw new RuntimeException("无法获取 " + (mapperMethod != null ?
        mapperMethod.getName() + " 方法" : mapperType.getSimpleName() + " 接口") + "对应的实体类");
  }

  /**
   * 获取类型对应的实体信息
   *
   * @param entityClass 实体类类型
   * @return 实体类信息
   */
  public static EntityTable create(Class<?> entityClass) {
    EntityFactory factory = EntityFactoryInstance.getInstance();
    //创建 EntityTable，不处理列（字段），此时返回的 EntityTable 已经经过了所有处理链的加工
    EntityTable entityTable = factory.createEntityTable(entityClass);
    //处理 EntityTable 中的所有列
    factory.assembleEntityColumns(entityTable);
    return entityTable;
  }

  /**
   * 创建列信息
   *
   * @param field 字段信息
   * @return 实体类中列的信息
   */
  public static Optional<List<EntityColumn>> create(EntityField field) {
    return EntityFactoryInstance.getInstance().createEntityColumn(field);
  }

  /**
   * @return 执行顺序（想象多个同心环，顺序号越小，执行越靠内，越大越靠外）
   */
  public int getOrder() {
    return DEFAULT_ORDER;
  }

  /**
   * 设置下一个处理器
   *
   * @param next 下一个处理器
   */
  protected void setNext(EntityFactory next) {
    this.next = next;
  }

  /**
   * 获取下一个处理器
   *
   * @return 下一个处理器
   */
  public EntityFactory next() {
    return this.next;
  }

  /**
   * 根据实体类创建 EntityTable，可以使用自己的注解来实现，这一步只返回 EntityTable，不处理其中的字段
   *
   * @param entityClass 实体类类型
   * @return 实体类信息
   */
  public abstract EntityTable createEntityTable(Class<?> entityClass);

  /**
   * 生成实体表中的字段信息
   *
   * @param entityTable 实体类信息
   */
  public abstract void assembleEntityColumns(EntityTable entityTable);

  /**
   * 创建列信息，一个字段可能不是列，也可能是列，还有可能对应多个列（例如 ValueObject对象）
   *
   * @param field 字段信息
   * @return 实体类中列的信息，如果返回空，则不属于实体中的列
   */
  public abstract Optional<List<EntityColumn>> createEntityColumn(EntityField field);

  /**
   * 实例
   */
  static class EntityFactoryInstance {
    private static volatile EntityFactory INSTANCE;

    /**
     * 通过 SPI 获取扩展的实现或使用默认实现
     *
     * @return 实例
     */
    public static EntityFactory getInstance() {
      if (INSTANCE == null) {
        synchronized (EntityFactory.class) {
          if (INSTANCE == null) {
            //SPI获取所有实现类
            ServiceLoader<EntityFactory> factoryServiceLoader = ServiceLoader.load(EntityFactory.class);
            List<EntityFactory> factories = new ArrayList<>();
            for (EntityFactory factory : factoryServiceLoader) {
              factories.add(factory);
            }
            //没有发现SPI方式的实现类时，直接使用默认的实现
            if (factories.isEmpty()) {
              INSTANCE = new DefaultEntityFactory();
            } else {
              //SPI实现类存在时，先倒序排列（序号越大越早执行）
              factories.sort(Comparator.comparing(EntityFactory::getOrder).reversed());
              for (int i = 0; i < factories.size() - 1; i++) {
                //设置 next 链
                factories.get(i).setNext(factories.get(i + 1));
              }
              //将默认实现放到最后一个处理链中
              factories.get(factories.size() - 1).setNext(new DefaultEntityFactory());
              //返回链头作为示例
              INSTANCE = factories.get(0);
            }
            //增加缓存功能
            INSTANCE = new CachingEntityFactory(INSTANCE);
          }
        }
      }
      return INSTANCE;
    }
  }
}

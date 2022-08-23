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

import java.util.List;

/**
 * 实体类工厂处理链，支持单例，线程安全
 *
 * @author liuzh
 */
public class DefaultEntityTableFactoryChain implements EntityTableFactory.Chain {
  private final List<EntityTableFactory>       factories;
  private final DefaultEntityTableFactoryChain next;
  private final int                            index;

  public DefaultEntityTableFactoryChain(List<EntityTableFactory> factories) {
    this(factories, 0);
  }

  private DefaultEntityTableFactoryChain(List<EntityTableFactory> factories, int index) {
    this.factories = factories;
    this.index = index;
    if (this.index < this.factories.size()) {
      this.next = new DefaultEntityTableFactoryChain(factories, this.index + 1);
    } else {
      this.next = null;
    }
  }

  @Override
  public EntityTable createEntityTable(Class<?> entityClass) {
    if (index < factories.size()) {
      return factories.get(index).createEntityTable(entityClass, next);
    }
    return null;
  }

}

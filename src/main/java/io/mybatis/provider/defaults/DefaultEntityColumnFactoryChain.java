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

import io.mybatis.provider.EntityColumn;
import io.mybatis.provider.EntityColumnFactory;
import io.mybatis.provider.EntityField;
import io.mybatis.provider.EntityTable;

import java.util.List;
import java.util.Optional;

/**
 * 列工厂处理链，支持单例，线程安全
 *
 * @author liuzh
 */
public class DefaultEntityColumnFactoryChain implements EntityColumnFactory.Chain {
  private final List<EntityColumnFactory>       factories;
  private final DefaultEntityColumnFactoryChain next;
  private final int                             index;

  public DefaultEntityColumnFactoryChain(List<EntityColumnFactory> factories) {
    this(factories, 0);
  }

  private DefaultEntityColumnFactoryChain(List<EntityColumnFactory> factories, int index) {
    this.factories = factories;
    this.index = index;
    if (this.index < this.factories.size()) {
      this.next = new DefaultEntityColumnFactoryChain(factories, this.index + 1);
    } else {
      this.next = null;
    }
  }

  @Override
  public Optional<List<EntityColumn>> createEntityColumn(EntityTable entityTable, EntityField field) {
    if (index < factories.size()) {
      return factories.get(index).createEntityColumn(entityTable, field, next);
    }
    return Optional.empty();
  }

}

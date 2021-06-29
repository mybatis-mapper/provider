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
  private final List<EntityColumnFactory>       entityColumnFactories;
  private final DefaultEntityColumnFactoryChain next;
  private final int                             index;

  public DefaultEntityColumnFactoryChain(List<EntityColumnFactory> entityColumnFactories) {
    this(entityColumnFactories, 0);
  }

  private DefaultEntityColumnFactoryChain(List<EntityColumnFactory> entityColumnFactories, int index) {
    this.entityColumnFactories = entityColumnFactories;
    this.index = index;
    if (this.index < this.entityColumnFactories.size()) {
      this.next = new DefaultEntityColumnFactoryChain(entityColumnFactories, this.index + 1);
    } else {
      this.next = null;
    }
  }

  @Override
  public Optional<List<EntityColumn>> createEntityColumn(EntityTable entityTable, EntityField field) {
    if (index < entityColumnFactories.size()) {
      return entityColumnFactories.get(index).createEntityColumn(entityTable, field, next);
    }
    return null;
  }

}
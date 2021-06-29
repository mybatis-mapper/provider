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
  private final List<EntityTableFactory>       entityTableFactories;
  private final DefaultEntityTableFactoryChain next;
  private final int                            index;

  public DefaultEntityTableFactoryChain(List<EntityTableFactory> entityTableFactories) {
    this(entityTableFactories, 0);
  }

  private DefaultEntityTableFactoryChain(List<EntityTableFactory> entityTableFactories, int index) {
    this.entityTableFactories = entityTableFactories;
    this.index = index;
    if (this.index < this.entityTableFactories.size()) {
      this.next = new DefaultEntityTableFactoryChain(entityTableFactories, this.index + 1);
    } else {
      this.next = null;
    }
  }

  @Override
  public EntityTable createEntityTable(Class<?> entityClass) {
    if (index < entityTableFactories.size()) {
      return entityTableFactories.get(index).createEntityTable(entityClass, next);
    }
    return null;
  }

}
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
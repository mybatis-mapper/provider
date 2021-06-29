package io.mybatis.provider;

import io.mybatis.provider.defaults.DefaultEntityTableFactoryChain;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChainTest {

  public static class ATable extends EntityTable {
    EntityTable delegate;

    public ATable(Class<?> entityClass, EntityTable delegate) {
      super(entityClass);
      this.delegate = delegate;
    }
  }

  public static class A implements EntityTableFactory {
    @Override
    public EntityTable createEntityTable(Class<?> entityClass, Chain chain) {
      return new ATable(entityClass, chain.createEntityTable(entityClass));
    }

    @Override
    public int getOrder() {
      return 10;
    }
  }

  public static class BTable extends EntityTable {
    EntityTable delegate;

    public BTable(Class<?> entityClass, EntityTable delegate) {
      super(entityClass);
      this.delegate = delegate;
    }
  }

  public static class B implements EntityTableFactory {
    @Override
    public EntityTable createEntityTable(Class<?> entityClass, Chain chain) {
      return new BTable(entityClass, chain.createEntityTable(entityClass));
    }

    @Override
    public int getOrder() {
      return 20;
    }
  }

  public static class CTable extends EntityTable {
    EntityTable delegate;

    public CTable(Class<?> entityClass, EntityTable delegate) {
      super(entityClass);
      this.delegate = delegate;
    }
  }

  public static class C implements EntityTableFactory {
    @Override
    public EntityTable createEntityTable(Class<?> entityClass, Chain chain) {
      return new CTable(entityClass, chain.createEntityTable(entityClass));
    }

    @Override
    public int getOrder() {
      return 30;
    }
  }

  public static class User {
  }

  @Test
  public void test() {
    List<EntityTableFactory> factories = new ArrayList<>();
    factories.add(new A());
    factories.add(new B());
    factories.add(new C());
    factories.sort(Comparator.comparing(EntityTableFactory::getOrder).reversed());
    DefaultEntityTableFactoryChain chain = new DefaultEntityTableFactoryChain(factories);
    EntityTable entityTable = chain.createEntityTable(User.class);
    Assert.assertTrue(entityTable instanceof CTable);
    Assert.assertTrue(((CTable) entityTable).delegate instanceof BTable);
    Assert.assertTrue(((BTable) ((CTable) entityTable).delegate).delegate instanceof ATable);
  }

}

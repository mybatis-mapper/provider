package io.mybatis.provider;

/**
 * 实体类信息工厂，可以通过 SPI 加入处理链
 *
 * @author liuzh
 */
public interface EntityTableFactory extends Order {

  /**
   * 根据实体类创建 EntityTable，可以使用自己的注解来实现，这一步只返回 EntityTable，不处理其中的字段
   *
   * @param entityClass 实体类类型
   * @param chain       调用下一个
   * @return 实体类信息
   */
  EntityTable createEntityTable(Class<?> entityClass, Chain chain);

  /**
   * 工厂链
   */
  interface Chain {
    /**
     * 根据实体类创建 EntityTable，可以使用自己的注解来实现，这一步只返回 EntityTable，不处理其中的字段
     *
     * @param entityClass 实体类类型
     * @return 实体类信息
     */
    EntityTable createEntityTable(Class<?> entityClass);
  }

}

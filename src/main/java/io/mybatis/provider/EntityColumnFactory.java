package io.mybatis.provider;

import java.util.List;
import java.util.Optional;

/**
 * 实体类信息工厂，可以通过 SPI 加入处理链
 *
 * @author liuzh
 */
public interface EntityColumnFactory extends Order {

  /**
   * 创建列信息，一个字段可能不是列，也可能是列，还有可能对应多个列（例如 ValueObject对象）
   *
   * @param entityTable
   * @param field       字段信息
   * @param chain       调用下一个
   * @return 实体类中列的信息，如果返回空，则不属于实体中的列
   */
  Optional<List<EntityColumn>> createEntityColumn(EntityTable entityTable, EntityField field, Chain chain);

  /**
   * 工厂链
   */
  interface Chain {
    /**
     * 创建列信息，一个字段可能不是列，也可能是列，还有可能对应多个列（例如 ValueObject对象）
     *
     * @param entityTable
     * @param field       字段信息
     * @return 实体类中列的信息，如果返回空，则不属于实体中的列
     */
    Optional<List<EntityColumn>> createEntityColumn(EntityTable entityTable, EntityField field);
  }

}

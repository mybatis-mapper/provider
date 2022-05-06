package io.mybatis.provider.style;

import io.mybatis.provider.EntityField;
import io.mybatis.provider.EntityTable;

/**
 * @author liuzh
 */
public class LowerStyle extends NormalStyle {
  @Override
  public String getStyle() {
    return LOWER;
  }

  @Override
  public String tableName(Class<?> entityClass) {
    return super.tableName(entityClass).toLowerCase();
  }

  @Override
  public String columnName(EntityTable entityTable, EntityField field) {
    return super.columnName(entityTable, field).toLowerCase();
  }

}

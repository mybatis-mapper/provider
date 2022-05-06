package io.mybatis.provider.style;

import io.mybatis.provider.EntityField;
import io.mybatis.provider.EntityTable;

/**
 * @author liuzh
 */
public class UpperUnderscoreStyle extends LowerUnderscoreStyle {
  @Override
  public String getStyle() {
    return UPPER_UNDERSCORE;
  }

  @Override
  public String tableName(Class<?> entityClass) {
    return super.tableName(entityClass).toUpperCase();
  }

  @Override
  public String columnName(EntityTable entityTable, EntityField field) {
    return super.columnName(entityTable, field).toUpperCase();
  }

}

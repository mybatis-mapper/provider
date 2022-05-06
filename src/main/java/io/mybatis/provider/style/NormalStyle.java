package io.mybatis.provider.style;

import io.mybatis.provider.EntityField;
import io.mybatis.provider.EntityTable;
import io.mybatis.provider.Style;

/**
 * @author liuzh
 */
public class NormalStyle implements Style {
  @Override
  public String getStyle() {
    return NORMAL;
  }

  @Override
  public String tableName(Class<?> entityClass) {
    return entityClass.getSimpleName();
  }

  @Override
  public String columnName(EntityTable entityTable, EntityField field) {
    return field.getName();
  }

}

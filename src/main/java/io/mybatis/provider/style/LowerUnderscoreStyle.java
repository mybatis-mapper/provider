package io.mybatis.provider.style;

import io.mybatis.provider.EntityField;
import io.mybatis.provider.EntityTable;

/**
 * @author liuzh
 */
public class LowerUnderscoreStyle extends NormalStyle {
  /**
   * 将驼峰风格替换为下划线风格
   */
  public static String camelhumpToUnderline(String str) {
    final int size;
    final char[] chars;
    final StringBuilder sb = new StringBuilder(
        (size = (chars = str.toCharArray()).length) * 3 / 2 + 1);
    char c;
    for (int i = 0; i < size; i++) {
      c = chars[i];
      if (Character.isUpperCase(c)) {
        sb.append('_').append(Character.toLowerCase(c));
      } else {
        sb.append(c);
      }
    }
    return sb.charAt(0) == '_' ? sb.substring(1) : sb.toString();
  }

  @Override
  public String getStyle() {
    return LOWER_UNDERSCORE;
  }

  @Override
  public String tableName(Class<?> entityClass) {
    return camelhumpToUnderline(super.tableName(entityClass));
  }

  @Override
  public String columnName(EntityTable entityTable, EntityField field) {
    return camelhumpToUnderline(super.columnName(entityTable, field));
  }

}

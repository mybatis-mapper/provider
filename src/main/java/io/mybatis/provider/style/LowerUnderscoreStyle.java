/*
 * Copyright 2020-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

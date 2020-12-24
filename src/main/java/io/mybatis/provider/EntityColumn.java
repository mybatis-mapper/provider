/*
 * Copyright 2020 the original author or authors.
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

package io.mybatis.provider;

import java.util.Objects;

/**
 * 实体中字段和列的对应关系接口，记录字段上提供的列信息
 *
 * @author liuzh
 */
public class EntityColumn extends Delegate<EntityColumn> {
  /**
   * 列名
   */
  protected String      column;
  /**
   * 实体类字段
   */
  protected EntityField field;
  /**
   * 是否为主键
   */
  protected boolean     id;
  /**
   * 所在实体类
   */
  protected EntityTable entityTable;

  public EntityColumn(EntityColumn delegate) {
    super(delegate);
  }

  public EntityColumn(EntityField field, String column, boolean id) {
    super(null);
    if (column == null || column.isEmpty()) {
      throw new NullPointerException("The column name cannot be empty");
    }
    if (field == null) {
      throw new NullPointerException("The column corresponding to the Java field cannot be empty");
    }
    this.column = column;
    this.field = field;
    this.id = id;
  }

  //<editor-fold desc="基础方法，这部分方法需要考虑代理形式的用法">

  /**
   * 是否为主键
   */
  public boolean isId() {
    return delegate != null ? delegate.isId() : id;
  }

  /**
   * 列名
   */
  public String column() {
    return delegate != null ? delegate.column() : column;
  }

  /**
   * Java 字段
   */
  public EntityField field() {
    return delegate != null ? delegate.field() : field;
  }

  /**
   * 所在实体
   */
  public EntityTable entityTable() {
    return delegate != null ? delegate.entityTable() : entityTable;
  }

  /**
   * 设置所属实体
   */
  public void setEntityTable(EntityTable entityTable) {
    if (delegate != null) {
      this.delegate.setEntityTable(entityTable);
    } else {
      this.entityTable = entityTable;
    }
  }
  //</editor-fold>

  //<editor-fold desc="根据上面基础方法就能直接实现的默认方法">

  /**
   * Java 类型
   */
  public Class<?> javaType() {
    return field().getType();
  }

  /**
   * 属性名
   */
  public String property() {
    return property("");
  }

  /**
   * 带指定前缀的属性名
   *
   * @param prefix 指定前缀，需要自己提供"."
   */
  public String property(String prefix) {
    return prefix + field().getName();
  }

  /**
   * 返回 xml 变量形式 #{property}
   */
  public String variables() {
    return variables("");
  }

  /**
   * 返回带前缀的 xml 变量形式 #{prefixproperty}
   *
   * @param prefix 指定前缀，需要自己提供"."
   */
  public String variables(String prefix) {
    return "#{" + property(prefix) + "}";
  }

  /**
   * 返回 column AS property 形式的字符串, 当 column 和 property 相同时没有别名
   */
  public String columnAsProperty() {
    return columnAsProperty("");
  }

  /**
   * 返回 column AS prefixproperty 形式的字符串
   *
   * @param prefix 指定前缀，需要自己提供"."
   */
  public String columnAsProperty(String prefix) {
    if (!Objects.equals(column(), property(prefix))) {
      return column() + " AS " + property(prefix);
    }
    return column();
  }

  /**
   * 返回 column = #{property} 形式的字符串
   */
  public String columnEqualsProperty() {
    return columnEqualsProperty("");
  }

  /**
   * 返回带前缀的 column = #{prefixproperty} 形式的字符串
   *
   * @param prefix 指定前缀，需要自己提供"."
   */
  public String columnEqualsProperty(String prefix) {
    return column() + " = " + variables(prefix);
  }

  /**
   * 返回 property != null 形式的字符串
   */
  public String notNullTest() {
    return notNullTest("");
  }

  /**
   * 返回带前缀的  prefixproperty != null 形式的字符串
   *
   * @param prefix 指定前缀，需要自己提供"."
   */
  public String notNullTest(String prefix) {
    return property(prefix) + " != null";
  }

  /**
   * 当字段类型为 String 时，返回 property != null and property != '' 形式的字符串.
   * 其他类型时和 {@link #notNullTest()} 方法一样.
   */
  public String notEmptyTest() {
    return notEmptyTest("");
  }

  /**
   * 当字段类型为 String 时，返回 prefixproperty != null and prefixproperty != '' 形式的字符串.
   * 其他类型时和 {@link #notNullTest()} 方法一样.
   *
   * @param prefix 指定前缀，需要自己提供"."
   */
  public String notEmptyTest(String prefix) {
    if (field().getType() == String.class) {
      return notNullTest(prefix) + " and " + property(prefix) + " != '' ";
    }
    return notNullTest();
  }
  //</editor-fold>

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof EntityColumn)) return false;
    EntityColumn that = (EntityColumn) o;
    return column().equals(that.column());
  }

  @Override
  public int hashCode() {
    return Objects.hash(column());
  }

  @Override
  public String toString() {
    return columnEqualsProperty();
  }

}

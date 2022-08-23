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

package io.mybatis.provider;

import io.mybatis.provider.util.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.UnknownTypeHandler;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;

import static io.mybatis.provider.EntityTable.DELIMITER;

/**
 * 实体中字段和列的对应关系接口，记录字段上提供的列信息
 *
 * @author liuzh
 */
@Accessors(fluent = true)
public class EntityColumn extends EntityProps<EntityColumn> {
  /**
   * 实体类字段
   */
  @Getter
  protected final EntityField                  field;
  /**
   * 所在实体类
   */
  @Getter
  @Setter
  protected       EntityTable                  entityTable;
  /**
   * 列名
   */
  @Getter
  @Setter
  protected       String                       column;
  /**
   * 是否为主键
   */
  @Getter
  @Setter
  protected       boolean                      id;
  /**
   * 排序方式
   */
  @Getter
  @Setter
  protected       String                       orderBy;
  /**
   * 排序的优先级，数值越小优先级越高
   */
  @Getter
  @Setter
  protected       int                          orderByPriority;
  /**
   * 是否查询字段
   */
  @Getter
  @Setter
  protected       boolean                      selectable = true;
  /**
   * 是否插入字段
   */
  @Getter
  @Setter
  protected       boolean                      insertable = true;
  /**
   * 是否更新字段
   */
  @Getter
  @Setter
  protected       boolean                      updatable  = true;
  /**
   * jdbc类型
   */
  @Getter
  @Setter
  protected       JdbcType                     jdbcType;
  /**
   * 类型处理器
   */
  @Getter
  @Setter
  protected       Class<? extends TypeHandler> typeHandler;
  /**
   * 精度
   */
  @Getter
  @Setter
  protected       String                       numericScale;

  //<editor-fold desc="根据上面基础方法就能直接实现的默认方法">

  protected EntityColumn(EntityField field) {
    this.field = field;
  }

  public static EntityColumn of(EntityField field) {
    return new EntityColumn(field);
  }

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
    return "#{" + property(prefix)
        + jdbcTypeVariables().orElse("")
        + typeHandlerVariables().orElse("")
        + numericScaleVariables().orElse("") + "}";
  }

  /**
   * 数据库类型 {, jdbcType=VARCHAR}
   */
  public Optional<String> jdbcTypeVariables() {
    if (this.jdbcType != null && this.jdbcType != JdbcType.UNDEFINED) {
      return Optional.of(", jdbcType=" + jdbcType);
    }
    return Optional.empty();
  }

  /**
   * 类型处理器 {, typeHandler=XXTypeHandler}
   */
  public Optional<String> typeHandlerVariables() {
    if (this.typeHandler != null && this.typeHandler != UnknownTypeHandler.class) {
      return Optional.of(", typeHandler=" + typeHandler.getName());
    }
    return Optional.empty();
  }

  /**
   * 小数位数 {, numericScale=2}
   */
  public Optional<String> numericScaleVariables() {
    if (Utils.isNotEmpty(this.numericScale)) {
      return Optional.of(", numericScale=" + numericScale);
    }
    return Optional.empty();
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
    // 这里的column 和 property 的比较 应该是需要忽略界定符之后再比较
    // eg: mysql 中 【`order`】 应该认为是 和 field 的 【order】 相同
    String column = column();
    Matcher matcher = DELIMITER.matcher(column());
    if (matcher.find()) {
      column = matcher.group(1);
    }
    if (!Objects.equals(column, property(prefix))) {
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

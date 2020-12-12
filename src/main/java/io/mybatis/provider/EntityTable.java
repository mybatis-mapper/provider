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

import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.session.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 实体表接口，记录实体和表的关系
 *
 * @author liuzh
 */
public class EntityTable extends Delegate<EntityTable> {
  /**
   * 表名
   */
  protected String             table;
  /**
   * 实体类
   */
  protected Class<?>           entityClass;
  /**
   * 字段信息
   */
  protected List<EntityColumn> columns;
  //<editor-fold desc="基础方法，必须实现的方法">

  public EntityTable(EntityTable delegate) {
    super(delegate);
  }

  /**
   * 构造方法
   *
   * @param table       表名
   * @param entityClass 类型
   */
  public EntityTable(String table, Class<?> entityClass) {
    super(null);
    this.table = table;
    this.entityClass = entityClass;
  }

  /**
   * 表名
   */
  public String table() {
    return delegate != null ? delegate.table() : table;
  }

  /**
   * 实体类型
   */
  public Class<?> entityClass() {
    return delegate != null ? delegate.entityClass() : entityClass;
  }

  /**
   * 返回所有列
   *
   * @return 所有列信息
   */
  public List<EntityColumn> columns() {
    if (delegate != null) {
      return delegate.columns();
    }
    if (this.columns == null) {
      this.columns = new ArrayList<>();
    }
    return columns;
  }

  /**
   * 返回所有字段
   *
   * @return 所有字段
   */
  public List<EntityField> fields() {
    return columns().stream().map(EntityColumn::field).collect(Collectors.toList());
  }

  /**
   * 返回所有列名
   *
   * @return 所有列名
   */
  public List<String> columnNames() {
    return columns().stream().map(EntityColumn::column).collect(Collectors.toList());
  }

  /**
   * 返回所有属性名
   *
   * @return 所有属性名
   */
  public List<String> properties() {
    return columns().stream().map(EntityColumn::property).collect(Collectors.toList());
  }

  /**
   * 添加列
   */
  public void addColumn(EntityColumn column) {
    //不重复添加同名的列
    if (!columns().contains(column)) {
      if (column.field().getDeclaringClass() != entityClass()) {
        columns().add(0, column);
      } else {
        columns().add(column);
      }
      column.setEntityTable(this);
    } else {
      //同名列在父类存在时，说明是子类覆盖的，字段的顺序应该更靠前
      EntityColumn existsColumn = columns().remove(columns().indexOf(column));
      columns().add(0, existsColumn);
    }
  }

  /**
   * 设置运行时信息，不同方法分别执行一次，需要保证幂等
   *
   * @param configuration   MyBatis 配置类，慎重操作
   * @param providerContext 当前方法信息
   * @param cacheKey        缓存 key，每个方法唯一，默认和 msId 一样
   */
  public void initRuntimeContext(Configuration configuration, ProviderContext providerContext, String cacheKey) {
    if (delegate != null) {
      delegate.initRuntimeContext(configuration, providerContext, cacheKey);
    }
  }
  //</editor-fold>

  //<editor-fold desc="根据基础方法能直接实现的默认方法，实现方法时要避免破坏接口间的调用关系">

  /**
   * 返回主键列，不会为空，当根据主键作为条件时，必须使用当前方法返回的列，没有设置主键时，当前方法返回所有列
   */
  public List<EntityColumn> idColumns() {
    List<EntityColumn> idColumns = columns().stream().filter(EntityColumn::isId).collect(Collectors.toList());
    if (idColumns.isEmpty()) {
      return columns();
    }
    return idColumns;
  }

  /**
   * 返回普通列，排除主键字段，当根据非主键作为条件时，必须使用当前方法返回的列
   */
  public List<EntityColumn> normalColumns() {
    return columns().stream().filter(column -> !column.isId()).collect(Collectors.toList());
  }

  /**
   * 返回查询列，默认所有列，当获取查询列时，必须使用当前方法返回的列
   */
  public List<EntityColumn> selectColumns() {
    return columns();
  }

  /**
   * 返回查询列，默认所有列，当使用查询条件列时，必须使用当前方法返回的列
   */
  public List<EntityColumn> whereColumns() {
    return columns();
  }

  /**
   * 所有 insert 用到的字段，默认全部字段，当插入列时，必须使用当前方法返回的列
   */
  public List<EntityColumn> insertColumns() {
    return columns();
  }

  /**
   * 所有 update 用到的字段，默认全部字段，当更新列时，必须使用当前方法返回的列
   */
  public List<EntityColumn> updateColumns() {
    return columns();
  }

  /**
   * 所有 GROUP BY 到的字段，默认为空，当使用 GROUP BY 列时，必须使用当前方法返回的列
   */
  public Optional<List<EntityColumn>> groupByColumns() {
    return Optional.empty();
  }

  /**
   * 所有 HAVING 到的字段，默认为空，当使用 HAVING 列时，必须使用当前方法返回的列
   */
  public Optional<List<EntityColumn>> havingColumns() {
    return Optional.empty();
  }

  /**
   * 所有排序用到的字段，默认为空，当使用排序列时，必须使用当前方法返回的列
   */
  public Optional<List<EntityColumn>> orderByColumns() {
    return Optional.empty();
  }

  /**
   * 所有查询列，形如 column1, column2, ...
   */
  public String baseColumnList() {
    return selectColumns().stream().map(EntityColumn::column).collect(Collectors.joining(","));
  }

  /**
   * 所有查询列，形如 column1 AS property1, column2 AS property2, ...
   */
  public String baseColumnAsPropertyList() {
    return selectColumns().stream().map(EntityColumn::columnAsProperty).collect(Collectors.joining(","));
  }

  /**
   * 所有 insert 列，形如 column1, column2, ...，字段来源 {@link #insertColumns()}
   */
  public String insertColumnList() {
    return insertColumns().stream().map(EntityColumn::column).collect(Collectors.joining(","));
  }

  /**
   * 所有 order by 字段，默认空，字段来源 {@link #groupByColumns()} 参考值: column1, column2, ...
   * <p>
   * 默认重写 {@link #groupByColumns()} 方法即可，当前方法不需要重写
   */
  public Optional<String> groupByColumnList() {
    Optional<List<EntityColumn>> groupByColumns = groupByColumns();
    return groupByColumns.map(entityColumns -> entityColumns.stream().map(EntityColumn::column)
        .collect(Collectors.joining(",")));
  }

  /**
   * 带上 GROUP BY 前缀的方法，默认空，默认查询列来自 {@link #groupByColumnList()}
   * <p>
   * 默认重写 {@link #groupByColumns()} 方法即可，当前方法不需要重写
   */
  public Optional<String> groupByColumn() {
    Optional<String> groupByColumnList = groupByColumnList();
    return groupByColumnList.map(s -> " GROUP BY " + s);
  }

  /**
   * 所有 having 字段，默认空，字段来源 {@link #havingColumns()} 参考值: column1, column2, ...
   */
  public Optional<String> havingColumnList() {
    Optional<List<EntityColumn>> havingColumns = havingColumns();
    return havingColumns.map(entityColumns -> entityColumns.stream().map(EntityColumn::column)
        .collect(Collectors.joining(",")));
  }

  /**
   * 带上 HAVING 前缀的方法，默认空，默认查询列来自 {@link #havingColumnList()}
   */
  public Optional<String> havingColumn() {
    Optional<String> havingColumnList = havingColumnList();
    return havingColumnList.map(s -> " HAVING " + s);
  }

  /**
   * 所有 order by 字段，默认空，字段来源 {@link #orderByColumns()} 参考值: column1, column2, ...
   * <p>
   * 默认重写 {@link #orderByColumns()} 方法即可，当前方法不需要重写
   */
  public Optional<String> orderByColumnList() {
    Optional<List<EntityColumn>> orderByColumns = orderByColumns();
    return orderByColumns.map(entityColumns -> entityColumns.stream().map(EntityColumn::column)
        .collect(Collectors.joining(",")));
  }

  /**
   * 带上 ORDER BY 前缀的方法，默认空，默认查询列来自 {@link #orderByColumnList()}
   * <p>
   * 默认重写 {@link #orderByColumns()} 方法即可，当前方法不需要重写
   */
  public Optional<String> orderByColumn() {
    Optional<String> orderColumnList = orderByColumnList();
    return orderColumnList.map(s -> " ORDER BY " + s);
  }
  //</editor-fold>

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof EntityTable)) return false;
    EntityTable entity = (EntityTable) o;
    return table().equals(entity.table());
  }

  @Override
  public int hashCode() {
    return Objects.hash(table());
  }

  @Override
  public String toString() {
    return table();
  }
}

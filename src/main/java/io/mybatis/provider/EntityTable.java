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

import io.mybatis.provider.defaults.GenericTypeResolver;
import io.mybatis.provider.util.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeException;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.UnknownTypeHandler;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 实体表接口，记录实体和表的关系
 *
 * @author liuzh
 */
@Accessors(fluent = true)
public class EntityTable extends EntityProps<EntityTable> {
  public static final Pattern            DELIMITER         = Pattern.compile("^[`\\[\"]?(.*?)[`\\]\"]?$");
  public static final String             RESULT_MAP_NAME   = "BaseProviderResultMap";
  /**
   * 原始表名，在拼 SQL 中，使用 {@link #tableName()} 方法，这个方法可能会返回代理方法加工后的值
   */
  @Getter
  @Setter
  protected           String             table;
  /**
   * 实体类和字段转表名和字段名方式
   */
  @Getter
  @Setter
  protected           String             style;
  /**
   * 实体类
   */
  @Getter
  @Setter
  protected           Class<?>           entityClass;
  /**
   * 字段信息
   */
  @Setter
  protected           List<EntityColumn> columns;
  /**
   * 初始化完成，可以使用
   */
  @Getter
  @Setter
  protected           boolean            ready;
  /**
   * 使用指定的 &lt;resultMap&gt;
   */
  @Getter
  @Setter
  protected           String             resultMap;
  /**
   * 自动根据字段生成 &lt;resultMap&gt;
   */
  @Getter
  @Setter
  protected           boolean            autoResultMap;
  /**
   * 已初始化自动ResultMap
   */
  protected           List<ResultMap>    resultMaps;
  /**
   * 已经初始化的配置
   */
  protected           Set<Configuration> initConfiguration = new HashSet<>();
  //<editor-fold desc="基础方法，必须实现的方法">

  protected EntityTable(Class<?> entityClass) {
    this.entityClass = entityClass;
  }

  public static EntityTable of(Class<?> entityClass) {
    return new EntityTable(entityClass);
  }

  /**
   * 获取 SQL 语句中使用的表名
   */
  public String tableName() {
    return table();
  }

  /**
   * 返回所有列
   *
   * @return 所有列信息
   */
  public List<EntityColumn> columns() {
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
  public List<String> fieldNames() {
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
      column.entityTable(this);
    } else {
      //同名列在父类存在时，说明是子类覆盖的，字段的顺序应该更靠前
      EntityColumn existsColumn = columns().remove(columns().indexOf(column));
      columns().add(0, existsColumn);
    }
  }

  /**
   * 是否使用 resultMaps
   *
   * @param providerContext 当前方法信息
   * @param cacheKey        缓存 key，每个方法唯一，默认和 msId 一样
   * @return true 是，false 否
   */
  protected boolean canUseResultMaps(ProviderContext providerContext, String cacheKey) {
    if (resultMaps != null
        && providerContext.getMapperMethod().isAnnotationPresent(SelectProvider.class)) {
      Class<?> resultType = resultMaps.get(0).getType();
      //类型相同时直接返回
      if (resultType == providerContext.getMapperMethod().getReturnType()) {
        return true;
      }
      //可能存在泛型的情况，如 List<T>, Optional<T>, 还有 MyBatis 包含的一些注解
      Class<?> returnType = GenericTypeResolver.getReturnType(
          providerContext.getMapperMethod(), providerContext.getMapperType());
      return resultType == returnType;
    }
    return false;
  }

  /**
   * 当前实体类是否使用 resultMap
   *
   * @return
   */
  public boolean useResultMaps() {
    return resultMaps != null || autoResultMap || Utils.isNotEmpty(resultMap);
  }

  /**
   * 是否已经替换 resultMap
   *
   * @param configuration MyBatis 配置类，慎重操作
   * @param cacheKey      缓存 key，每个方法唯一，默认和 msId 一样
   * @return
   */
  protected boolean hasBeenReplaced(Configuration configuration, String cacheKey) {
    MappedStatement mappedStatement = configuration.getMappedStatement(cacheKey);
    if (mappedStatement.getResultMaps() != null && mappedStatement.getResultMaps().size() > 0) {
      return mappedStatement.getResultMaps().get(0) == resultMaps.get(0);
    }
    return false;
  }

  /**
   * 设置运行时信息，不同方法分别执行一次，需要保证幂等
   *
   * @param configuration   MyBatis 配置类，慎重操作，多数据源或多个配置时，需要区分 Configuration 执行
   * @param providerContext 当前方法信息
   * @param cacheKey        缓存 key，每个方法唯一，默认和 msId 一样
   */
  public void initRuntimeContext(Configuration configuration, ProviderContext providerContext, String cacheKey) {
    //初始化一次，后续不会重复初始化
    if (!initConfiguration.contains(configuration)) {
      initResultMap(configuration, providerContext, cacheKey);
      initConfiguration.add(configuration);
    }
    if (canUseResultMaps(providerContext, cacheKey)) {
      synchronized (cacheKey) {
        if (!hasBeenReplaced(configuration, cacheKey)) {
          MetaObject metaObject = SystemMetaObject.forObject(configuration.getMappedStatement(cacheKey));
          metaObject.setValue("resultMaps", Collections.unmodifiableList(resultMaps));
        }
      }
    }
  }

  protected void initResultMap(Configuration configuration, ProviderContext providerContext, String cacheKey) {
    //使用指定的 resultMap
    if (Utils.isNotEmpty(resultMap)) {
      synchronized (this) {
        if (resultMaps == null) {
          resultMaps = new ArrayList<>();
          String resultMapId = generateResultMapId(providerContext, resultMap);
          if (configuration.hasResultMap(resultMapId)) {
            resultMaps.add(configuration.getResultMap(resultMapId));
          } else if (configuration.hasResultMap(resultMap)) {
            resultMaps.add(configuration.getResultMap(resultMap));
          } else {
            throw new RuntimeException(entityClass().getName() + " configured resultMap: " + resultMap + " not found");
          }
        }
      }
    }
    //自动生成 resultMap
    else if (autoResultMap) {
      synchronized (this) {
        if (resultMaps == null) {
          resultMaps = new ArrayList<>();
          ResultMap resultMap = genResultMap(configuration, providerContext, cacheKey);
          resultMaps.add(resultMap);
          configuration.addResultMap(resultMap);
        }
      }
    }
  }

  protected String generateResultMapId(ProviderContext providerContext, String resultMapId) {
    if (resultMapId.indexOf(".") > 0) {
      return resultMapId;
    }
    return providerContext.getMapperType().getName() + "." + resultMapId;
  }

  protected ResultMap genResultMap(Configuration configuration, ProviderContext providerContext, String cacheKey) {
    List<ResultMapping> resultMappings = new ArrayList<>();
    for (EntityColumn entityColumn : selectColumns()) {
      String column = entityColumn.column();
      //去掉可能存在的分隔符，例如：`order`
      Matcher matcher = DELIMITER.matcher(column);
      if (matcher.find()) {
        column = matcher.group(1);
      }
      ResultMapping.Builder builder = new ResultMapping.Builder(configuration, entityColumn.property(), column, entityColumn.javaType());
      if (entityColumn.jdbcType != null && entityColumn.jdbcType != JdbcType.UNDEFINED) {
        builder.jdbcType(entityColumn.jdbcType);
      }
      if (entityColumn.typeHandler != null && entityColumn.typeHandler != UnknownTypeHandler.class) {
        try {
          builder.typeHandler(getTypeHandlerInstance(entityColumn.javaType(), entityColumn.typeHandler));
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
      List<ResultFlag> flags = new ArrayList<>();
      if (entityColumn.id) {
        flags.add(ResultFlag.ID);
      }
      builder.flags(flags);
      resultMappings.add(builder.build());
    }
    String resultMapId = generateResultMapId(providerContext, RESULT_MAP_NAME);
    ResultMap.Builder builder = new ResultMap.Builder(configuration, resultMapId, entityClass(), resultMappings, true);
    return builder.build();
  }


  /**
   * 实例化TypeHandler
   */
  public TypeHandler getTypeHandlerInstance(Class<?> javaTypeClass, Class<?> typeHandlerClass) {
    if (javaTypeClass != null) {
      try {
        Constructor<?> c = typeHandlerClass.getConstructor(Class.class);
        return (TypeHandler) c.newInstance(javaTypeClass);
      } catch (NoSuchMethodException ignored) {
        // ignored
      } catch (Exception e) {
        throw new TypeException("Failed invoking constructor for handler " + typeHandlerClass, e);
      }
    }
    try {
      Constructor<?> c = typeHandlerClass.getConstructor();
      return (TypeHandler) c.newInstance();
    } catch (Exception e) {
      throw new TypeException("Unable to find a usable constructor for " + typeHandlerClass, e);
    }
  }
  //</editor-fold>

  //<editor-fold desc="根据基础方法能直接实现的默认方法，实现方法时要避免破坏接口间的调用关系">

  /**
   * 返回主键列，不会为空，当根据主键作为条件时，必须使用当前方法返回的列，没有设置主键时，当前方法返回所有列
   */
  public List<EntityColumn> idColumns() {
    List<EntityColumn> idColumns = columns().stream().filter(EntityColumn::id).collect(Collectors.toList());
    if (idColumns.isEmpty()) {
      return columns();
    }
    return idColumns;
  }

  /**
   * 返回普通列，排除主键字段，当根据非主键作为条件时，必须使用当前方法返回的列
   */
  public List<EntityColumn> normalColumns() {
    return columns().stream().filter(column -> !column.id()).collect(Collectors.toList());
  }

  /**
   * 返回查询列，当获取查询列时，必须使用当前方法返回的列
   */
  public List<EntityColumn> selectColumns() {
    return columns().stream().filter(EntityColumn::selectable).collect(Collectors.toList());
  }

  /**
   * 返回查询列，默认所有列，当使用查询条件列时，必须使用当前方法返回的列
   */
  public List<EntityColumn> whereColumns() {
    return columns();
  }

  /**
   * 所有 insert 用到的字段，当插入列时，必须使用当前方法返回的列
   */
  public List<EntityColumn> insertColumns() {
    return columns().stream().filter(EntityColumn::insertable).collect(Collectors.toList());
  }

  /**
   * 所有 update 用到的字段，当更新列时，必须使用当前方法返回的列
   */
  public List<EntityColumn> updateColumns() {
    return columns().stream().filter(EntityColumn::updatable).collect(Collectors.toList());
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
   * 所有排序用到的字段
   */
  public Optional<List<EntityColumn>> orderByColumns() {
    List<EntityColumn> orderByColumns = columns().stream()
        .filter(c -> Utils.isNotEmpty(c.orderBy))
        .sorted(Comparator.comparing(EntityColumn::orderByPriority))
        .collect(Collectors.toList());
    if (orderByColumns.size() > 0) {
      return Optional.of(orderByColumns);
    }
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
    //当存在 resultMaps 时，查询列不能用别名
    if (useResultMaps()) {
      return baseColumnList();
    }
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
    return orderByColumns.map(entityColumns -> entityColumns.stream()
        .map(column -> column.column() + " " + column.orderBy())
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

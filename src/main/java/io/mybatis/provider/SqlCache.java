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
import org.apache.ibatis.mapping.SqlSource;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * sql缓存
 *
 * @author liuzh
 */
public class SqlCache {
  /**
   * 执行方法上下文
   */
  private final    ProviderContext  providerContext;
  /**
   * 实体类信息
   */
  private final    EntityTable      entity;
  /**
   * sql 提供者
   */
  private final    Supplier<String> sqlScriptSupplier;
  /**
   * 生成的 MyBatis SqlSource，缓存该对象，避免重复解析上面的 sql 脚本生成
   */
  private volatile SqlSource        sqlSource;

  SqlCache(ProviderContext providerContext, EntityTable entity, Supplier<String> sqlScriptSupplier) {
    Objects.requireNonNull(providerContext);
    Objects.requireNonNull(entity);
    Objects.requireNonNull(sqlScriptSupplier);
    this.providerContext = providerContext;
    this.entity = entity;
    this.sqlScriptSupplier = sqlScriptSupplier;
  }

  /**
   * 该方法延迟到最终生成 SqlSource 时才执行
   */
  public String getSqlScript() {
    return sqlScriptSupplier.get();
  }

  /**
   * @return 执行方法上下文
   */
  public ProviderContext getProviderContext() {
    return providerContext;
  }

  /**
   * @return 实体类信息
   */
  public EntityTable getEntity() {
    return entity;
  }

  /**
   * @return sql数据源
   */
  SqlSource getSqlSource() {
    return sqlSource;
  }

  /**
   * 设置 sql数据源
   *
   * @param sqlSource sql数据源
   */
  void setSqlSource(SqlSource sqlSource) {
    this.sqlSource = sqlSource;
  }

}

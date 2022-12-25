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

package io.mybatis.provider.keysql;

import io.mybatis.provider.EntityColumn;
import io.mybatis.provider.EntityTable;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;

import java.sql.Statement;
import java.util.Date;

/**
 * 生成主键
 */
public class GenIdKeyGenerator implements KeyGenerator {
  private final GenId<?>      genId;
  private final EntityTable   table;
  private final EntityColumn  column;
  private final Configuration configuration;
  private final boolean       executeBefore;
  private       Date          firstTime;

  public GenIdKeyGenerator(GenId<?> genId, EntityTable table, EntityColumn column, Configuration configuration, boolean executeBefore) {
    this.genId = genId;
    this.table = table;
    this.column = column;
    this.configuration = configuration;
    this.executeBefore = executeBefore;
  }

  @Override
  public void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
    if (executeBefore) {
      genId(parameter);
    }
  }

  @Override
  public void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
    if (!executeBefore) {
      genId(parameter);
    }
  }

  /**
   * 只有第一次需要 executeBefore 时，此时已经漏掉了 processBefore 的执行时机，这里需要额外执行一次
   */
  public void genIdFirstTime(Object parameter) {
    if (runFirstTime()) {
      this.firstTime = new Date();
      genId(parameter);
    }
  }

  public void genId(Object parameter) {
    Object id = genId.genId(table, column);
    configuration.newMetaObject(parameter).setValue(column.property(), id);
  }

  public boolean runFirstTime() {
    return executeBefore && this.firstTime == null;
  }
}

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

import io.mybatis.config.ConfigHelper;
import io.mybatis.provider.EntityColumn;
import io.mybatis.provider.EntityTable;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;

import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 生成主键
 */
public class GenIdKeyGenerator implements KeyGenerator {
  /**
   * 并发度，默认1000并发，当第一次并发insert时，实际并发不超过该值时，可以保证不会出现before=true时漏生成主键
   */
  private static volatile Integer       CONCURRENCY;
  private final           GenId<?>      genId;
  private final           EntityTable   table;
  private final           EntityColumn  column;
  private final           Configuration configuration;
  private final           boolean       executeBefore;
  private                 AtomicInteger count = new AtomicInteger(0);

  public GenIdKeyGenerator(GenId<?> genId, EntityTable table, EntityColumn column, Configuration configuration, boolean executeBefore) {
    this.genId = genId;
    this.table = table;
    this.column = column;
    this.configuration = configuration;
    this.executeBefore = executeBefore;
  }

  private static int getConcurrency() {
    if (CONCURRENCY == null) {
      synchronized (GenIdKeyGenerator.class) {
        if (CONCURRENCY == null) {
          CONCURRENCY = ConfigHelper.getInt("mybatis.provider.genId.concurrency", 1000);
        }
      }
    }
    return CONCURRENCY;
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

  @SuppressWarnings({"rawtypes", "unchecked"})
  public void genId(Object parameter) {
    if (parameter != null) {
      if (table.entityClass().isInstance(parameter)) {
        MetaObject metaObject = configuration.newMetaObject(parameter);
        if (metaObject.getValue(column.property()) == null) {
          Object id = genId.genId(table, column);
          metaObject.setValue(column.property(), id);
        }
      } else if (parameter instanceof Map) {
        new HashSet<>(((Map<String, Object>) parameter).values()).forEach(this::genId);
      } else if (parameter instanceof Iterator) {
        Iterator iterator = (Iterator) parameter;
        Set<Object> set = new HashSet();
        while (iterator.hasNext()) {
          set.add(iterator.next());
        }
        set.forEach(this::genId);
      } else if (parameter instanceof Iterable) {
        Set<Object> set = new HashSet();
        ((Iterable) parameter).forEach(set::add);
        set.forEach(this::genId);
      }
    }
  }

  /**
   * 准备参数，当executeBefore=true时，需要在执行前生成主键，如果是在ms初始化前进来该方法，就没有生成id，这里需要补充执行一次
   * <p>
   * ms初始化之后再进来时，会按照selectKey的方式自动调用该方法，不需要再这里补充调用。
   * <p>
   * 这里设置 {@link #CONCURRENCY} 是为了避免后续ms正常后，这里判断为空会多此一举，所以设置一个阈值，超过阈值后就不再判断了。
   * <p>
   * 只有当第一次出现并发插入超过 {@link #CONCURRENCY} 值时，才可能会出现漏掉id的情况。
   */
  public void prepare(Object parameter) {
    if (executeBefore) {
      if (count.get() < getConcurrency()) {
        count.incrementAndGet();
        genId(parameter);
      }
    }
  }

}

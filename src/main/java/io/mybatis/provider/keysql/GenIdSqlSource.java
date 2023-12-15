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

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;

/**
 * 当需要插入前生成主键时，通过封装 SqlSource，实现在执行前生成主键
 */
public class GenIdSqlSource implements SqlSource {
  private final SqlSource         sqlSource;
  private final GenIdKeyGenerator keyGenerator;

  public GenIdSqlSource(SqlSource sqlSource, GenIdKeyGenerator keyGenerator) {
    this.sqlSource = sqlSource;
    this.keyGenerator = keyGenerator;
  }

  @Override
  public BoundSql getBoundSql(Object parameterObject) {
    // 初始化时，会漏掉第一次执行，这里需要补上
    keyGenerator.prepare(parameterObject);
    return sqlSource.getBoundSql(parameterObject);
  }

}

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

package io.mybatis.provider.defaults;

import io.mybatis.provider.EntityTable;
import io.mybatis.provider.SqlScript;
import org.apache.ibatis.builder.annotation.ProviderContext;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Parameter;

public class NotNullSqlWrapper extends AnnotationSqlWrapper {

  public NotNullSqlWrapper(Object target, ElementType type, Annotation[] annotations) {
    super(target, type, annotations);
  }

  @Override
  public SqlScript wrap(ProviderContext context, EntityTable entity, SqlScript sqlScript) {
    String variable = getParameterName((Parameter) target);
    return (entityTable) -> "<bind name=\"" + variable + "_notNull\" value=\"@io.mybatis.provider.util.Assert@notNull("
        + variable + ", '参数 \\'" + variable + "\\' 不能为空')\"/>\n" + sqlScript.getSql(entityTable);
  }

}

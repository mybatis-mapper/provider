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

import io.mybatis.provider.SqlScriptWrapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.scripting.xmltags.DynamicContext;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 注解方式的 {@link SqlScriptWrapper}
 *
 * @author liuzh
 */
public abstract class AnnotationSqlWrapper implements SqlScriptWrapper {
  protected final ElementType  type;
  protected final Object       target;
  protected final Annotation[] annotations;

  public AnnotationSqlWrapper(Object target, ElementType type, Annotation[] annotations) {
    this.type = type;
    this.target = target;
    this.annotations = annotations;
  }

  public ElementType getType() {
    return type;
  }

  public Object getTarget() {
    return target;
  }

  public Annotation[] getAnnotations() {
    return annotations;
  }

  /**
   * 获取参数名
   *
   * @param parameter
   * @return
   */
  public String getParameterName(Parameter parameter) {
    //优先使用 @Param 注解指定的值
    Optional<Annotation> paramOptional = Stream.of(annotations).filter(a -> a.annotationType() == Param.class).findFirst();
    if (paramOptional.isPresent()) {
      return ((Param) paramOptional.get()).value();
    }
    Executable executable = parameter.getDeclaringExecutable();
    //只有一个参数时，只能使用默认名称
    if (executable.getParameterCount() == 1) {
      return DynamicContext.PARAMETER_OBJECT_KEY;
    }
    //参数名
    String name = parameter.getName();
    if (!name.startsWith("arg")) {
      return name;
    }
    //获取参数顺序号
    int index = 0;
    Parameter[] parameters = executable.getParameters();
    for (; index < parameters.length; index++) {
      if (parameters[index] == parameter) {
        break;
      }
    }
    //如果方法不是默认名，就直接使用该名称
    if (!name.equals("arg" + index)) {
      return name;
    } else {
      return ParamNameResolver.GENERIC_NAME_PREFIX + (index + 1);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AnnotationSqlWrapper that = (AnnotationSqlWrapper) o;
    return type == that.type && target.equals(that.target);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, target);
  }
}

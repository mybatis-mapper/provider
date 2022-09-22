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

import io.mybatis.provider.*;
import org.apache.ibatis.builder.annotation.ProviderContext;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通过 {@link SqlWrapper} 注解支持对 SQL 的扩展
 *
 * @author liuzh
 */
public class AnnotationSqlScriptWrapper implements SqlScriptWrapper {

  @Override
  public SqlScript wrap(ProviderContext context, EntityTable entity, SqlScript sqlScript) {
    Class<?> mapperType = context.getMapperType();
    Method mapperMethod = context.getMapperMethod();
    //接口注解
    List<AnnotationSqlWrapper> wrappers = parseAnnotations(mapperType, ElementType.TYPE, mapperType.getAnnotations());
    //方法注解
    wrappers.addAll(parseAnnotations(mapperMethod, ElementType.METHOD, mapperMethod.getAnnotations()));
    //参数注解
    Parameter[] parameters = mapperMethod.getParameters();
    Annotation[][] parameterAnnotations = mapperMethod.getParameterAnnotations();
    for (int i = 0; i < parameters.length; i++) {
      wrappers.addAll(parseAnnotations(parameters[i], ElementType.PARAMETER, parameterAnnotations[i]));
    }
    //去重，排序
    wrappers = wrappers.stream().distinct().sorted(Comparator.comparing(f -> ((Order) f).getOrder()).reversed()).collect(Collectors.toList());
    for (SqlScriptWrapper wrapper : wrappers) {
      sqlScript = wrapper.wrap(context, entity, sqlScript);
    }
    return sqlScript;
  }

  /**
   * 获取对象上的 AbstractSqlScriptWrapper 实例
   *
   * @param target
   * @param type
   * @param annotations
   * @return
   */
  private List<AnnotationSqlWrapper> parseAnnotations(Object target, ElementType type, Annotation[] annotations) {
    List<Class<? extends AnnotationSqlWrapper>> classes = new ArrayList<>();
    for (int i = 0; i < annotations.length; i++) {
      Annotation annotation = annotations[i];
      Class<? extends Annotation> annotationType = annotation.annotationType();
      if (annotationType == SqlWrapper.class) {
        classes.addAll(Arrays.asList(((SqlWrapper) annotation).value()));
      } else if (annotationType.isAnnotationPresent(SqlWrapper.class)) {
        SqlWrapper annotationTypeAnnotation = annotationType.getAnnotation(SqlWrapper.class);
        classes.addAll(Arrays.asList(annotationTypeAnnotation.value()));
      }
    }
    return classes.stream().map(c -> (AnnotationSqlWrapper) newInstance(c, target, type, annotations))
        .collect(Collectors.toList());
  }

  /**
   * 实例化 AbstractSqlScriptWrapper 对象
   *
   * @param instanceClass
   * @param target
   * @param type
   * @param annotations
   * @param <T>
   * @return
   */
  public <T> T newInstance(Class instanceClass, Object target, ElementType type, Annotation[] annotations) {
    try {
      return (T) instanceClass.getConstructor(Object.class, ElementType.class, Annotation[].class).newInstance(target, type, annotations);
    } catch (Exception e) {
      throw new RuntimeException("instance [ " + instanceClass + " ] error", e);
    }
  }

}

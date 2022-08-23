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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * 参考 {@link java.lang.reflect.Field} 中的同名方法
 *
 * @author liuzh
 */
public class EntityField {
  /**
   * 所在实体类类型
   */
  protected Class<?> entityClass;
  /**
   * 对应实体类中的 Java 字段（可以自己扩展方法注解）
   */
  protected Field    field;

  public EntityField() {
  }

  public EntityField(Class<?> entityClass, Field field) {
    this.entityClass = entityClass;
    this.field = field;
    this.field.setAccessible(true);
  }

  /**
   * @return 当前字段所在的类
   */
  public Class<?> getDeclaringClass() {
    return field.getDeclaringClass();
  }

  /**
   * @return 字段名
   */
  public String getName() {
    return field.getName();
  }

  /**
   * @return 字段类型
   */
  public Class<?> getType() {
    return (Class<?>) GenericTypeResolver.resolveFieldType(field, entityClass);
  }

  /**
   * 获取字段上的指定注解信息
   *
   * @param annotationClass 注解类型
   * @param <T>
   * @return 注解信息
   */
  public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
    return field.getAnnotation(annotationClass);
  }

  /**
   * 获取字段上的全部注解信息
   *
   * @return 注解信息
   */
  public Annotation[] getAnnotations() {
    return field.getAnnotations();
  }

  /**
   * 字段上是否配置了指定的注解
   *
   * @param annotationClass 注解类型
   * @return 字段上是否配置了指定的注解
   */
  public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
    return field.isAnnotationPresent(annotationClass);
  }

  /**
   * 反射获取字段值
   *
   * @param obj 对象
   * @return 字段值
   */
  public Object get(Object obj) {
    try {
      return field.get(obj);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Error getting field value by reflection", e);
    }
  }

  /**
   * 反射设置字段值
   *
   * @param obj   对象
   * @param value 字段值
   */
  public void set(Object obj, Object value) {
    try {
      field.set(obj, value);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Error in reflection setting field value", e);
    }
  }

}

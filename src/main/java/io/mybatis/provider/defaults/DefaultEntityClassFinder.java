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

import io.mybatis.provider.Entity;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * 默认实现，针对 {@link Entity} 注解实现
 *
 * @author liuzh
 */
public class DefaultEntityClassFinder extends GenericEntityClassFinder {

  @Override
  public Optional<Class<?>> findEntityClass(Class<?> mapperType, Method mapperMethod) {
    if (mapperMethod != null) {
      //首先是接口方法
      if (mapperMethod.isAnnotationPresent(Entity.class)) {
        Entity entity = mapperMethod.getAnnotation(Entity.class);
        return Optional.of(entity.value());
      }
    }
    //其次是接口上
    if (mapperType.isAnnotationPresent(Entity.class)) {
      Entity entity = mapperType.getAnnotation(Entity.class);
      return Optional.of(entity.value());
    }
    //没有明确指名的情况下，通过泛型获取
    return super.findEntityClass(mapperType, mapperMethod);
  }

  @Override
  public boolean isEntityClass(Class<?> clazz) {
    return clazz.isAnnotationPresent(Entity.Table.class);
  }

}

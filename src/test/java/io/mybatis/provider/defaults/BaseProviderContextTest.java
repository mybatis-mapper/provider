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

import java.lang.reflect.Method;

public class BaseProviderContextTest {

  public Context context(Class<?> mapperType, String methodName) {
    Method[] methods = mapperType.getMethods();
    for (Method method : methods) {
      if (method.getName().equals(methodName)) {
        try {
          return new Context(mapperType, method);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }
    throw new IllegalArgumentException(methodName + " 参数对应方法不存在");
  }

  static class Context {
    public final Class<?> mapperType;
    public final Method   mapperMethod;

    public Context(Class<?> mapperType, Method mapperMethod) {
      this.mapperType = mapperType;
      this.mapperMethod = mapperMethod;
    }
  }

}

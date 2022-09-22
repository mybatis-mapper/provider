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


import io.mybatis.provider.defaults.AnnotationSqlWrapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 通过注解对 SQL 进行二次加工，可以用于接口，方法，参数，以及注解，用于注解时，只支持一层搜索
 *
 * @author liuzh
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.PARAMETER})
public @interface SqlWrapper {

  /**
   * 加工处理器
   *
   * @return
   */
  Class<? extends AnnotationSqlWrapper>[] value() default {};

}

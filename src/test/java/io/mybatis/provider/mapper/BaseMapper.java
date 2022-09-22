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

package io.mybatis.provider.mapper;

import io.mybatis.provider.Caching;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.SelectProvider;

/**
 * 测试接口泛型参数识别类型，故意调换的两个泛型参数顺序，正常应该找到第二个泛型参数
 */
public interface BaseMapper<ID, T> extends BaseMapper2<Long, T> {

  @Lang(Caching.class)
  @SelectProvider(type = BaseProvider.class, method = "getById")
  T getById(ID id);

  @Lang(Caching.class)
  @InsertProvider(type = BaseProvider.class, method = "insertSelective")
  int insertSelective(T user);

  @Lang(Caching.class)
  @DeleteProvider(type = BaseProvider.class, method = "deleteById")
  int deleteById(ID id);
}

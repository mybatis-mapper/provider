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
import io.mybatis.provider.model.User1;
import io.mybatis.provider.model.User2;
import io.mybatis.provider.model.User3;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.SelectKey;

public interface User123Mapper {

  @Lang(Caching.class)
  @InsertProvider(type = BaseProvider.class, method = "insertSelective")
  int insertUser1_0(User1 user);

  @Lang(Caching.class)
  @Options(useGeneratedKeys = true, keyProperty = "id")
  @InsertProvider(type = BaseProvider.class, method = "insertSelective")
  int insertUser1_1(User1 user);

  @Lang(Caching.class)
  @SelectKey(statement = "CALL IDENTITY()", keyProperty = "id", before = false, resultType = Long.class)
  @InsertProvider(type = BaseProvider.class, method = "insertSelective")
  int insertUser1_2(User1 user);

  @Lang(Caching.class)
  @InsertProvider(type = BaseProvider.class, method = "insertSelective")
  int insertUser2(User2 user);

  @Lang(Caching.class)
  @InsertProvider(type = BaseProvider.class, method = "insertSelective")
  int insertUser3(User3 user);

}

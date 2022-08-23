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
import io.mybatis.provider.Entity;
import io.mybatis.provider.EntityInfoMapper;
import io.mybatis.provider.defaults.NotNull;
import io.mybatis.provider.model.BaseUser;
import io.mybatis.provider.model.User;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Options;

/**
 * 接口上的 <code>@Entity</code> 注解优先级低于方法上的注解
 */
@Entity(BaseUser.class)
public interface BaseUserMapper extends EntityInfoMapper<BaseUser> {

  /**
   * 当前方法只能通过接口上的注解识别实体类类型
   *
   * @param id
   * @return
   */
  @Lang(Caching.class)
  @DeleteProvider(type = BaseProvider.class, method = "deleteById")
  int deleteById(Long id);

  /**
   * 当前方法只能通过接口上的注解识别实体类类型
   *
   * @param id
   * @return
   */
  @Lang(Caching.class)
  @DeleteProvider(type = BaseProvider.class, method = "deleteById")
  int deleteByIdNotNull(@NotNull Long id);

  /**
   * 方法上的 <code>@Entity</code> 注解优先级高于接口上的注解
   */
  @Entity(User.class)
  @Lang(Caching.class)
  @Options(useGeneratedKeys = true, keyProperty = "id")//主键回写
  @InsertProvider(type = BaseProvider.class, method = "insertSelective")
  int insertSelective(User user);

}

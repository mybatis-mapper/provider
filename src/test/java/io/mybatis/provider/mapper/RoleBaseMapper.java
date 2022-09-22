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
import io.mybatis.provider.model.Role;
import io.mybatis.provider.model.User;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.Lang;

/**
 * 测试接口泛型参数识别类型
 */
//@Entity(Role.class)
public interface RoleBaseMapper extends BaseMapper<Long, Role> {

  /**
   * 当前方法<b>无法</b>能通过接口泛型类型识别实体类类型，必须通过接口或方法上的 @Entity 指定
   *
   * @param id
   * @return
   */
  @Lang(Caching.class)
  @DeleteProvider(type = BaseProvider.class, method = "deleteById")
  int deleteRoleById(Long id);

  /**
   * 在 role 中通过指定User.class操作user实体
   *
   * @param id
   * @return
   */
  @Entity(User.class)
  @Lang(Caching.class)
  @DeleteProvider(type = BaseProvider.class, method = "deleteById")
  int deleteUserById(Long id);

}

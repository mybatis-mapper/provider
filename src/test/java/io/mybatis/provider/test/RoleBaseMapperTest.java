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

package io.mybatis.provider.test;

import io.mybatis.provider.BaseTest;
import io.mybatis.provider.mapper.RoleBaseMapper;
import io.mybatis.provider.mapper.UserMapper;
import io.mybatis.provider.model.Role;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

public class RoleBaseMapperTest extends BaseTest {

  @Test
  public void testDeleteById() {
    try (SqlSession sqlSession = getSqlSession()) {
      RoleBaseMapper roleMapper = sqlSession.getMapper(RoleBaseMapper.class);
      Assert.assertEquals(1, roleMapper.deleteRoleById(1L));
      Assert.assertNull(roleMapper.getById(1L));
    }
  }

  @Test
  public void testDeleteUserById() {
    try (SqlSession sqlSession = getSqlSession()) {
      RoleBaseMapper roleMapper = sqlSession.getMapper(RoleBaseMapper.class);
      Assert.assertEquals(1, roleMapper.deleteUserById(1L));
      UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
      Assert.assertNull(userMapper.getById(1L));
    }
  }

  @Test
  public void testEntityInfo() {
    try (SqlSession sqlSession = getSqlSession()) {
      RoleBaseMapper roleMapper = sqlSession.getMapper(RoleBaseMapper.class);
      Assert.assertEquals(Role.class, roleMapper.entityClass());
    }
  }

}

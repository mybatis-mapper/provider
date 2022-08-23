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
import io.mybatis.provider.model.Role;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

public class BaseMapperTest extends BaseTest {

  @Test
  public void testGetById() {
    try (SqlSession sqlSession = getSqlSession()) {
      RoleBaseMapper roleMapper = sqlSession.getMapper(RoleBaseMapper.class);
      Role role = roleMapper.getById(1L);
      Assert.assertNotNull(role);
      Assert.assertEquals("男主角", role.getName());
    }
  }

  @Test
  public void testInsertSelective() {
    try (SqlSession sqlSession = getSqlSession()) {
      RoleBaseMapper roleMapper = sqlSession.getMapper(RoleBaseMapper.class);
      Role role = new Role();
      role.setName("友情主演");
      Assert.assertEquals(1, roleMapper.insertSelective(role));
      //主键不回写
      Assert.assertNull(role.getId());
      sqlSession.rollback();
    }
  }


}

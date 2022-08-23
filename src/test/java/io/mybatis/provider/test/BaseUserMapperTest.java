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
import io.mybatis.provider.mapper.BaseUserMapper;
import io.mybatis.provider.model.BaseUser;
import io.mybatis.provider.model.User;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

public class BaseUserMapperTest extends BaseTest {

  @Test
  public void testInsertSelective() {
    try (SqlSession sqlSession = getSqlSession()) {
      BaseUserMapper userMapper = sqlSession.getMapper(BaseUserMapper.class);
      User user = new User();
      user.setSex("男");
      Assert.assertEquals(1, userMapper.insertSelective(user));
      //主键回写
      Assert.assertNotNull(user.getId());
      //默认值不回写
      Assert.assertNull(user.getUsername());
      sqlSession.rollback();
    }
  }

  @Test
  public void testDeleteById() {
    try (SqlSession sqlSession = getSqlSession()) {
      BaseUserMapper userMapper = sqlSession.getMapper(BaseUserMapper.class);
      Assert.assertEquals(1, userMapper.deleteById(1L));
      sqlSession.rollback();
    }
  }

  @Test
  public void testDeleteByIdNull() {
    try (SqlSession sqlSession = getSqlSession()) {
      BaseUserMapper userMapper = sqlSession.getMapper(BaseUserMapper.class);
      Assert.assertEquals(0, userMapper.deleteById(null));
      sqlSession.rollback();
    }
  }

  @Test(expected = Exception.class)
  public void testDeleteByIdNotNull() {
    try (SqlSession sqlSession = getSqlSession()) {
      BaseUserMapper userMapper = sqlSession.getMapper(BaseUserMapper.class);
      Assert.assertEquals(0, userMapper.deleteByIdNotNull(null));
      sqlSession.rollback();
    }
  }

  @Test
  public void testEntityInfo() {
    try (SqlSession sqlSession = getSqlSession()) {
      BaseUserMapper baseUserMapper = sqlSession.getMapper(BaseUserMapper.class);
      Assert.assertEquals(BaseUser.class, baseUserMapper.entityClass());
    }
  }

}

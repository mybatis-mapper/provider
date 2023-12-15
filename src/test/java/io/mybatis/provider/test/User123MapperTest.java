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
import io.mybatis.provider.mapper.User123Mapper;
import io.mybatis.provider.model.User1;
import io.mybatis.provider.model.User2;
import io.mybatis.provider.model.User3;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

public class User123MapperTest extends BaseTest {

  @Test
  public void testInsert() {
    try (SqlSession sqlSession = getSqlSession()) {
      User123Mapper userMapper = sqlSession.getMapper(User123Mapper.class);

      User1 u1 = new User1();
      u1.setUsername("hello");
      Assert.assertEquals(1, userMapper.insertUser1_0(u1));
      Assert.assertNotNull(u1.getId());
      u1.setId(null);
      Assert.assertEquals(1, userMapper.insertUser1_0(u1));
      Assert.assertNotNull(u1.getId());

      u1 = new User1();
      u1.setUsername("hello");
      Assert.assertEquals(1, userMapper.insertUser1_1(u1));
      Assert.assertNotNull(u1.getId());
      u1.setId(null);
      Assert.assertEquals(1, userMapper.insertUser1_1(u1));
      Assert.assertNotNull(u1.getId());

      u1 = new User1();
      u1.setUsername("hello");
      Assert.assertEquals(1, userMapper.insertUser1_2(u1));
      Assert.assertNotNull(u1.getId());
      u1.setId(null);
      Assert.assertEquals(1, userMapper.insertUser1_2(u1));
      Assert.assertNotNull(u1.getId());

      User2 u2 = new User2();
      u2.setUsername("hello");
      Assert.assertEquals(1, userMapper.insertUser2(u2));
      Assert.assertNotNull(u2.getId());
      u2.setId(null);
      Assert.assertEquals(1, userMapper.insertUser2(u2));
      Assert.assertNotNull(u2.getId());

      User3 u3 = new User3();
      u3.setUsername("hello");
      Assert.assertEquals(1, userMapper.insertUser3(u3));
      Assert.assertNotNull(u3.getId());
      u3.setId(null);
      Assert.assertEquals(1, userMapper.insertUser3(u3));
      Assert.assertNotNull(u3.getId());
      u3.setId(null);
      Assert.assertEquals(1, userMapper.insertUser3(u3));
      Assert.assertNotNull(u3.getId());
      u3.setId(null);
      Assert.assertEquals(1, userMapper.insertUser3(u3));
      Assert.assertNotNull(u3.getId());
    }
  }

}

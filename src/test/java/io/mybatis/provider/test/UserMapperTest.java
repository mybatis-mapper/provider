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
import io.mybatis.provider.EntityFactory;
import io.mybatis.provider.EntityTable;
import io.mybatis.provider.mapper.UserMapper;
import io.mybatis.provider.model.User;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class UserMapperTest extends BaseTest {

  private static EntityTable entityTable;

  @BeforeClass
  public static void initEntityTable() {
    entityTable = EntityFactory.create(User.class);
  }

  @Test
  public void testEntityTable() {
    Assert.assertEquals(3, entityTable.columns().size());
    Assert.assertEquals(1, entityTable.idColumns().size());
    Assert.assertEquals(3, entityTable.selectColumns().size());
    Assert.assertEquals(3, entityTable.insertColumns().size());
    Assert.assertEquals(3, entityTable.updateColumns().size());
    Assert.assertEquals(3, entityTable.whereColumns().size());

    Assert.assertEquals("user", entityTable.table());
    Assert.assertEquals("ID,name,SEX", entityTable.baseColumnList());
    Assert.assertEquals("ID AS id,name AS username,SEX AS sex", entityTable.baseColumnAsPropertyList());
  }

  @Test
  public void testGetById() {
    try (SqlSession sqlSession = getSqlSession()) {
      UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
      User user = userMapper.getById(1L);
      Assert.assertNotNull(user);
      Assert.assertEquals("张无忌", user.getUsername());
      Assert.assertEquals("男", user.getSex());
    }
  }

}

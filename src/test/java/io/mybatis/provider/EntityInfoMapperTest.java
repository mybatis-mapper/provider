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

import org.junit.Assert;
import org.junit.Test;

public class EntityInfoMapperTest {

  @Test
  public void test() {
    UserMapper userMapper = new UserMapper() {
    };
    Assert.assertEquals(User.class, userMapper.entityClass());

    EntityTable entityTable = userMapper.entityTable();
    Assert.assertEquals("sys_user", entityTable.table());
  }

  interface UserMapper extends EntityInfoMapper<User> {

  }

  @Entity.Table("sys_user")
  class User {
//    @Entity.Column(id = true)
//    private Long id;
  }

}

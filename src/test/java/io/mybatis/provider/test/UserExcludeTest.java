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

import io.mybatis.provider.EntityFactory;
import io.mybatis.provider.EntityTable;
import io.mybatis.provider.model.UserExclude;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class UserExcludeTest {
  private static EntityTable entityTable;

  @BeforeClass
  public static void initEntityTable() {
    entityTable = EntityFactory.create(UserExclude.class);
  }

  @Test
  public void testEntityTable() {
    Assert.assertEquals(2, entityTable.columns().size());
    Assert.assertEquals(1, entityTable.idColumns().size());
    Assert.assertEquals(2, entityTable.selectColumns().size());
    Assert.assertEquals(2, entityTable.insertColumns().size());
    Assert.assertEquals(2, entityTable.updateColumns().size());
    Assert.assertEquals(2, entityTable.whereColumns().size());

    Assert.assertEquals("user", entityTable.table());
    Assert.assertEquals("catalog", entityTable.catalog());
    Assert.assertEquals("schema", entityTable.schema());
    Assert.assertEquals("catalog.schema.user", entityTable.tableName());
    Assert.assertEquals("ID,name", entityTable.baseColumnList());
    Assert.assertEquals("ID AS id,name AS username", entityTable.baseColumnAsPropertyList());

  }

}

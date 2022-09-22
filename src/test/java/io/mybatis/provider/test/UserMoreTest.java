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

import io.mybatis.provider.EntityColumn;
import io.mybatis.provider.EntityFactory;
import io.mybatis.provider.EntityTable;
import io.mybatis.provider.model.UserMore;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.StringTypeHandler;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Optional;

public class UserMoreTest {
  private static EntityTable entityTable;

  @BeforeClass
  public static void initEntityTable() {
    entityTable = EntityFactory.create(UserMore.class);
  }

  @Test
  public void testEntityTable() {
    Assert.assertEquals(8, entityTable.columns().size());
    Assert.assertEquals(1, entityTable.idColumns().size());
    Assert.assertEquals(7, entityTable.selectColumns().size());
    Assert.assertEquals(7, entityTable.insertColumns().size());
    Assert.assertEquals(6, entityTable.updateColumns().size());
    Assert.assertEquals(8, entityTable.whereColumns().size());

    Assert.assertEquals("sys_user", entityTable.table());
    Assert.assertEquals("id,name,is_admin,seq,points,when_created,info", entityTable.baseColumnList());
    Assert.assertEquals("id,name,is_admin,seq,points,when_created,info", entityTable.baseColumnAsPropertyList());
    Assert.assertEquals("seq DESC,name ASC", entityTable.orderByColumnList().get());

  }

  protected EntityColumn column(String fieldName) {
    Optional<EntityColumn> optionalEntityColumn = entityTable.columns().stream()
        .filter(c -> c.property().equals(fieldName)).findFirst();
    return optionalEntityColumn.orElseThrow(() -> new RuntimeException(fieldName + " 不存在"));
  }

  @Test
  public void testEntityColumn() {
    EntityColumn idColumn = column("id");
    Assert.assertEquals("id", idColumn.column());
    Assert.assertTrue(idColumn.id());
    Assert.assertTrue(idColumn.selectable());
    Assert.assertFalse(idColumn.updatable());
    Assert.assertFalse(idColumn.insertable());

    EntityColumn adminColumn = column("admin");
    Assert.assertEquals("is_admin", adminColumn.column());
    Assert.assertFalse(adminColumn.id());
    Assert.assertFalse(adminColumn.updatable());
    Assert.assertTrue(adminColumn.selectable());
    Assert.assertTrue(adminColumn.insertable());

    EntityColumn seqColumn = column("seq");
    Assert.assertEquals("seq", seqColumn.column());
    Assert.assertEquals("DESC", seqColumn.orderBy());
    Assert.assertFalse(seqColumn.id());
    Assert.assertTrue(seqColumn.updatable());
    Assert.assertTrue(seqColumn.selectable());
    Assert.assertTrue(seqColumn.insertable());

    EntityColumn pointsColumn = column("points");
    Assert.assertEquals("points", pointsColumn.column());
    Assert.assertFalse(pointsColumn.numericScale().isEmpty());
    Assert.assertEquals("4", pointsColumn.numericScale());
    Assert.assertEquals(", numericScale=4", pointsColumn.numericScaleVariables().get());
    Assert.assertEquals("#{points, numericScale=4}", pointsColumn.variables());
    Assert.assertFalse(pointsColumn.id());
    Assert.assertTrue(pointsColumn.updatable());
    Assert.assertTrue(pointsColumn.selectable());
    Assert.assertTrue(pointsColumn.insertable());

    EntityColumn passwordColumn = column("password");
    Assert.assertEquals("password", passwordColumn.column());
    Assert.assertTrue(passwordColumn.numericScale().isEmpty());
    Assert.assertFalse(passwordColumn.id());
    Assert.assertFalse(passwordColumn.selectable());
    Assert.assertTrue(passwordColumn.updatable());
    Assert.assertTrue(passwordColumn.insertable());

    EntityColumn whenCreatedColumn = column("whenCreated");
    Assert.assertEquals("when_created", whenCreatedColumn.column());
    Assert.assertEquals(JdbcType.TIMESTAMP, whenCreatedColumn.jdbcType());
    Assert.assertEquals("#{whenCreated, jdbcType=TIMESTAMP}", whenCreatedColumn.variables());
    Assert.assertTrue(whenCreatedColumn.numericScale().isEmpty());
    Assert.assertFalse(whenCreatedColumn.id());
    Assert.assertTrue(whenCreatedColumn.selectable());
    Assert.assertTrue(whenCreatedColumn.updatable());
    Assert.assertTrue(whenCreatedColumn.insertable());

    EntityColumn infoColumn = column("info");
    Assert.assertEquals("info", infoColumn.column());
    Assert.assertEquals(StringTypeHandler.class, infoColumn.typeHandler());
    Assert.assertEquals("#{info, typeHandler=org.apache.ibatis.type.StringTypeHandler}", infoColumn.variables());
    Assert.assertTrue(infoColumn.numericScale().isEmpty());
    Assert.assertFalse(infoColumn.id());
    Assert.assertTrue(infoColumn.selectable());
    Assert.assertTrue(infoColumn.updatable());
    Assert.assertTrue(infoColumn.insertable());
  }

}

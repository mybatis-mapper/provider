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

package io.mybatis.provider.fn;

import io.mybatis.provider.Entity;
import io.mybatis.provider.model.User;
import io.mybatis.provider.model.UserIs;
import org.apache.ibatis.type.JdbcType;
import org.junit.Assert;
import org.junit.Test;

public class FnTest {

  @Test
  public void test() {
    // field名称由mybatis.provider.style属性决定，test中文件：mybatis-provider.propertis配置为upper_underscore
    Assert.assertEquals("sex", ((Fn<User, Object>) User::getSex).toField());
    Assert.assertEquals("SEX", ((Fn<User, Object>) User::getSex).toColumn());
    Assert.assertEquals("username", ((Fn<User, Object>) User::getUsername).toField());
    Assert.assertEquals("name", ((Fn<User, Object>) User::getUsername).toColumn());
    Assert.assertEquals("admin", ((Fn<UserIs, Object>) UserIs::isAdmin).toField());
    Assert.assertEquals("is_admin", ((Fn<UserIs, Object>) UserIs::isAdmin).toColumn());

    Assert.assertEquals("is_admin", Fn.field(UserIs.class, UserIs::isAdmin).toColumn());
    Assert.assertEquals("is_admin", Fn.field(UserIs.class, "admin").toColumn());
    Assert.assertEquals("is_admin", Fn.column(UserIs.class, "is_admin").toColumn());
  }

  @Test
  public void testExtends() {
    Assert.assertEquals("id", ((Fn<SysUser, Object>) SysUser::getId).toField());
    Assert.assertEquals("ID", ((Fn<SysUser, Object>) SysUser::getId).toColumn());
    Assert.assertEquals("whenCreate", ((Fn<SysUser, Object>) SysUser::getWhenCreate).toField());
    Assert.assertEquals("when_create", ((Fn<SysUser, Object>) SysUser::getWhenCreate).toColumn());
    Assert.assertEquals("userName", ((Fn<SysUser, Object>) SysUser::getUserName).toField());
    Assert.assertEquals("name", ((Fn<SysUser, Object>) SysUser::getUserName).toColumn());

    Assert.assertEquals("id", ((Fn<SysRole, Object>) SysRole::getId).toField());
    Assert.assertEquals("ID", ((Fn<SysRole, Object>) SysRole::getId).toColumn());
    Assert.assertEquals("whenCreate", ((Fn<SysRole, Object>) SysRole::getWhenCreate).toField());
    Assert.assertEquals("when_create", ((Fn<SysRole, Object>) SysRole::getWhenCreate).toColumn());
    Assert.assertEquals("roleName", ((Fn<SysRole, Object>) SysRole::getRoleName).toField());
    Assert.assertEquals("name", ((Fn<SysRole, Object>) SysRole::getRoleName).toColumn());


    Assert.assertEquals("when_create", Fn.field(SysRole.class, BaseEntity::getWhenCreate).toColumn());
    Assert.assertEquals("when_create", Fn.field(SysRole.class, "whenCreate").toColumn());
    Assert.assertEquals("when_create", Fn.column(SysRole.class, "when_create").toColumn());
  }

  public static class BaseId {
    @Entity.Column(id = true)
    private Long id;

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }
  }

  public static class BaseEntity extends BaseId {
    @Entity.Column(value = "when_create", jdbcType = JdbcType.TIMESTAMP)
    private Long whenCreate;

    public Long getWhenCreate() {
      return whenCreate;
    }

    public void setWhenCreate(Long whenCreate) {
      this.whenCreate = whenCreate;
    }
  }

  @Entity.Table("sys_user")
  public static class SysUser extends BaseEntity {
    @Entity.Column("name")
    private String userName;

    public String getUserName() {
      return userName;
    }

    public void setUserName(String userName) {
      this.userName = userName;
    }
  }

  @Entity.Table("sys_role")
  public class SysRole extends BaseEntity {
    @Entity.Column("name")
    private String roleName;

    public String getRoleName() {
      return roleName;
    }

    public void setRoleName(String roleName) {
      this.roleName = roleName;
    }
  }


}

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

package io.mybatis.provider.defaults;

import io.mybatis.provider.mapper.RoleBaseMapper;
import io.mybatis.provider.model.BaseId;
import io.mybatis.provider.model.BaseUser;
import io.mybatis.provider.model.Role;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

public class GenericTypeResolverTest extends BaseProviderContextTest {

  @Test
  public void resolveMapperTypes() {
    Type[] types = GenericTypeResolver.resolveMapperTypes(RoleBaseMapper.class);
    Assert.assertEquals(2, types.length);
    Assert.assertEquals(Long.class, types[0]);
    Assert.assertEquals(Role.class, types[1]);
  }

  @Test
  public void resolveMapperTypes2() {
    Context context = context(RoleBaseMapper.class, "getById");
    Type[] types = GenericTypeResolver.resolveMapperTypes(context.mapperMethod, context.mapperType);
    Assert.assertEquals(2, types.length);
    Assert.assertEquals(Long.class, types[0]);
    Assert.assertEquals(Role.class, types[1]);

    context = context(RoleBaseMapper.class, "deleteRoleById");
    types = GenericTypeResolver.resolveMapperTypes(context.mapperMethod, context.mapperType);
    Assert.assertEquals(0, types.length);
  }

  @Test
  public void resolveFieldType() throws NoSuchFieldException {
    Field id = BaseId.class.getDeclaredField("id");
    Field username = BaseUser.class.getDeclaredField("username");

    Type type = GenericTypeResolver.resolveFieldType(id, BaseId.class);
    Assert.assertEquals(Object.class, type);

    type = GenericTypeResolver.resolveFieldType(id, BaseUser.class);
    Assert.assertEquals(Long.class, type);

    type = GenericTypeResolver.resolveFieldType(username, BaseUser.class);
    Assert.assertEquals(String.class, type);
  }

  @Test
  public void resolveReturnType() {
    Context context = context(RoleBaseMapper.class, "getById");
    Type type = GenericTypeResolver.resolveReturnType(context.mapperMethod, context.mapperType);
    Assert.assertEquals(Role.class, type);
  }

  @Test
  public void resolveParamTypes() {
    Context context = context(RoleBaseMapper.class, "getById");
    Type[] types = GenericTypeResolver.resolveParamTypes(context.mapperMethod, context.mapperType);
    Assert.assertEquals(1, types.length);
    Assert.assertEquals(Long.class, types[0]);

    context = context(RoleBaseMapper.class, "insertSelective");
    types = GenericTypeResolver.resolveParamTypes(context.mapperMethod, context.mapperType);
    Assert.assertEquals(1, types.length);
    Assert.assertEquals(Role.class, types[0]);
  }


}

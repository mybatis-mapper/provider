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

import io.mybatis.provider.Entity;
import io.mybatis.provider.mapper.RoleBaseMapper;
import io.mybatis.provider.model.Role;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

public class GenericEntityClassFinderTest extends BaseProviderContextTest {
  private final GenericEntityClassFinder classFinder = new GenericEntityClassFinder() {
    @Override
    public boolean isEntityClass(Class<?> clazz) {
      return clazz.isAnnotationPresent(Entity.Table.class);
    }
  };

  @Test
  public void getEntityClass() {
    //通过返回值获取
    Context context = context(RoleBaseMapper.class, "getById");
    Optional<Class<?>> optionalClass = classFinder.findEntityClass(context.mapperType, context.mapperMethod);
    Assert.assertTrue(optionalClass.isPresent());
    Assert.assertEquals(Role.class, optionalClass.get());

    //通过参数获取
    context = context(RoleBaseMapper.class, "insertSelective");
    optionalClass = classFinder.findEntityClass(context.mapperType, context.mapperMethod);
    Assert.assertTrue(optionalClass.isPresent());
    //当前方法只是根据泛型获取，因此这里得到的类型不对
    Assert.assertEquals(Role.class, optionalClass.get());

    //通过父接口泛型获取
    context = context(RoleBaseMapper.class, "deleteById");
    optionalClass = classFinder.findEntityClass(context.mapperType, context.mapperMethod);
    Assert.assertTrue(optionalClass.isPresent());
    //当前方法只是根据泛型获取，因此这里得到的类型不对
    Assert.assertEquals(Role.class, optionalClass.get());

    //通过接口泛型获取
    context = context(RoleBaseMapper.class, "deleteRoleById");
    optionalClass = classFinder.findEntityClass(context.mapperType, context.mapperMethod);
    Assert.assertTrue(optionalClass.isPresent());
    //当前方法只是根据泛型获取，因此这里得到的类型不对
    Assert.assertEquals(Role.class, optionalClass.get());


    context = context(RoleBaseMapper.class, "deleteUserById");
    optionalClass = classFinder.findEntityClass(context.mapperType, context.mapperMethod);
    Assert.assertTrue(optionalClass.isPresent());
    //当前方法只是根据泛型获取，因此这里得到的类型不对
    Assert.assertEquals(Role.class, optionalClass.get());
  }

  @Test
  public void getEntityClassByMapperMethodReturnType() {
    Context context = context(RoleBaseMapper.class, "getById");
    Optional<Class<?>> optionalClass = classFinder.getEntityClassByMapperMethodReturnType(context.mapperType, context.mapperMethod);
    Assert.assertTrue(optionalClass.isPresent());
    Assert.assertEquals(Role.class, optionalClass.get());

    context = context(RoleBaseMapper.class, "insertSelective");
    optionalClass = classFinder.getEntityClassByMapperMethodReturnType(context.mapperType, context.mapperMethod);
    Assert.assertFalse(optionalClass.isPresent());
  }

  @Test
  public void getEntityClassByMapperMethodParamTypes() {
    Context context = context(RoleBaseMapper.class, "insertSelective");
    Optional<Class<?>> optionalClass = classFinder.getEntityClassByMapperMethodParamTypes(context.mapperType, context.mapperMethod);
    Assert.assertTrue(optionalClass.isPresent());
    Assert.assertEquals(Role.class, optionalClass.get());

    context = context(RoleBaseMapper.class, "deleteById");
    optionalClass = classFinder.getEntityClassByMapperMethodParamTypes(context.mapperType, context.mapperMethod);
    Assert.assertFalse(optionalClass.isPresent());
  }

  @Test
  public void getEntityClassByMapperMethodAndMapperType() {
    Context context = context(RoleBaseMapper.class, "deleteById");
    Optional<Class<?>> optionalClass = classFinder.getEntityClassByMapperMethodAndMapperType(context.mapperType, context.mapperMethod);
    Assert.assertTrue(optionalClass.isPresent());
    Assert.assertEquals(Role.class, optionalClass.get());

    context = context(RoleBaseMapper.class, "deleteRoleById");
    optionalClass = classFinder.getEntityClassByMapperMethodAndMapperType(context.mapperType, context.mapperMethod);
    Assert.assertFalse(optionalClass.isPresent());
  }

  @Test
  public void getEntityClassByMapperType() {
    Context context = context(RoleBaseMapper.class, "deleteRoleById");
    Optional<Class<?>> optionalClass = classFinder.getEntityClassByMapperType(context.mapperType);
    Assert.assertTrue(optionalClass.isPresent());
    Assert.assertEquals(Role.class, optionalClass.get());

    context = context(RoleBaseMapper.class, "deleteById");
    optionalClass = classFinder.getEntityClassByMapperType(context.mapperType);
    Assert.assertTrue(optionalClass.isPresent());
    Assert.assertEquals(Role.class, optionalClass.get());
  }
}

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

package io.mybatis.provider.config;

import io.mybatis.config.Config;
import io.mybatis.config.ConfigHelper;
import org.junit.Assert;
import org.junit.Test;

public class ConfigHelperTest {

  @Test
  public void testUserConfig() {
    Config config = new ProviderUserConfig();
    String username = config.getStr("username");
    Assert.assertEquals("liuzh", username);
    Assert.assertEquals("abel533@gmail.com", config.getStr("email"));
  }

  @Test
  public void testVersionConfig() {
    Config config = new ProviderVersionConfig();
    String style = config.getStr("mybatis.provider.style");
    Assert.assertEquals("lower_underscore", style);

    // 通过配置指定版本，也可以 JVM 中 -Dio.mybatis.provider.version=v1.1
    System.setProperty("io.mybatis.provider.version", "v1.1");
    config = new ProviderVersionConfig();
    style = config.getStr("mybatis.provider.style");
    Assert.assertEquals("normal", style);
    // 清除配置，避免对其他测试产生影响
    System.clearProperty("io.mybatis.provider.version");
  }

  @Test
  public void test() {
    Assert.assertEquals("liuzh", ConfigHelper.getStr("username"));
    Assert.assertEquals("abel533@gmail.com", ConfigHelper.getStr("email"));
    Assert.assertEquals("upper_underscore", ConfigHelper.getStr("mybatis.provider.style"));
  }

}

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

import io.mybatis.provider.util.ServiceLoaderUtil;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.mapping.MappedStatement;

import java.util.List;

/**
 * 支持定制化处理 {@link MappedStatement}
 */
public interface MsCustomize {

  /**
   * 支持 SPI 扩展的默认实现
   */
  MsCustomize SPI = new MsCustomize() {
    private final List<MsCustomize> customizes = ServiceLoaderUtil.getInstances(MsCustomize.class);

    @Override
    public void customize(EntityTable entity, MappedStatement ms, ProviderContext context) {
      for (MsCustomize customize : customizes) {
        customize.customize(entity, ms, context);
      }
    }
  };

  /**
   * 定制化 ms
   *
   * @param entity  实体
   * @param ms      MappedStatement
   * @param context
   */
  void customize(EntityTable entity, MappedStatement ms, ProviderContext context);

}

/*
 * Copyright 2020 the original author or authors.
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

import org.apache.ibatis.builder.annotation.ProviderContext;

import java.util.function.Supplier;

/**
 * 对 xml 形式 sql 简单封装，便于使用
 *
 * @author liuzh
 */
public interface SqlScript {

  /**
   * 创建SQL并缓存
   *
   * @param providerContext 执行方法上下文
   * @param sqlScript       xml sql 脚本实现
   * @return 缓存key
   */
  static String caching(ProviderContext providerContext, SqlScript sqlScript) {
    EntityTable entity = EntityFactory.create(providerContext.getMapperType(), providerContext.getMapperMethod());
    return Caching.cache(providerContext, entity, () -> String.format("<script>\n%s\n</script>", sqlScript.getSql(entity)));
  }

  /**
   * 创建SQL并缓存
   *
   * @param providerContext 执行方法上下文
   * @param sqlScript       xml sql 脚本实现
   * @return 缓存key
   */
  static String caching(ProviderContext providerContext, SqlScript2 sqlScript) {
    EntityTable entity = EntityFactory.create(providerContext.getMapperType(), providerContext.getMapperMethod());
    return Caching.cache(providerContext, entity, () -> String.format("<script>\n%s\n</script>", sqlScript.getSql(entity, sqlScript)));
  }

  /**
   * 生成对应的 SQL，支持动态标签
   *
   * @param entity 实体类信息
   * @return xml sql 脚本
   */
  String getSql(EntityTable entity);

  /**
   * 生成 where 标签包装的 xml 结构
   *
   * @param content 标签中的内容
   * @return where 标签包装的 xml 结构
   */
  default String where(Supplier<String> content) {
    return String.format("\n<where>\n%s\n</where> ", content.get());
  }

  /**
   * 生成 choose 标签包装的 xml 结构
   *
   * @param content 标签中的内容
   * @return choose 标签包装的 xml 结构
   */
  default String choose(Supplier<String> content) {
    return String.format("\n<choose>\n%s\n</choose> ", content.get());
  }

  /**
   * 生成 otherwise 标签包装的 xml 结构
   *
   * @param content 标签中的内容
   * @return otherwise 标签包装的 xml 结构
   */
  default String otherwise(Supplier<String> content) {
    return String.format("\n<otherwise>\n%s\n</otherwise> ", content.get());
  }

  /**
   * 生成 set 标签包装的 xml 结构
   *
   * @param content 标签中的内容
   * @return set 标签包装的 xml 结构
   */
  default String set(Supplier<String> content) {
    return String.format("\n<set>\n%s\n</set> ", content.get());
  }

  /**
   * 生成 if 标签包装的 xml 结构
   *
   * @param test    if 的判断条件
   * @param content 标签中的内容
   * @return if 标签包装的 xml 结构
   */
  default String ifTest(String test, Supplier<String> content) {
    return String.format("<if test=\"%s\">\n%s\n</if> ", test, content.get());
  }

  /**
   * 生成 &lt;if test="_parameter != null"&gt; 标签包装的 xml 结构
   *
   * @param content 标签中的内容
   * @return &lt;if test="_parameter != null"&gt; 标签包装的 xml 结构
   */
  default String ifParameterNotNull(Supplier<String> content) {
    return String.format("\n<if test=\"_parameter != null\">\n%s\n</if> ", content.get());
  }

  /**
   * 生成 when 标签包装的 xml 结构
   *
   * @param test    when 的判断条件
   * @param content 标签中的内容
   * @return when 标签包装的 xml 结构
   */
  default String whenTest(String test, Supplier<String> content) {
    return String.format("\n<when test=\"%s\">\n%s\n</when> ", test, content.get());
  }

  /**
   * 生成 trim 标签包装的 xml 结构
   *
   * @param prefix          前缀
   * @param suffix          后缀
   * @param prefixOverrides 前缀替换内容
   * @param suffixOverrides 后缀替换内容
   * @param content         标签中的内容
   * @return trim 标签包装的 xml 结构
   */
  default String trim(String prefix, String suffix, String prefixOverrides, String suffixOverrides, Supplier<String> content) {
    return String.format("\n<trim prefix=\"%s\" prefixOverrides=\"%s\" suffixOverrides=\"%s\" suffix=\"%s\">\n%s\n</trim> "
        , prefix, prefixOverrides, suffixOverrides, suffix, content.get());
  }

  /**
   * 生成 trim 标签包装的 xml 结构
   *
   * @param prefix          前缀
   * @param suffix          后缀
   * @param prefixOverrides 前缀替换内容
   * @param content         标签中的内容
   * @return trim 标签包装的 xml 结构
   */
  default String trimPrefixOverrides(String prefix, String suffix, String prefixOverrides, Supplier<String> content) {
    return String.format("\n<trim prefix=\"%s\" prefixOverrides=\"%s\" suffix=\"%s\">\n%s\n</trim> ", prefix, prefixOverrides, suffix, content.get());
  }


  /**
   * 生成 trim 标签包装的 xml 结构
   *
   * @param prefix          前缀
   * @param suffix          后缀
   * @param suffixOverrides 后缀替换内容
   * @param content         标签中的内容
   * @return trim 标签包装的 xml 结构
   */
  default String trimSuffixOverrides(String prefix, String suffix, String suffixOverrides, Supplier<String> content) {
    return String.format("\n<trim prefix=\"%s\" suffixOverrides=\"%s\" suffix=\"%s\">\n%s\n</trim> ", prefix, suffixOverrides, suffix, content.get());
  }

  /**
   * 生成 foreach 标签包装的 xml 结构
   *
   * @param collection 遍历的对象
   * @param item       对象名
   * @param content    标签中的内容
   * @return foreach 标签包装的 xml 结构
   */
  default String foreach(String collection, String item, Supplier<String> content) {
    return String.format("\n<foreach collection=\"%s\" item=\"%s\">\n%s\n</foreach> ", collection, item, content.get());
  }

  /**
   * 生成 foreach 标签包装的 xml 结构
   *
   * @param collection 遍历的对象
   * @param item       对象名
   * @param separator  连接符
   * @param content    标签中的内容
   * @return foreach 标签包装的 xml 结构
   */
  default String foreach(String collection, String item, String separator, Supplier<String> content) {
    return String.format("\n<foreach collection=\"%s\" item=\"%s\" separator=\"%s\">\n%s\n</foreach> "
        , collection, item, separator, content.get());
  }

  /**
   * 生成 foreach 标签包装的 xml 结构
   *
   * @param collection 遍历的对象
   * @param item       对象名
   * @param separator  连接符
   * @param open       开始符号
   * @param close      结束符号
   * @param content    标签中的内容
   * @return foreach 标签包装的 xml 结构
   */
  default String foreach(String collection, String item, String separator, String open, String close, Supplier<String> content) {
    return String.format("\n<foreach collection=\"%s\" item=\"%s\" open=\"%s\" close=\"%s\" separator=\"%s\">\n%s\n</foreach> "
        , collection, item, open, close, separator, content.get());
  }

  /**
   * 生成 foreach 标签包装的 xml 结构
   *
   * @param collection 遍历的对象
   * @param item       对象名
   * @param separator  连接符
   * @param open       开始符号
   * @param close      结束符号
   * @param index      索引名（list为索引，map为key）
   * @param content    标签中的内容
   * @return foreach 标签包装的 xml 结构
   */
  default String foreach(String collection, String item, String separator, String open, String close, String index, Supplier<String> content) {
    return String.format("\n<foreach collection=\"%s\" item=\"%s\" index=\"%s\" open=\"%s\" close=\"%s\" separator=\"%s\">\n%s\n</foreach> "
        , collection, item, index, open, close, separator, content.get());
  }

  /**
   * 生成 bind 标签包装的 xml 结构
   *
   * @param name  变量名
   * @param value 变量值
   * @return bind 标签包装的 xml 结构
   */
  default String bind(String name, String value) {
    return String.format("\n<bind name=\"%s\" value=\"%s\"/>", name, value);
  }

  /**
   * 支持简单写法
   */
  interface SqlScript2 extends SqlScript {

    @Override
    default String getSql(EntityTable entity) {
      return getSql(entity, this);
    }

    /**
     * 生成对应的 SQL，支持动态标签
     *
     * @param entity 实体类信息
     * @param util   当前对象的引用，可以在 lambda 中使用当前对象的方法
     * @return 对应的 SQL，支持动态标签
     */
    String getSql(EntityTable entity, SqlScript util);

  }

}

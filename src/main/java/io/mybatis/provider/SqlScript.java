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

import org.apache.ibatis.builder.annotation.ProviderContext;

import java.util.function.Supplier;

/**
 * 对 xml 形式 sql 简单封装，便于使用
 *
 * @author liuzh
 */
public interface SqlScript {
  /**
   * 换行符
   */
  String LF = "\n";

  /**
   * 创建SQL并缓存
   *
   * @param providerContext 执行方法上下文
   * @param sqlScript       xml sql 脚本实现
   * @return 缓存key
   */
  static String caching(ProviderContext providerContext, SqlScript sqlScript) {
    EntityTable entity = EntityFactory.create(providerContext.getMapperType(), providerContext.getMapperMethod());
    return Caching.cache(providerContext, entity, () -> String.format("<script>\n%s\n</script>",
        SqlScriptWrapper.wrapSqlScript(providerContext, entity, sqlScript).getSql(entity)));
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
    return Caching.cache(providerContext, entity, () -> String.format("<script>\n%s\n</script>",
        SqlScriptWrapper.wrapSqlScript(providerContext, entity, sqlScript).getSql(entity)));
  }

  /**
   * 生成 where 标签包装的 xml 结构
   *
   * @param content 标签中的内容
   * @return where 标签包装的 xml 结构
   */
  default String where(LRSupplier content) {
    return String.format("\n<where>%s\n</where> ", content.getWithLR());
  }

  /**
   * 生成对应的 SQL，支持动态标签
   *
   * @param entity 实体类信息
   * @return xml sql 脚本
   */
  String getSql(EntityTable entity);

  /**
   * 生成 choose 标签包装的 xml 结构
   *
   * @param content 标签中的内容
   * @return choose 标签包装的 xml 结构
   */
  default String choose(LRSupplier content) {
    return String.format("\n<choose>%s\n</choose> ", content.getWithLR());
  }

  /**
   * 生成 otherwise 标签包装的 xml 结构
   *
   * @param content 标签中的内容
   * @return otherwise 标签包装的 xml 结构
   */
  default String otherwise(LRSupplier content) {
    return String.format("\n<otherwise>%s\n</otherwise> ", content.getWithLR());
  }

  /**
   * 生成 set 标签包装的 xml 结构
   *
   * @param content 标签中的内容
   * @return set 标签包装的 xml 结构
   */
  default String set(LRSupplier content) {
    return String.format("\n<set>%s\n</set> ", content.getWithLR());
  }

  /**
   * 生成 if 标签包装的 xml 结构
   *
   * @param test    if 的判断条件
   * @param content 标签中的内容
   * @return if 标签包装的 xml 结构
   */
  default String ifTest(String test, LRSupplier content) {
    return String.format("<if test=\"%s\">%s\n</if> ", test, content.getWithLR());
  }

  /**
   * 生成 &lt;if test="_parameter != null"&gt; 标签包装的 xml 结构，允许参数为空时使用，
   * 当参数必填时，可以使用 {@link #parameterNotNull(String)} 方法
   *
   * @param content 标签中的内容
   * @return &lt;if test="_parameter != null"&gt; 标签包装的 xml 结构
   */
  default String ifParameterNotNull(LRSupplier content) {
    return String.format("<if test=\"_parameter != null\">%s\n</if> ", content.getWithLR());
  }

  /**
   * 增加对参数的校验，参数不能为空
   *
   * @param message 提示信息
   * @return 在代码基础上增加一段校验
   */
  default String parameterNotNull(String message) {
    return variableNotNull("_parameter", message);
  }

  /**
   * 增加对参数的校验，参数必须为 true
   *
   * @param variable 参数, 值为 boolean
   * @param message  提示信息
   * @return 在代码基础上增加一段校验
   */
  default String variableIsTrue(String variable, String message) {
    return "\n${@io.mybatis.provider.util.Assert@isTrue(" + variable + ", '" + message + "')}\n";
  }

  /**
   * 增加对参数的校验，参数必须为 false
   *
   * @param variable 参数, 值为 boolean
   * @param message  提示信息
   * @return 在代码基础上增加一段校验
   */
  default String variableIsFalse(String variable, String message) {
    return "\n${@io.mybatis.provider.util.Assert@isFalse(" + variable + ", '" + message + "')}\n";
  }

  /**
   * 增加对参数的校验，参数不能为 null
   *
   * @param variable 参数
   * @param message  提示信息
   * @return 在代码基础上增加一段校验
   */
  default String variableNotNull(String variable, String message) {
    return "\n${@io.mybatis.provider.util.Assert@notNull(" + variable + ", '" + message + "')}\n";
  }

  /**
   * 增加对参数的校验，参数不能为空
   *
   * @param message 提示信息
   * @return 在代码基础上增加一段校验
   */
  default String variableNotEmpty(String variable, String message) {
    return "\n${@io.mybatis.provider.util.Assert@notEmpty(" + variable + ", '" + message + "')}\n";
  }

  /**
   * 生成 when 标签包装的 xml 结构
   *
   * @param test    when 的判断条件
   * @param content 标签中的内容
   * @return when 标签包装的 xml 结构
   */
  default String whenTest(String test, LRSupplier content) {
    return String.format("\n<when test=\"%s\">%s\n</when> ", test, content.getWithLR());
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
  default String trim(String prefix, String suffix, String prefixOverrides, String suffixOverrides, LRSupplier content) {
    return String.format("\n<trim prefix=\"%s\" prefixOverrides=\"%s\" suffixOverrides=\"%s\" suffix=\"%s\">%s\n</trim> "
        , prefix, prefixOverrides, suffixOverrides, suffix, content.getWithLR());
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
  default String trimPrefixOverrides(String prefix, String suffix, String prefixOverrides, LRSupplier content) {
    return String.format("\n<trim prefix=\"%s\" prefixOverrides=\"%s\" suffix=\"%s\">%s\n</trim> ", prefix, prefixOverrides, suffix, content.getWithLR());
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
  default String trimSuffixOverrides(String prefix, String suffix, String suffixOverrides, LRSupplier content) {
    return String.format("\n<trim prefix=\"%s\" suffixOverrides=\"%s\" suffix=\"%s\">%s\n</trim> ", prefix, suffixOverrides, suffix, content.getWithLR());
  }

  /**
   * 生成 foreach 标签包装的 xml 结构
   *
   * @param collection 遍历的对象
   * @param item       对象名
   * @param content    标签中的内容
   * @return foreach 标签包装的 xml 结构
   */
  default String foreach(String collection, String item, LRSupplier content) {
    return String.format("\n<foreach collection=\"%s\" item=\"%s\">%s\n</foreach> ", collection, item, content.getWithLR());
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
  default String foreach(String collection, String item, String separator, LRSupplier content) {
    return String.format("\n<foreach collection=\"%s\" item=\"%s\" separator=\"%s\">%s\n</foreach> "
        , collection, item, separator, content.getWithLR());
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
  default String foreach(String collection, String item, String separator, String open, String close, LRSupplier content) {
    return String.format("\n<foreach collection=\"%s\" item=\"%s\" open=\"%s\" close=\"%s\" separator=\"%s\">%s\n</foreach> "
        , collection, item, open, close, separator, content.getWithLR());
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
  default String foreach(String collection, String item, String separator, String open, String close, String index, LRSupplier content) {
    return String.format("\n<foreach collection=\"%s\" item=\"%s\" index=\"%s\" open=\"%s\" close=\"%s\" separator=\"%s\">%s\n</foreach> "
        , collection, item, index, open, close, separator, content.getWithLR());
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
   * 保证所有字符串前面都有换行符
   */
  interface LRSupplier extends Supplier<String> {

    default String getWithLR() {
      String str = get();
      if (!str.isEmpty() && str.charAt(0) == LF.charAt(0)) {
        return str;
      }
      return LF + str;
    }

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

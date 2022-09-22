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

import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 缓存 xml 形式对应的 SqlSource，避免重复解析
 *
 * @author liuzh
 */
public class Caching extends XMLLanguageDriver {
  public static final Log log = LogFactory.getLog(Caching.class);

  /**
   * 缓存方法对应的 SqlCache
   */
  private static final Map<String, SqlCache>                      CACHE_SQL                   = new ConcurrentHashMap<>(16);
  /**
   * 多数据源，多配置的情况下（甚至单元测试时），同一个方法会在不同的 Configuration 中出现，如果不做处理就会出现不一致
   */
  private static final Map<Configuration, Map<String, SqlSource>> CONFIGURATION_CACHE_KEY_MAP = new ConcurrentHashMap<>(1);

  /**
   * 根据接口和方法生成缓存 key
   *
   * @param providerContext 执行方法上下文
   * @return 缓存key，经过 String.intern 处理，可以作为锁对象
   */
  private static String cacheKey(ProviderContext providerContext) {
    return (providerContext.getMapperType().getName() + "." + providerContext.getMapperMethod().getName()).intern();
  }

  /**
   * 判断方法是否提供了 @Lang(Caching.class) 注解
   *
   * @param providerContext 执行方法上下文
   */
  private static void isAnnotationPresentLang(ProviderContext providerContext) {
    Method mapperMethod = providerContext.getMapperMethod();
    if (mapperMethod.isAnnotationPresent(Lang.class)) {
      Lang lang = mapperMethod.getAnnotation(Lang.class);
      if (lang.value() == Caching.class) {
        return;
      }
    }
    throw new RuntimeException(mapperMethod + " need to configure @Lang(Caching.class) to use the Caching.cache method for caching");
  }

  /**
   * 缓存 sqlScript 对应的 SQL 和配置
   *
   * @param providerContext   执行方法上下文
   * @param entity            实体类信息
   * @param sqlScriptSupplier sql脚本提供者
   * @return 缓存的 key
   */
  public static String cache(ProviderContext providerContext, EntityTable entity, Supplier<String> sqlScriptSupplier) {
    String cacheKey = cacheKey(providerContext);
    if (!CACHE_SQL.containsKey(cacheKey)) {
      isAnnotationPresentLang(providerContext);
      synchronized (cacheKey) {
        if (!CACHE_SQL.containsKey(cacheKey)) {
          CACHE_SQL.put(cacheKey, new SqlCache(providerContext, entity, sqlScriptSupplier));
        }
      }
    }
    return cacheKey;
  }

  @Override
  public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {
    //调用过 Caching.cache 方法的，这里的 script 就是 String.intern 后的 cacheKey，可以用来加锁
    //没有调用过 Caching.cache 方法的，属于默认方式，可能是误加 @Lang(Caching.class) 注解，这里执行 else 中默认的方式
    //先判断 CACHE_SQL 中是否有此 script，有就是调用过 Caching.cache 方法后的 cacheKey
    if (CACHE_SQL.containsKey(script)) {
      //为了容易理解，使用 cacheKey 变量代替 script
      String cacheKey = script;
      //取出缓存的信息
      SqlCache cache = CACHE_SQL.get(cacheKey);
      //判断是否已经解析过
      if (!(CONFIGURATION_CACHE_KEY_MAP.containsKey(configuration) && CONFIGURATION_CACHE_KEY_MAP.get(configuration).containsKey(cacheKey))) {
        synchronized (cacheKey) {
          if (!(CONFIGURATION_CACHE_KEY_MAP.containsKey(configuration) && CONFIGURATION_CACHE_KEY_MAP.get(configuration).containsKey(cacheKey))) {
            //初始化 EntityTable，每个方法执行一次，可以利用 configuration 进行一些特殊操作
            cache.getEntity().initRuntimeContext(configuration, cache.getProviderContext(), cacheKey);
            Map<String, SqlSource> cachekeyMap = CONFIGURATION_CACHE_KEY_MAP.computeIfAbsent(configuration, k -> new HashMap<>());
            //下面的方法才会真正生成最终的 XML SQL，生成的时候可以用到上面的 configuration 和 ProviderContext 参数
            String sqlScript = cache.getSqlScript();
            if (log.isTraceEnabled()) {
              log.trace("cacheKey - " + cacheKey + " :\n" + sqlScript + "\n");
            }
            //缓存 sqlSource
            SqlSource sqlSource = super.createSqlSource(configuration, sqlScript, parameterType);
            cachekeyMap.put(cacheKey, sqlSource);
          }
        }
      }
      return CONFIGURATION_CACHE_KEY_MAP.get(configuration).get(cacheKey);
    } else {
      return super.createSqlSource(configuration, script, parameterType);
    }
  }

}

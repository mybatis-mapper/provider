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

package io.mybatis.provider.keysql;

import io.mybatis.provider.Caching;
import io.mybatis.provider.EntityColumn;
import io.mybatis.provider.EntityTable;
import io.mybatis.provider.MsCustomize;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.IncompleteElementException;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 针对注解 @Entity.KeySql 的处理，当方法使用 MyBatis 注解配置过主键策略时，会有警告信息，并跳过主键的自动配置
 */
public class KeySqlMsCustomize implements MsCustomize {
  public static final Log log = LogFactory.getLog(KeySqlMsCustomize.class);

  @Override
  public void customize(EntityTable entity, MappedStatement ms, ProviderContext context) {
    Method mapperMethod = context.getMapperMethod();
    if (mapperMethod.isAnnotationPresent(InsertProvider.class)) {
      List<EntityColumn> ids = entity.idColumns().stream()
          .filter(EntityColumn::hasPrimaryKeyStrategy).collect(Collectors.toList());
      if (ids.size() > 1) {
        throw new RuntimeException("只能有一个主键配置 @Entity.KeySql 注解");
      }
      if (ids.size() < 1) {
        return;
      }
      if (mapperMethod.isAnnotationPresent(Options.class)) {
        Options options = mapperMethod.getAnnotation(Options.class);
        if (options.useGeneratedKeys()) {
          log.warn("The method [" + mapperMethod.getName() + "] of the mapper [" + context.getMapperType().getName()
              + "] is configured with the @Options(useGeneratedKeys = true) annotation, and the @Entity.KeySql annotation will be ignored.");
          return;
        }
      }
      if (mapperMethod.isAnnotationPresent(SelectKey.class)) {
        log.warn("The method [" + mapperMethod.getName() + "] of the mapper [" + context.getMapperType().getName()
            + "] is configured with the @SelectKey annotation, and the @Entity.KeySql annotation will be ignored.");
        return;
      }
      EntityColumn id = ids.get(0);
      if (id.useGeneratedKeys()) {
        MetaObject metaObject = ms.getConfiguration().newMetaObject(ms);
        metaObject.setValue("keyGenerator", Jdbc3KeyGenerator.INSTANCE);
        metaObject.setValue("keyProperties", new String[]{id.property()});
      } else if (!id.afterSql().isEmpty()) {
        KeyGenerator keyGenerator = handleSelectKeyAnnotation(ms, context, id, id.afterSql(), false);
        MetaObject metaObject = ms.getConfiguration().newMetaObject(ms);
        metaObject.setValue("keyGenerator", keyGenerator);
        metaObject.setValue("keyProperties", new String[]{id.property()});
      } else if (id.genId() != GenId.NULL.class) {
        Class<? extends GenId> genIdClass = id.genId();
        boolean executeBefore = id.genIdExecuteBefore();
        GenId<?> genId = null;
        try {
          genId = genIdClass.getConstructor().newInstance();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        KeyGenerator keyGenerator = new GenIdKeyGenerator(genId, entity, id, ms.getConfiguration(), executeBefore);
        MetaObject metaObject = ms.getConfiguration().newMetaObject(ms);
        metaObject.setValue("keyGenerator", keyGenerator);
        metaObject.setValue("keyProperties", new String[]{id.property()});
      }
    }
  }

  private KeyGenerator handleSelectKeyAnnotation(MappedStatement ms,
                                                 ProviderContext context,
                                                 EntityColumn column,
                                                 String sql,
                                                 boolean executeBefore) {
    String id = ms.getId() + SelectKeyGenerator.SELECT_KEY_SUFFIX;
    Configuration configuration = ms.getConfiguration();
    LanguageDriver languageDriver = configuration.getLanguageDriver(Caching.class);
    SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, ms.getParameterMap().getType());

    MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration, id, sqlSource, SqlCommandType.SELECT)
        .resource(ms.getResource())
        .fetchSize(null)
        .timeout(null)
        .statementType(StatementType.PREPARED)
        .keyGenerator(NoKeyGenerator.INSTANCE)
        .keyProperty(column.property())
        .keyColumn(column.column())
        .databaseId(null)
        .lang(languageDriver)
        .resultOrdered(false)
        .resultSets(null)
        .resultMaps(getStatementResultMaps(ms, context, null, column.javaType(), id))
        .resultSetType(null)
        .flushCacheRequired(false)
        .useCache(false)
        .cache(null);
    ParameterMap statementParameterMap = getStatementParameterMap(ms, context, null, ms.getParameterMap().getType(), id);
    if (statementParameterMap != null) {
      statementBuilder.parameterMap(statementParameterMap);
    }

    MappedStatement statement = statementBuilder.build();
    configuration.addMappedStatement(statement);

    id = applyCurrentNamespace(context.getMapperType().getName(), id, false);

    MappedStatement keyStatement = configuration.getMappedStatement(id, false);
    SelectKeyGenerator answer = new SelectKeyGenerator(keyStatement, executeBefore);
    configuration.addKeyGenerator(id, answer);
    return answer;
  }

  public String applyCurrentNamespace(String currentNamespace, String base, boolean isReference) {
    if (base == null) {
      return null;
    }
    if (isReference) {
      // is it qualified with any namespace yet?
      if (base.contains(".")) {
        return base;
      }
    } else {
      // is it qualified with this namespace yet?
      if (base.startsWith(currentNamespace + ".")) {
        return base;
      }
      if (base.contains(".")) {
        throw new BuilderException("Dots are not allowed in element names, please remove it from " + base);
      }
    }
    return currentNamespace + "." + base;
  }

  private ParameterMap getStatementParameterMap(
      MappedStatement ms,
      ProviderContext context,
      String parameterMapName,
      Class<?> parameterTypeClass,
      String statementId) {
    parameterMapName = applyCurrentNamespace(context.getMapperType().getName(), parameterMapName, true);
    ParameterMap parameterMap = null;
    if (parameterMapName != null) {
      try {
        parameterMap = ms.getConfiguration().getParameterMap(parameterMapName);
      } catch (IllegalArgumentException e) {
        throw new IncompleteElementException("Could not find parameter map " + parameterMapName, e);
      }
    } else if (parameterTypeClass != null) {
      List<ParameterMapping> parameterMappings = new ArrayList<>();
      parameterMap = new ParameterMap.Builder(
          ms.getConfiguration(),
          statementId + "-Inline",
          parameterTypeClass,
          parameterMappings).build();
    }
    return parameterMap;
  }

  private List<ResultMap> getStatementResultMaps(MappedStatement ms,
                                                 ProviderContext context,
                                                 String resultMap,
                                                 Class<?> resultType,
                                                 String statementId) {
    resultMap = applyCurrentNamespace(context.getMapperType().getName(), resultMap, true);

    List<ResultMap> resultMaps = new ArrayList<>();
    if (resultMap != null) {
      String[] resultMapNames = resultMap.split(",");
      for (String resultMapName : resultMapNames) {
        try {
          resultMaps.add(ms.getConfiguration().getResultMap(resultMapName.trim()));
        } catch (IllegalArgumentException e) {
          throw new IncompleteElementException("Could not find result map '" + resultMapName + "' referenced from '" + statementId + "'", e);
        }
      }
    } else if (resultType != null) {
      ResultMap inlineResultMap = new ResultMap.Builder(
          ms.getConfiguration(),
          statementId + "-Inline",
          resultType,
          new ArrayList<>(),
          null).build();
      resultMaps.add(inlineResultMap);
    }
    return resultMaps;
  }

}

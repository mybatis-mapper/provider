package io.mybatis.provider.defaults;

import io.mybatis.provider.EntityTable;
import io.mybatis.provider.SqlScript;
import org.apache.ibatis.builder.annotation.ProviderContext;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Parameter;

public class NotNullSqlWrapper extends AnnotationSqlWrapper {

  public NotNullSqlWrapper(Object target, ElementType type, Annotation[] annotations) {
    super(target, type, annotations);
  }

  @Override
  public SqlScript wrap(ProviderContext context, EntityTable entity, SqlScript sqlScript) {
    String variable = getParameterName((Parameter) target);
    return (entityTable) -> "<bind name=\"" + variable + "_notNull\" value=\"@io.mybatis.provider.util.Assert@notNull("
      + variable + ", '参数 \\'" + variable + "\\' 不能为空')\"/>\n" + sqlScript.getSql(entityTable);
  }

}

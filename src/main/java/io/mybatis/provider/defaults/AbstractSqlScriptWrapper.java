package io.mybatis.provider.defaults;

import io.mybatis.provider.SqlScriptWrapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.scripting.xmltags.DynamicContext;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 注解方式的 {@link SqlScriptWrapper}
 *
 * @author liuzh
 */
public abstract class AbstractSqlScriptWrapper implements SqlScriptWrapper {
  protected final ElementType  type;
  protected final Object       target;
  protected final Annotation[] annotations;

  public AbstractSqlScriptWrapper(Object target, ElementType type, Annotation[] annotations) {
    this.type = type;
    this.target = target;
    this.annotations = annotations;
  }

  public ElementType getType() {
    return type;
  }

  public Object getTarget() {
    return target;
  }

  public Annotation[] getAnnotations() {
    return annotations;
  }

  /**
   * 获取参数名
   *
   * @param parameter
   * @return
   */
  public String getParameterName(Parameter parameter) {
    //优先使用 @Param 注解指定的值
    Optional<Annotation> paramOptional = Stream.of(annotations).filter(a -> a.annotationType() == Param.class).findFirst();
    if (paramOptional.isPresent()) {
      return ((Param) paramOptional.get()).value();
    }
    //参数名
    String name = parameter.getName();
    Executable executable = parameter.getDeclaringExecutable();
    //获取参数顺序号
    int index = 0;
    Parameter[] parameters = executable.getParameters();
    for (; index < parameters.length; index++) {
      if (parameters[index] == parameter) {
        break;
      }
    }
    //如果方法不是默认名，就直接使用该名称
    if (!name.equals("arg" + index)) {
      return name;
    } else if (executable.getParameterCount() == 1) {
      return DynamicContext.PARAMETER_OBJECT_KEY;
    } else {
      return ParamNameResolver.GENERIC_NAME_PREFIX + (index + 1);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AbstractSqlScriptWrapper that = (AbstractSqlScriptWrapper) o;
    return type == that.type && target.equals(that.target);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, target);
  }
}

package io.mybatis.provider;


import io.mybatis.provider.defaults.AnnotationSqlWrapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 通过注解对 SQL 进行二次加工，可以用于接口，方法，参数，以及注解，用于注解时，只支持一层搜索
 *
 * @author liuzh
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.PARAMETER})
public @interface SqlWrapper {

  /**
   * 加工处理器
   *
   * @return
   */
  Class<? extends AnnotationSqlWrapper>[] value() default {};

}

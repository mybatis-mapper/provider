package io.mybatis.provider.defaults;

import io.mybatis.provider.SqlWrapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法参数不能为空
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@SqlWrapper(NotNullSqlScriptWrapper.class)
public @interface NotNull {

}

package io.mybatis.provider;

import io.mybatis.provider.util.ServiceLoaderUtil;
import org.apache.ibatis.builder.annotation.ProviderContext;

import java.util.List;

/**
 * SPI 接口：对最终的SQL进行处理
 *
 * @author liuzh
 */
public interface SqlScriptWrapper extends Order {

  /**
   * 对 script 包装中的 SQL 进行加工处理
   *
   * @param context   当前接口和方法信息
   * @param entity    实体类
   * @param sqlScript sql脚本
   * @return
   */
  SqlScript wrap(ProviderContext context, EntityTable entity, SqlScript sqlScript);

  /**
   * 包装 SQL
   *
   * @param context   当前接口和方法信息
   * @param entity    实体类
   * @param sqlScript sql脚本
   * @return
   */
  static SqlScript wrapSqlScript(ProviderContext context, EntityTable entity, SqlScript sqlScript) {
    for (SqlScriptWrapper wrapper : Instance.getEntityTableFactoryChain()) {
      sqlScript = wrapper.wrap(context, entity, sqlScript);
    }
    return sqlScript;
  }

  /**
   * 实例
   */
  class Instance {
    private static volatile List<SqlScriptWrapper> sqlScriptWrappers;

    /**
     * 获取处理实体的工厂链
     *
     * @return 实例
     */
    public static List<SqlScriptWrapper> getEntityTableFactoryChain() {
      if (sqlScriptWrappers == null) {
        synchronized (EntityFactory.class) {
          if (sqlScriptWrappers == null) {
            sqlScriptWrappers = ServiceLoaderUtil.getInstances(SqlScriptWrapper.class);
          }
        }
      }
      return sqlScriptWrappers;
    }
  }

}

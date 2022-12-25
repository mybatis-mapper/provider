package io.mybatis.provider.keysql;

import io.mybatis.provider.EntityColumn;
import io.mybatis.provider.EntityTable;

/**
 * 通过接口生成主键
 *
 * @author liuzh
 */
public interface GenId<T> {

  T genId(EntityTable table, EntityColumn column);

  class NULL implements GenId {
    @Override
    public Object genId(EntityTable table, EntityColumn column) {
      throw new UnsupportedOperationException();
    }
  }

}

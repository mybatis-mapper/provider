package io.mybatis.provider;

import io.mybatis.provider.util.ServiceLoaderUtil;

import java.util.List;

/**
 * 实体名称转换，支持 SPI 替换默认实现
 *
 * @author liuzh
 */
public interface NameConvert {
  /**
   * 默认实现，驼峰转下划线
   */
  NameConvert DEFAULT = new NameConvert() {

    public boolean isUppercaseAlpha(char c) {
      return (c >= 'A') && (c <= 'Z');
    }

    public char toLowerAscii(char c) {
      if (isUppercaseAlpha(c)) {
        c += (char) 0x20;
      }
      return c;
    }

    /**
     * 将驼峰风格替换为下划线风格
     */
    @Override
    public String convert(String str) {
      final int size;
      final char[] chars;
      final StringBuilder sb = new StringBuilder(
          (size = (chars = str.toCharArray()).length) * 3 / 2 + 1);
      char c;
      for (int i = 0; i < size; i++) {
        c = chars[i];
        if (isUppercaseAlpha(c)) {
          sb.append('_').append(toLowerAscii(c));
        } else {
          sb.append(c);
        }
      }
      return sb.charAt(0) == '_' ? sb.substring(1) : sb.toString();
    }
  };

  /**
   * 获取实例，默认驼峰转下划线，可以通过 SPI 覆盖 {@link NameConvert} 实现
   *
   * @return 实现
   */
  static NameConvert getInstance() {
    return NameConvertInstance.getInstance();
  }

  /**
   * 转换名称
   *
   * @param name
   * @return
   */
  String convert(String name);

  /**
   * 转换实体名
   *
   * @param entityClass 实体类
   * @return 名称，一般用于表名
   */
  default String convertEntityClass(Class<?> entityClass) {
    return convert(entityClass.getSimpleName());
  }

  /**
   * 转换实体字段
   *
   * @param field 字段
   * @return 名称，一般用于列名
   */
  default String convertEntityField(EntityField field) {
    return convert(field.getName());
  }

  class NameConvertInstance {
    private static volatile NameConvert INSTANCE;

    public static NameConvert getInstance() {
      if (INSTANCE == null) {
        synchronized (NameConvert.class) {
          if (INSTANCE == null) {
            List<NameConvert> instances = ServiceLoaderUtil.getInstances(NameConvert.class);
            if (instances.size() > 0) {
              INSTANCE = instances.get(0);
            } else {
              INSTANCE = DEFAULT;
            }
          }
        }
      }
      return INSTANCE;
    }
  }

}

package io.mybatis.provider;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;
import java.util.Map;

@Accessors(fluent = true)
public class EntityProps {

  /**
   * 附加属性，用于扩展
   */
  @Getter
  protected Map<String, Object> props;

  /**
   * 获取属性值
   *
   * @param prop
   * @return
   */
  public Object getProp(String prop) {
    return props != null ? props.get(prop) : null;
  }

  /**
   * 设置属性值
   *
   * @param prop
   * @param value
   */
  public void putProp(String prop, Object value) {
    if (props == null) {
      props = new LinkedHashMap<>();
    }
    props.put(prop, value);
  }
}

package io.mybatis.provider;

import io.mybatis.config.ConfigHelper;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Accessors(fluent = true)
public class EntityProps<T extends EntityProps> {

  /**
   * 附加属性，用于扩展
   */
  @Getter
  protected Properties props;

  /**
   * 附加属性的类型
   */
  @Getter
  protected Map<String, Class<?>> propTypeMap;

  /**
   * 获取属性值
   *
   * @param prop 属性名
   * @return 属性值
   */
  public <V> V getProp(String prop) {
    if (prop == null || prop.isEmpty()) {
      return null;
    }
    Object val = props != null ? (V) props.get(prop) : null;
    // 如果配置值不存在，从全局获取配置
    if (val == null) {
      val = ConfigHelper.getStr(prop);
    }
    if (val == null) {
      return (V) val;
    }
    if (!(val instanceof String)) {
      return (V) val;
    }
    if (propTypeMap.containsKey(prop)) {
      Class<?> type = propTypeMap.get(prop);
      if (type != null) {
        return (V) convertValue((String) val, type);
      }
    }
    return (V) val;
  }

  /**
   * 获取属性值
   *
   * @param prop 属性名
   * @param def  默认值
   * @return 属性值
   */
  public <V> V getProp(String prop, V def) {
    V val = getProp(prop);
    return val != null ? val : def;
  }

  /**
   * 设置属性值
   *
   * @param prop  属性名
   * @param value 属性值
   */
  public T setProp(String prop, Object value, Class<?> type) {
    if (props == null) {
      props = new Properties();
      propTypeMap = new HashMap<>();
    }
    props.put(prop, value);
    propTypeMap.put(prop, type);
    return (T) this;
  }

  /**
   * 设置属性值
   *
   * @param prop  属性名
   * @param value 属性值
   */
  public T setProp(String prop, Object value) {
    return setProp(prop, value, value != null ? value.getClass() : null);
  }

  /**
   * 设置属性值
   *
   * @param prop 注解信息
   */
  public T setProp(Entity.Prop prop) {
    return setProp(prop.name(), prop.value(), prop.type());
  }

  /**
   * 设置属性值，这里不是替换原 props，而且整体追加到原 props
   *
   * @param props 属性
   */
  public T setProps(Map<String, ?> props) {
    if (props != null && !props.isEmpty()) {
      for (Map.Entry<String, ?> entry : props.entrySet()) {
        setProp(entry.getKey(), entry.getValue());
      }
    }
    return (T) this;
  }

  /**
   * 删除属性值
   *
   * @param prop 属性名
   * @param <V>
   * @return 被删除属性名对应的属性值
   */
  public <V> V removeProp(String prop) {
    if (props != null) {
      V value = getProp(prop);
      props.remove(prop);
      return value;
    } else {
      return null;
    }
  }

  /**
   * 获取属性值
   *
   * @param value 值
   * @param type  类型
   * @return 转换后的值
   */
  public static Object convertValue(String value, Class<?> type) {
    if (type == Boolean.class) {
      return Boolean.parseBoolean(value);
    } else if (type == Integer.class) {
      return Integer.parseInt(value);
    } else if (type == Long.class) {
      return Long.parseLong(value);
    } else if (type == Double.class) {
      return Double.parseDouble(value);
    } else if (type == Float.class) {
      return Float.parseFloat(value);
    }
    return value;
  }

}

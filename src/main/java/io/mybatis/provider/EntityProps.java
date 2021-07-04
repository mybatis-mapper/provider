package io.mybatis.provider;

import io.mybatis.provider.util.Utils;
import lombok.Getter;
import lombok.experimental.Accessors;

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
   * 获取属性值
   *
   * @param prop 属性名
   * @return 属性值
   */
  public <V> V getProp(String prop) {
    return props != null ? (V) props.get(prop) : null;
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
  public T setProp(String prop, Object value) {
    if (props == null) {
      props = new Properties();
    }
    props.put(prop, value);
    return (T) this;
  }

  /**
   * 设置属性值
   *
   * @param prop 注解信息
   */
  public T setProp(Entity.Prop prop) {
    if (props == null) {
      props = new Properties();
    }
    props.put(prop.name(), getEntityPropValue(prop));
    return (T) this;
  }

  /**
   * 设置属性值，这里不是替换原 props，而且整体追加到原 props
   *
   * @param props 属性
   */
  public <K, V> T setProps(Map<K, V> props) {
    if (this.props == null) {
      this.props = new Properties();
    }
    if (Utils.isNotEmpty(props)) {
      this.props.putAll(props);
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
   * @param prop
   * @return
   */
  public static Object getEntityPropValue(Entity.Prop prop) {
    Class type = prop.type();
    if (type == Boolean.class) {
      return Boolean.parseBoolean(prop.value());
    } else if (type == Integer.class) {
      return Integer.parseInt(prop.value());
    } else if (type == Long.class) {
      return Long.parseLong(prop.value());
    } else if (type == Double.class) {
      return Double.parseDouble(prop.value());
    } else if (type == Float.class) {
      return Float.parseFloat(prop.value());
    }
    return prop.value();
  }

}

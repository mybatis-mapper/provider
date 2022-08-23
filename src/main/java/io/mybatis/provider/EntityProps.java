/*
 * Copyright 2020-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.mybatis.provider;

import io.mybatis.config.ConfigHelper;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Accessors(fluent = true)
public class EntityProps<T extends EntityProps> {

  /**
   * 附加属性，用于扩展
   */
  @Getter
  protected Map<String, String> props;

  /**
   * 获取属性值
   *
   * @param prop 属性名
   */
  public String getProp(String prop) {
    if (prop == null || prop.isEmpty()) {
      return null;
    }
    String val = props != null ? props.get(prop) : null;
    // 如果配置值不存在，从全局获取配置
    if (val == null) {
      val = ConfigHelper.getStr(prop);
    }
    return val;
  }

  /**
   * 获取属性值
   *
   * @param prop         属性名
   * @param defaultValue 默认值
   */
  public String getProp(String prop, String defaultValue) {
    String val = getProp(prop);
    return val != null ? val : defaultValue;
  }

  /**
   * 获取整型值
   *
   * @param prop 参数
   */
  public Integer getPropInt(String prop) {
    String val = getProp(prop);
    if (val != null) {
      return Integer.parseInt(val);
    }
    return null;
  }

  /**
   * 获取整型值
   *
   * @param prop         参数
   * @param defaultValue 默认值
   */
  public Integer getPropInt(String prop, Integer defaultValue) {
    Integer val = getPropInt(prop);
    return val != null ? val : defaultValue;
  }

  /**
   * 获取布尔值
   *
   * @param prop 参数
   */
  public Boolean getPropBoolean(String prop) {
    String val = getProp(prop);
    return Boolean.parseBoolean(val);
  }

  /**
   * 获取布尔值
   *
   * @param prop         参数
   * @param defaultValue 默认值
   */
  public Boolean getPropBoolean(String prop, Boolean defaultValue) {
    String val = getProp(prop);
    return val != null ? Boolean.parseBoolean(val) : defaultValue;
  }

  /**
   * 设置属性值
   *
   * @param prop  属性名
   * @param value 属性值
   */
  public T setProp(String prop, String value) {
    if (this.props == null) {
      synchronized (this) {
        if (this.props == null) {
          this.props = new HashMap<>();
        }
      }
    }
    this.props.put(prop, value);
    return (T) this;
  }

  /**
   * 设置属性值
   *
   * @param prop 注解信息
   */
  public T setProp(Entity.Prop prop) {
    return setProp(prop.name(), prop.value());
  }

  /**
   * 设置属性值，这里不是替换原 props，而且整体追加到原 props
   *
   * @param props 属性
   */
  public T setProps(Map<String, String> props) {
    if (props != null && !props.isEmpty()) {
      for (Map.Entry<String, String> entry : props.entrySet()) {
        setProp(entry.getKey(), entry.getValue());
      }
    }
    return (T) this;
  }

  /**
   * 删除属性值
   *
   * @param prop 属性名
   * @return 被删除属性名对应的属性值
   */
  public String removeProp(String prop) {
    if (props != null) {
      String value = getProp(prop);
      props.remove(prop);
      return value;
    } else {
      return null;
    }
  }

}

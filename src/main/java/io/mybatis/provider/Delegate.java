/*
 * Copyright 2020 the original author or authors.
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

import java.util.Optional;

/**
 * 代理链封装，多级代理时，提供 {@link Delegate#delegate(Class)} 方法从多层代理中获取指定类型的对象
 */
public class Delegate<T extends Delegate> {
  protected T delegate;

  public Delegate(T delegate) {
    this.delegate = delegate;
  }

  /**
   * 递归获取代理层次中的指定类型
   *
   * @param delegateClass 指定的类型
   * @param <U>
   * @return
   */
  public <U extends T> Optional<U> delegate(Class<U> delegateClass) {
    if (delegateClass.isInstance(this)) {
      return Optional.of((U) this);
    } else if (delegate == null) {
      return Optional.empty();
    } else if (delegateClass.isInstance(delegate)) {
      return Optional.of((U) delegate);
    }
    return delegate.delegate(delegateClass);
  }

}

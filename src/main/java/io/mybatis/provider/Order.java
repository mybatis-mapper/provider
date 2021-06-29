package io.mybatis.provider;

public interface Order {

  /**
   * @return 执行顺序，数字越大优先级越高，越早执行
   */
  default int getOrder() {
    return 0;
  }

}

package io.mybatis.provider.util;

import io.mybatis.provider.Order;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * SPI 工具类
 *
 * @author liuzh
 */
public class ServiceLoaderUtil {

  /**
   * 获取实现
   *
   * @param clazz
   * @param <T>
   * @return
   */
  public static <T> List<T> getInstances(Class<T> clazz) {
    //SPI获取所有实现类
    ServiceLoader<T> serviceLoader = ServiceLoader.load(clazz);
    List<T> instances = new ArrayList<>();
    for (T factory : serviceLoader) {
      instances.add(factory);
    }
    if (instances.size() > 1 && Order.class.isAssignableFrom(clazz)) {
      instances.sort(Comparator.comparing(f -> ((Order) f).getOrder()).reversed());
    }
    return instances;
  }

}

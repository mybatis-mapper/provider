# 通用 Mapper 核心实现 Provider

核心中定义了对象和表之间映射关系数据的结构和获取方式，通过 SPI 支持部分自定义扩展。

核心中也定义了 Provider 中的实现中需要返回 namespace.methodName 类型的字符串（而不是SQL），通过中间缓存在 `Caching` 中获取真正的 SQL 信息。

当前项目没有直接提供可用的通用方法，方法在 **mybatis-mapper/mapper**( [gitee](https://gitee.com/mybatis-mapper/mapper)
| [GitHub](https://github.com/mybatis-mapper/mapper) )中提供。

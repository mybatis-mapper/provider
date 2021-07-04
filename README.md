# 通用 Mapper 核心实现 Provider

核心中定义了对象和表之间映射关系数据的结构和获取方式，通过 SPI 支持部分自定义扩展。

核心中也定义了 Provider 中的实现中需要返回 `namespace.methodName` 类型的字符串（而不是SQL），通过中间缓存在 `Caching` 中获取真正的 SQL 信息。

当前项目没有直接提供可用的通用方法，方法在 **mybatis-mapper/mapper**( [gitee](https://gitee.com/mybatis-mapper/mapper)
| [GitHub](https://github.com/mybatis-mapper/mapper) )中提供。

## 注解

核心提供了一套实体类的注解，简单示例如下：

```java

@Entity.Table(value = "user")
public class User {
  @Entity.Column(id = true)
  private Long   id;
  @Entity.Column("name")
  private String userName;
  @Entity.Column
  private String sex;
  //省略其他
}
```

除了最基本的注解配置外，还有更多可配置的属性，下面是个复杂的例子：

```java
//autoResultMap 自动生成 <resultMap> 结果映射，支持查询结果中的 typeHandler 等配置
@Entity.Table(value = "sys_user", remark = "系统用户", autoResultMap = true,
  props = {
    //deleteByExample方法中的Example条件不能为空，默认允许空，另外两个配置类似
    @Entity.Prop(name = "deleteByExample.allowEmpty", value = "false", type = Boolean.class),
    @Entity.Prop(name = "updateByExample.allowEmpty", value = "false", type = Boolean.class),
    @Entity.Prop(name = "updateByExampleSelective.allowEmpty", value = "false", type = Boolean.class)
  })
public class User {
  @Entity.Column(id = true, remark = "主键", updatable = false, insertable = false)
  private Long    id;
  @Entity.Column(value = "name", remark = "帐号")
  private String  name;
  @Entity.Column(value = "is_admin", remark = "是否为管理员", updatable = false)
  private boolean admin;
  @Entity.Column(remark = "顺序号", orderBy = "DESC")
  private Integer seq;
  @Entity.Column(numericScale = "4", remark = "积分（保留4位小数）")
  private Double  points;
  @Entity.Column(selectable = false, remark = "密码")
  private String  password;
  @Entity.Column(value = "when_created", remark = "创建时间", jdbcType = JdbcType.TIMESTAMP)
  private Date    whenCreated;
  @Entity.Column(remark = "介绍", typeHandler = StringTypeHandler.class)
  private String  info;
  //不是表字段
  private String  noEntityColumn;
  //省略其他
}
```

## 拼接 SQL 的方法

提供了 `SqlScript` 类用于拼接 XML 形式的 SQL，简单示例如下：

```java
class DemoProvider {
  /**
   * 根据主键删除
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String deleteByPrimaryKey(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, entity -> "DELETE FROM " + entity.table()
      + " WHERE " + entity.idColumns().stream().map(EntityColumn::columnEqualsProperty).collect(Collectors.joining(" AND ")));
  }
}
```

`SqlScript.caching` 会缓存拼接 SQL 的 lambda 方法，并且返回方法的 id。

> **特别注意，这里返回的不是 sql，而且缓存 SQL 后的 key**  
> key值形式如：`io.mybatis.mapper.UserMapper.deleteByPrimaryKey`。

上面方法在执行时，最终拼接的 SQL 示例如下：

```xml

<script>
  DELETE FROM user WHERE id = #{id}
</script>
```

复杂一点的：

```java
class DemoProvider {
  /**
   * 根据主键查询实体
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String selectByPrimaryKey(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, new SqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        return "SELECT " + entity.baseColumnAsPropertyList()
          + " FROM " + entity.table()
          + where(() -> entity.idColumns().stream().map(EntityColumn::columnEqualsProperty).collect(Collectors.joining(" AND ")));
      }
    });
  }
}
```

上面方法在执行时，最终拼接的 SQL 示例如下：

```xml
<script>
  SELECT id,name AS userName,sex FROM user
  <where>
    id = #{id}
  </where>
</script>
```

更复杂的：

```java
class DemoProvider {
  /**
   * 保存实体中不为空的字段
   *
   * @param providerContext 上下文
   * @return cacheKey
   */
  public static String insertSelective(ProviderContext providerContext) {
    return SqlScript.caching(providerContext, new SqlScript() {
      @Override
      public String getSql(EntityTable entity) {
        return "INSERT INTO " + entity.table()
          + trimSuffixOverrides("(", ")", ",", () ->
          entity.insertColumns().stream().map(column ->
            ifTest(column.notNullTest(), () -> column.column() + ",")
          ).collect(Collectors.joining(LF)))
          + trimSuffixOverrides(" VALUES (", ")", ",", () ->
          entity.insertColumns().stream().map(column ->
            ifTest(column.notNullTest(), () -> column.variables() + ",")
          ).collect(Collectors.joining(LF)));
      }
    });
  }
}
```

上面方法在执行时，最终拼接的 SQL 示例如下：

```xml
<script>
  INSERT INTO user
  <trim prefix="(" suffixOverrides="," suffix=")">
    <if test="id != null">
      id,
    </if>
    <if test="userName != null">
      name,
    </if>
    <if test="sex != null">
      sex,
    </if>
  </trim>
  <trim prefix=" VALUES (" suffixOverrides="," suffix=")">
    <if test="id != null">
      #{id},
    </if>
    <if test="userName != null">
      #{userName},
    </if>
    <if test="sex != null">
      #{sex},
    </if>
  </trim>
</script>
```

更多用法文档看 **mybatis-mapper/mapper**( [gitee](https://gitee.com/mybatis-mapper/mapper)
| [GitHub](https://github.com/mybatis-mapper/mapper) )。

## `EntityTable` 和 `EntityColumn`

`EntityTable` 和 `EntityColumn` 记录了实体类和表之间的映射信息和额外的很多配置信息。

这两个类中提供了大量便于在 XML 中使用的方法，例如: `entity.insertColumns()`，这个方法会把 `insertable=false`的排除后返回。

还有 `EntityColumn::columnEqualsProperty` 这样的方法返回 `column = #{property}` 形式的字符串。

为了使用上的统一，应该尽可能使用提供的现成方法，尽量避免自己拼接常用的字符串。

## 扩展

主要提供下面 3 个支持 SPI 扩展的接口：

1. `EntityClassFinder` 如何找到执行方法的实体类类型
2. `EntityTableFactory` 根据实体类构造 `EntityTable` 对象（表信息）
3. `EntityColumnFactory` 根据实体类中的字段构建 `EntityColumn` 对象（列信息）

更具体的信息可以查看本项目中的默认实现，还有 **mybatis-mapper/mapper**( [gitee](https://gitee.com/mybatis-mapper/mapper)
| [GitHub](https://github.com/mybatis-mapper/mapper) ) 中提供的 **jpa** 实现。

更复杂的还有一个兼容 **tk-mapper** 的项目：**mybatis-mapper/tk-mapper**( [gitee](https://gitee.com/mybatis-mapper/tk-mapper)
| [GitHub](https://github.com/mybatis-mapper/tk-mapper) ) 的实现。

## Caching - LanguageDriver

这是整个通用机制的核心，`Caching` 实现了 `LanguageDriver` 接口， 允许 Provider 方法的实现返回缓存后的方法 key， 在真正执行的时候，再从缓存中找到 SQL 真正执行。

为了让 `Caching` 生效，需要在接口方法添加 `@Lang(Caching.class)` 注解，例如：

```java
class DemoMapper<T> {
  /**
   * 根据主键查询实体
   *
   * @param id 主键
   * @return 实体
   */
  @Lang(Caching.class)
  @SelectProvider(type = EntityProvider.class, method = "selectByPrimaryKey")
  Optional<T> selectByPrimaryKey(I id);
}
```
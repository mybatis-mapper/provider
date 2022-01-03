package io.mybatis.provider;

import io.mybatis.provider.model.User;
import io.mybatis.provider.util.Utils;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;

public class NameConvertTest {

  @Test
  public void test() {
    Assert.assertEquals("user", Utils.convert("User"));
    Assert.assertEquals("user_name", Utils.convert("UserName"));
    Assert.assertEquals("user_name", Utils.convert("userName"));
    Assert.assertEquals("u_f_o", Utils.convert("UFO"));
    Assert.assertEquals("u_f_o", Utils.convert("uFO"));
    Assert.assertEquals("user1", Utils.convert("User1"));
    Assert.assertEquals("user2_role", Utils.convert("User2Role"));
    Assert.assertEquals("user", Utils.convertEntityClass(User.class));

    try {
      Field username = User.class.getDeclaredField("username");
      EntityField entityField = new EntityField(User.class, username);
      Assert.assertEquals("username", Utils.convertEntityField(entityField));
    } catch (NoSuchFieldException e) {
      Assert.fail();
    }
  }

}

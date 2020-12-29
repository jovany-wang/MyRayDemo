package wang.jovany;

import io.ray.api.ObjectRef;
import io.ray.api.Ray;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DemoTest {

  private static String echo(String str) {
    return str;
  }

  @Test
  public void testDemo() {
    Ray.init();

    ObjectRef<String> obj = Ray.task(DemoTest::echo, "hello Ray!").remote();
    Assert.assertEquals("hello Ray!", Ray.get(obj));

    Ray.shutdown();
  }
}

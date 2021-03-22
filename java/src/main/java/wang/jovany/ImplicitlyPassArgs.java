package wang.jovany;

import io.ray.api.ObjectRef;
import io.ray.api.Ray;
import org.testng.annotations.Test;

public class ImplicitlyPassArgs {

  static String echo(String str) {
    return str;
  }

  @Test
  public void testPassArgs() {
    Ray.init();
    ObjectRef<String> s = Ray.put("abc");
    ObjectRef<String> obj = Ray.task(ImplicitlyPassArgs::echo, s).remote();
    System.out.println(obj.get());
    Ray.shutdown();
  }
}

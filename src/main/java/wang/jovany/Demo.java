package wang.jovany;

import io.ray.api.ObjectRef;
import io.ray.api.Ray;

public class Demo {

  private static String echo(String str) {
    return str;
  }

  public static void main(String[] args) {
    Ray.init();

    ObjectRef<String> obj = Ray.task(Demo::echo, "hello Ray!").remote();
    System.out.println(Ray.get(obj));

    Ray.shutdown();
  }
}

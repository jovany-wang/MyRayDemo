package wang.jovany;

import io.ray.api.ActorHandle;
import io.ray.api.Ray;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Demo {

  public static class MyActor {
    public String echo(String str) {
      return str;
    }
  }

  public static void main(String[] args) throws InterruptedException {
    System.setProperty("ray.job.long-running", "true");
//    System.setProperty("ray.job.id", "0001");
    Ray.init();

    if(args == null || args.length != 3) {
      throw new RuntimeException("args is null or args.length is not 2.");
    }


    ActorHandle<MyActor> myActor = Ray.actor(MyActor::new).remote();

    while (true) {
      myActor.task(MyActor::echo, "hello my world.").remote().get();
      TimeUnit.MILLISECONDS.sleep(50);
      if (args.length > 100) {
        break;
      }
    }

    final boolean asyncCreateActors = args[0].equalsIgnoreCase("true");
    final int numActors = Integer.parseInt(args[1]);
    final boolean invokeShutdown = args[2].equalsIgnoreCase("true");
    System.out.println("driver args: asyncCreateActors=" + asyncCreateActors
            + ", numActors=" + numActors + ",invokeShutdown=" + invokeShutdown);

    List<ActorHandle<MyActor>> handles = new ArrayList<>();
    for(int i = 0; i < numActors; ++i) {
      handles.add(Ray.actor(MyActor::new).remote());
    }

    if(!asyncCreateActors) {
      handles.forEach(actorHandle -> {
        String ret = (actorHandle).task(MyActor::echo, "hello").remote().get();
        System.out.println("Succeed: ret is" + ret);
      });
    }

    System.out.println("This Demo driver is shutting down.");
    if (invokeShutdown) {
      Ray.shutdown();
    }
  }
}

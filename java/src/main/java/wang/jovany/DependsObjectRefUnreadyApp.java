package wang.jovany;

import io.ray.api.ActorHandle;
import io.ray.api.ObjectRef;
import io.ray.api.Ray;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DependsObjectRefUnreadyApp {

  public static class SlowSayHelloActor {

    public String slowSayHello() {
      try {
        TimeUnit.SECONDS.sleep(5);
      } catch (InterruptedException e) {
        System.out.println("Failed to sleep in slowSayHello.");
      }
      return "hello";
    }

  }

  public static class MyActor {

    private String echoStr = "";

    private int index = -1;

    public MyActor(ObjectRef<String> obj, Integer index) {
      echoStr = obj.get();
      this.index = index;
    }

    public String echo() {
      return echoStr + "_" + String.valueOf(index);
    }
  }

  public static void main(String[] args) {

    if(args == null || args.length != 3) {
      throw new RuntimeException("args is null or args.length is not 3.");
    }

    final boolean asyncCreateActors = args[0].equalsIgnoreCase("true");
    final int numActors = Integer.parseInt(args[1]);
    final boolean invokeShutdown = args[2].equalsIgnoreCase("true");
    System.out.println("driver args: asyncCreateActors=" + asyncCreateActors
            + ", numActors=" + numActors + ",invokeShutdown=" + invokeShutdown);

    Ray.init();

    // Create 1 slow actor.
    ActorHandle<SlowSayHelloActor> slowActor = Ray.actor(SlowSayHelloActor::new).remote();

    List<ActorHandle<DependsObjectRefUnreadyApp.MyActor>> handles = new ArrayList<>();
    for(int i = 0; i < numActors; ++i) {
      ObjectRef<String> obj = slowActor.task(SlowSayHelloActor::slowSayHello).remote();
      handles.add(Ray.actor(MyActor::new, obj, i).setMaxRestarts(-1).remote());
    }

    if(!asyncCreateActors) {
      handles.forEach(actorHandle -> {
        String ret = (actorHandle).task(MyActor::echo).remote().get();
        System.out.println("DependenciesUnreadyApp Succeed: ret is " + ret);
      });
    }

    System.out.println("This DependenciesUnreadyApp driver is shutting down.");
    if (invokeShutdown) {
      Ray.shutdown();
    }
  }

}

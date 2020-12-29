package wang.jovany;

import io.ray.api.ActorHandle;
import io.ray.api.Ray;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DependsActorUnreadyApp {

  public static class SlowActor {

    public SlowActor() {
      try {
        TimeUnit.SECONDS.sleep(5);
      } catch (InterruptedException e) {
        System.out.println("Failed to sleep in slowSayHello.");
      }
    }

    public String echo() {
      return "hello";
    }

  }

  public static class DownstreamActor {

    private String echoStr = "";

    private int index = -1;

    public DownstreamActor(ActorHandle<SlowActor> actorHandle, Integer index) {
      echoStr = actorHandle.task(SlowActor::echo).remote().get();
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
    System.out.println("DependsActorUnreadyApp driver args: asyncCreateActors=" + asyncCreateActors
            + ", numActors=" + numActors + ",invokeShutdown=" + invokeShutdown);

    Ray.init();

    List<ActorHandle<DownstreamActor>> handles = new ArrayList<>();
    for(int i = 0; i < numActors; ++i) {
      ActorHandle<SlowActor> slowActor = Ray.actor(SlowActor::new).remote();
      handles.add(Ray.actor(DownstreamActor::new, slowActor, i).setMaxRestarts(-1).remote());
    }

    if(!asyncCreateActors) {
      handles.forEach(actorHandle -> {
        String ret = (actorHandle).task(DownstreamActor::echo).remote().get();
        System.out.println("DependsActorUnreadyApp Succeed: ret is " + ret);
      });
    }

    System.out.println("This DependsActorUnreadyApp driver is shutting down.");
    if (invokeShutdown) {
      Ray.shutdown();
    }
  }

}

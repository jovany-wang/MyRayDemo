package wang.jovany;

import io.ray.api.ActorHandle;
import io.ray.api.ObjectRef;
import io.ray.api.Ray;
import io.ray.api.id.ActorId;
import io.ray.api.id.TaskId;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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

  public static class RuntimeContextTester {

    public String testRuntimeContext(ActorId actorId) {
      Assert.assertNotEquals(Ray.getRuntimeContext().getCurrentTaskId(), TaskId.NIL);
      Assert.assertEquals(actorId, Ray.getRuntimeContext().getCurrentActorId());
      return "ok";
    }

    public String testRuntimeContextInRunnable(ActorId actorId) throws InterruptedException, ExecutionException {
      CompletableFuture<ActorId> future =new CompletableFuture<>();
      Runnable runnable = Ray.wrapRunnable(() -> {
        future.complete(Ray.getRuntimeContext().getCurrentActorId());
      });
      Thread th = new Thread(runnable);
      th.setName("11111");
      th.start();
      th.join();
      Assert.assertEquals(actorId, future.get());
      return "ok";
    }
  }

  @Test
  public void testRuntimeContextInActor() {

    Ray.init();
    ActorHandle<RuntimeContextTester> actor = Ray.actor(RuntimeContextTester::new).remote();
    Assert.assertEquals(
            "ok", actor.task(RuntimeContextTester::testRuntimeContext, actor.getId()).remote().get());
    Ray.shutdown();
  }

  @Test
  public void testGetCurrentActorInRunnable() {
    System.setProperty("ray.run-mode", "SINGLE_PROCESS");
    Ray.init();
    ActorHandle<RuntimeContextTester> actor = Ray.actor(RuntimeContextTester::new).remote();
    Assert.assertEquals(
            "ok", actor.task(RuntimeContextTester::testRuntimeContextInRunnable, actor.getId()).remote().get());
    Ray.shutdown();
  }

}

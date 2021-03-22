package wang.jovany;

public class JsonDemo {

  public static void main(String[] args) {
    if (args.length < 1) {
      throw new IllegalArgumentException(
              "Failed to start DefaultDriver: driver class name was not specified.");
    }
    String driverClassName = args[0];
    int driverArgsLength = args.length - 1;
    String[] driverArgs = null;
    if (driverArgsLength > 0) {
      driverArgs = new String[driverArgsLength];
      System.arraycopy(args, 1, driverArgs, 0, driverArgsLength);
    } else {
      driverArgs = new String[0];
    }

    System.out.println("driver args: " + driverArgs[0]);
  }

}

package gravity.cli;

public final class Main {
  private Main() {}

  public static void main(String[] args) {
    System.setProperty("gravity.packaged.jvm.cli", "true");
    String[] delegated = new String[args.length + 2];
    delegated[0] = "-m";
    delegated[1] = "gravity.bootstrap";
    System.arraycopy(args, 0, delegated, 2, args.length);
    clojure.main.main(delegated);
  }
}

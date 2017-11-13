import java.nio.file.*;
import java.io.IOException;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class DMXTester {
  public static void main(String[] args) {
    DMXDriver driver = null;

    try {
      driver = new DMXDriver();
    } catch (IOException ex) {
      ex.printStackTrace();
      System.out.println("Make sure you ran insmod and started this program as root.");
      System.exit(-1);
    }

    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    boolean quit = false;
    String input = null;
    String[] splitInput = null;

    printHelp();

    do {
      System.out.print(">");
      try {
        input = reader.readLine().toLowerCase();
        splitInput = input.split(" ");
      } catch (IOException ex) {
        ex.printStackTrace();
        System.exit(-1);
      }
      if (splitInput[0].startsWith("quit")) {
        quit = true;
      } else if (splitInput[0].startsWith("clear")) {
        try {
          driver.clearDMX();
        } catch (IOException ex) {
          ex.printStackTrace();
          System.exit(-1);
        }
      } else if (splitInput[0].startsWith("set")) {
        int address, value;
        try {
          if (splitInput.length != 3)
            throw new IllegalArgumentException("Set command requires two arguments");
          address = Integer.parseInt(splitInput[1]);
          value = Integer.parseInt(splitInput[2]);
          driver.setDMX(address, value);
        } catch (IllegalArgumentException ex) {
          System.out.println(ex.getMessage());
          printHelp();
        } catch (IOException ex) {
          ex.printStackTrace();
          System.exit(-1);
        }
      } else if (input.startsWith("help")) {
        printHelp();
      }
    } while (!quit);
  }

  public static void printHelp() {
    System.out.println("Commands:");
    System.out.println("\tquit");
    System.out.println("\tclear");
    System.out.println("\tset addr val");
    System.out.println("\thelp");
  }
}

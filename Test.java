import java.util.Scanner;

public class Test {
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    Solution solution = new Solution();
    // while(true){
    //   System.out.print("Enter Commands: ");
    //   String input = scanner.nextLine();
    //   solution.runCommand(input);
    // }

    solution.runCommand("after 10 20");  // ✅ Should work
    solution.runCommand("after 20 30");  // ✅ Should work
    solution.runCommand("after 30 40");  // ✅ Should work
    solution.runCommand("after 40 10");  // ❌ Should detect cycle
    solution.runCommand("after 10 10");  // ❌ Should detect cycle
  }
}

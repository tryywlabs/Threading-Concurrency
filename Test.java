import java.util.Scanner;

public class Test {
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    Solution solution = new Solution();
    while(true){
      System.out.print("Enter Commands: ");
      String input = scanner.nextLine();
      solution.runCommand(input);
    }
  }
}

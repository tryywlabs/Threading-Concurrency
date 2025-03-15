import java.lang.Thread;
import java.util.NoSuchElementException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.lang.NumberFormatException;

public class Solution implements CommandRunner {
static List<Thread> runningThreads;
static HashMap<String, String> finishedThreads;
SlowCalculator calc;

//METHOD: Solution Class Constructor
  public Solution(){
    runningThreads = new ArrayList<Thread>();
    finishedThreads = new HashMap<>();
  }

  //METHOD: runCommand
  public String runCommand(String command){
    if(command == null || command.trim().isEmpty()){
      return "Invalid Command";
    }
    //Split command input by whitespace, depending on length of split string the evaluation of command will differ.
    command = command.toLowerCase();
    String[] parsedCmd = command.split(" ");
    String inputCmd = parsedCmd[0];
    String inputLong1 = "";
    String inputLong2 = "";
    String res;
    
    if(parsedCmd.length > 1){
      inputLong1 = parsedCmd[1];
      //only evaluate 3rd input if string is long enough
      if(parsedCmd.length >2 ){
        inputLong2 = parsedCmd[2];
      }
    }
    //only evaluate 2nd input if string is long enough
    switch(inputCmd){
      case "start": 
        res = start(inputLong1);
        break;
      case "cancel":
        cancel(inputLong1);
        break;
      case "running":
        running();
        break;
      case "get":
        get(inputLong1);
        break;
      case "after":
        after(inputLong1, inputLong2);
        break;
      case "finish":
        finish();
        break;
      case "abort":
        abort();
        break;
        default:
          System.out.println("\nValid Commands:");
          System.out.println("\tstart N");
          System.out.println("\tcancel N");
          System.out.println("\trunning");
          System.out.println("\tget N");
          System.out.println("\tafter N M");
          System.out.println("\tfinish");
          System.out.println("\tabort\n");
      }
      //explanation: when calculation finishes, store in key-value pair of long-result
      for(Thread thread : runningThreads){
        if(!thread.isAlive()){
          finishedThreads.put(inputLong1, calc.getRes());
          runningThreads.remove(thread);
        }
      }
      return "";
  }

  /* METHODS: COMMAND IMPLEMENTATION METHODS */
  /*
   * 1. START
   * 2. CANCEL
   * 3. RUNNING
   * 4. GET
   * 5. AFTER
   * 6. FINISH
   * 7. ABORT
   */

  //COMMAND: START
  public String start(String inputLong){
    String res = "";
    try {
      Long N = Long.parseLong(inputLong);
      this.calc = new SlowCalculator(N);
      Thread thread = new Thread(calc, String.valueOf(N));
      System.out.printf("Started %d %n", N);
      thread.start();
      runningThreads.add(thread);
      res = calc.getRes();
    } catch (NumberFormatException e) {
      System.err.println("Your input cannot be converted to a number\n" + e);
    }
    return res;
  }

  //COMMAND: CANCEL
  public void cancel(String inputLong){
    try {
      for(Thread thread : runningThreads){
        if(thread.getName().equals(inputLong)){
          thread.interrupt();
          runningThreads.remove(thread);
          break;
        }
      }
    } catch (NoSuchElementException e) {
      System.err.println("Thread not found (yet)\n" + e);
    }
  }

  //COMMAND: RUNNING
  public void running(){
    int threads = runningThreads.size();
    if(threads > 0){
      System.out.printf("%d calculations running: ", threads);
      for(Thread thread : runningThreads){
        System.out.print(thread.getName() + " ");
      }
      System.out.print('\n');
    }
    else{
      System.out.println("no calculations running");
    }
  }

  //COMMAND: GET
  public void get(String inputLong){

  }

  //COMMAND: AFTER
  public void after(String inputLong1, String inputLong2){
    System.out.printf("%s will start after %s", inputLong1, inputLong2);
  }

  //COMMAND: FINISH
  public void finish(){

  }

  //COMMAND: ABORT
  public void abort(){
    for(Thread thread : runningThreads){
      thread.interrupt();
    }
    System.out.println("aborted");
  }
}
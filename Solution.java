import java.lang.Thread;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Solution implements CommandRunner {
private ConcurrentHashMap<Long, Thread> runningThreads;
private ConcurrentHashMap<Long, Integer> finishedThreads;
private ConcurrentHashMap<Long, Thread> cancelledThreads;
private ConcurrentHashMap<String, Thread> abortedThreads;
private ConcurrentHashMap<Long, List<Long>> dependencies;
private List<Long> circularPath = new ArrayList<>();

//METHOD: Solution Class Constructor
  public Solution(){
    runningThreads = new ConcurrentHashMap<>();
    finishedThreads = new ConcurrentHashMap<>();
    cancelledThreads = new ConcurrentHashMap<>();
    abortedThreads = new ConcurrentHashMap<>();
    dependencies = new ConcurrentHashMap<>();
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
    Long inputLong1 = null;
    Long inputLong2 = null;
    //only evaluate 2nd input if string is long enough
    if(parsedCmd.length > 1){
        inputLong1 = Long.parseLong(parsedCmd[1]);
      }
    //only evaluate 3rd input if string is long enough
    if(parsedCmd.length >2 ){
      inputLong2 = Long.parseLong(parsedCmd[2]);
    }
    switch(inputCmd){
      case "start": 
        return start(inputLong1);
      case "cancel":
        System.out.println(cancel(inputLong1));
        break;
      case "running":
        System.out.println(running());
        break;
      case "get":
        System.out.println(get(inputLong1));
        break;
      case "after":
        System.out.println(after(inputLong1, inputLong2));
        break;
      case "finish":
        System.out.println(finish());
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
      
      //explanation: when calculation finishes, store in key-value pair of long-result.
      //Check for every entered command
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
  public synchronized String start(Long N){
    SlowCalculator calc = new SlowCalculator(N);
    Thread thread = new Thread(){
      public void run(){
        calc.run();
        finishedThreads.put(N, calc.getResult());
        
        if(dependencies.contains(N)){
          List<Long> dependents = dependencies.remove(N);
          for(Long dependent : dependents){
            start(dependent);
          }
        }
      }
    };
    runningThreads.put(N, thread);
    thread.start();
    return "started " + N;
  }

  //COMMAND: CANCEL
  public synchronized String cancel(Long N){
    if(cancelledThreads.containsKey(N)){
      return "cancelled";
    }

    Thread thread = runningThreads.get(N);
    try {
      if (thread != null) {
        if(thread.isAlive()){
          thread.interrupt();
          thread.join();
          runningThreads.remove(N);
          cancelledThreads.put(N, thread);
        }
        else{
          return "";
        }
      }
      else{
        return "";
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return "cancelled " + N;
  }

  //COMMAND: RUNNING
  public synchronized String running(){
    List<String> activeThreads = new ArrayList<>();
    for(HashMap.Entry<Long, Thread> entry : runningThreads.entrySet()){
      Thread thread = entry.getValue();
      if(thread.isAlive()){
        activeThreads.add(entry.getKey().toString());
      }
    }

    int count = activeThreads.size();

    if(count == 0){
      return "no calculations running";
    }
    else {
      StringBuilder sb = new StringBuilder();
      sb.append(count + " calculations running: ");
      for(String threadName : activeThreads){
        sb.append(threadName + " ");
      }
      return sb.toString();
    }
  }

  //COMMAND: GET
  public String get(Long N){
    if(cancelledThreads.containsKey(N)){
      return "cancelled";
    }

    Thread thread = runningThreads.get(N);
    if(thread != null){
      if(thread.isAlive()){
        return "calculating";
      }
      else{
        if(finishedThreads.containsKey(N)){
          return "result is " + finishedThreads.get(N);
        }
      }
    }
    return "No such thread found";
  }

  //COMMAND: AFTER
  public String after(Long N, Long M){
    //Check for circular dependency (Refer to Helper Method)
    if(isCircularDependent(N, M)){
      return circularDependencyMessage(N, M);
    }
    
    //Check if N is completed, start M immediately
    if(finishedThreads.containsKey(N)){
      return start(M);
    }

    //If N isn't finished, start a new dependency chain
    if(!dependencies.containsKey(N)){
      dependencies.put(N, new ArrayList<>());
    }
    //Add 'after N M' to the dependecy chain
    dependencies.get(N).add(M);
    //join thread

    return M + " will start after " + N;
  }

  //TODO
  //COMMAND: FINISH
  public String finish(){
    System.out.println("finishing up all tasks...");
    if(runningThreads.isEmpty()){
      return "finished";
    }
    return "";
  }

  //TODO
  //COMMAND: ABORT
  public void abort(){
    // for(Thread thread : runningThreads){
    //   thread.interrupt();
    //   abortedThreads.put(thread.getName(), thread);
    // }
    // System.out.println("aborted");
  }


  /*
   * HELPERS: AFTER METHOD
   * HELPERS: AFTER METHOD
   */
  
   //HELPER 1: Discern Circular Dependency
  private boolean isCircularDependent(Long N, Long M){
    //YES: Immediate Cycle is detected
    if(M.equals(N)){
      return true;
    }

    //NO: Key is not listed as a head of dependency chain
    if(!dependencies.containsKey(M)){
      return false;
    }

    //Get all of M's dependents
    List<Long> dependents = dependencies.get(M);
    //return false if M has no dependents
    if(dependents.isEmpty()) return false;
    //return true if N depends on M
    if(dependents.contains(N)){
      return true;
    }
    //Iterate and recursively check for N after any of M's dependents
    for(Long dependent : dependents){
      if(isCircularDependent(N, dependent)){
        return true;
      }
    }

    return false;
  }

  private String circularDependencyMessage(Long N, Long M){
    StringBuilder sb = new StringBuilder();

    sb.append("circular dependency ");
    sb.append(M + " ");
    List<Long> depList = circularPath(N, M);
    sb.append(depList.size());
    sb.append(" " + N);

    return sb.toString();
  }

  private List<Long> circularPath(Long N, Long M){
    HashSet<Long> checked = new HashSet<>();
    findCircularPath(N, M, checked);
    return circularPath;
  }

  private boolean findCircularPath(Long N, Long M, HashSet<Long> checked){
    circularPath.add(N);
    checked.add(N);

    if(N.equals(M) && circularPath.size() > 1) return true;

    if(!dependencies.contains(N)){
      circularPath.remove(N);
      return false;
    }

    for(Long dependent : dependencies.get(N)){
      if(!checked.contains(dependent)){
        if(findCircularPath(dependent, M, checked)){
          circularPath.add(Integer.toUnsignedLong(1));
          return true;
        }
      }
    }
    circularPath.remove(N);
    return false;
  }
}
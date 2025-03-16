import java.lang.Thread;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Solution implements CommandRunner {

  /*
   * ATTRIBUTES
   */
  private ConcurrentHashMap<Long, Thread> runningThreads;   //Map of currently running threads
  private ConcurrentHashMap<Long, Integer> finishedThreads; //Map of calculation finished threads, Long and Result
  private ConcurrentHashMap<Long, Thread> cancelledThreads; //Map of cancelled threads
  private ConcurrentHashMap<Long, List<Long>> dependencies; //Map of a thread and its dependencies as a list
  private ConcurrentLinkedQueue<Long> waiting;
  private List<Long> circularPath = new ArrayList<>();      //List to track circular path, reset everytime used

  public boolean getRunning(){
    return runningThreads.isEmpty();
  }

  //METHOD: Solution Class Constructor
  public Solution(){
    runningThreads = new ConcurrentHashMap<>();
    finishedThreads = new ConcurrentHashMap<>();
    cancelledThreads = new ConcurrentHashMap<>();
    dependencies = new ConcurrentHashMap<>();
    waiting = new ConcurrentLinkedQueue<>();
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

    try {
      switch(inputCmd){
        case "start":
          if(parsedCmd.length != 2){
            return "Invalid command";
          }
          return start(inputLong1);
        case "cancel":
          if(parsedCmd.length != 2){
            return "Invalid command";
          }
          return cancel(inputLong1);
        case "running":
          if(parsedCmd.length != 1){
            return "Invalid command";
          }
          return running();
        case "get":
          if(parsedCmd.length != 2){
            return "Invalid command";
          }
          return get(inputLong1);
        case "after":
          if(parsedCmd.length != 3){
            return "Invalid command";
          }
          return after(inputLong1, inputLong2);
        case "finish":
          if(parsedCmd.length != 1){
            return "Invalid command";
          }
          return finish();
        case "abort":
          if(parsedCmd.length != 1){
            return "Invalid command";
          }
          return abort();
        default:
          return "Invalid command";
        }
    } catch (NumberFormatException e) {
      return "Invalid command";
    } catch (Exception e){
      return "Invalid command";
    }

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
        runningThreads.remove(N);
        
        if(dependencies.contains(N)){
          //remove key N from dependencies when N finishes, start all dependent threads
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

    if(waiting.contains(N)){
      return "waiting";
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
    //Check for circular dependency (Refer to Helper Methods)
    if(isCircularDependent(N, M)){
      return createCircularMessage(N, M);
    }
    
    //Check if N is already completed, immediately start M
    if(finishedThreads.containsKey(N)){
      waiting.remove(M);
      return start(M);
    }

    //If N isn't finished, start a new dependency chain
    if(!dependencies.containsKey(N)){
      dependencies.put(N, new ArrayList<>());
    }
    //Add 'after N M' to the dependecy chain
    dependencies.get(N).add(M);
    waiting.add(M);
    //join thread

    return M + " will start after " + N;
  }

  //COMMAND: FINISH
  public String finish(){
    System.out.println("finishing up all tasks...");
    List<Thread> allActiveThreads = new ArrayList<>();

    //Get all active threads
    for(HashMap.Entry<Long, Thread> entry : runningThreads.entrySet()){
      Thread thread = entry.getValue();
      if(thread != null && thread.isAlive()){
        allActiveThreads.add(thread);
      }
    }

    //Wait for all threads to complete
    for(Thread thread : allActiveThreads){
      try{
        thread.join();
      }
      catch(InterruptedException e){
        Thread.currentThread().interrupt();
      }
    }

    //Process any dependencies
    List<Long> waitingThreads = new ArrayList<>(dependencies.keySet());
    while(!dependencies.isEmpty()){
      for(Long N : waitingThreads){
        if(finishedThreads.contains(N) || cancelledThreads.contains(N)){
          List<Long> dependents = dependencies.remove(N);
          if(dependents != null){
            for(Long dependent : dependents){
              start(dependent);
            }
          }
        }
      }
    }

    return "finished";
  }

  //COMMAND: ABORT
  public String abort(){
    List<Thread> allThreads = new ArrayList<>();

    for(HashMap.Entry<Long, Thread> entry : runningThreads.entrySet()){
      Thread thread = entry.getValue();
      if(thread != null && thread.isAlive()){
        thread.interrupt();
        allThreads.add(thread);
      }
    }

    for(Thread thread : allThreads){
      try{
        thread.join();
      }
      catch(InterruptedException e){
        Thread.currentThread().interrupt();
      }
    }

    runningThreads.clear();
    finishedThreads.clear();
    cancelledThreads.clear();
    dependencies.clear();
    circularPath.clear();

    return "aborted";
  }


  /*
   * HELPERS: FOR AFTER METHOD
   * HELPERS: FOR AFTER METHOD
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

  //HELPER 2: Create string for message, calling the findCircularPath() method 
  private String createCircularMessage(Long N, Long M){
    StringBuilder sb = new StringBuilder();

    sb.append("circular dependency ");
    sb.append(N);
    List<Long> depList = circularPath(N, M);
    sb.append(depList.size());
    sb.append(" " + N);

    return sb.toString();
  }

  //HELPER 3: Build Chains of Circular Dependencies
  private List<Long> circularPath(Long N, Long M){
    //reset before every check
    circularPath.clear();
    //Keep track of checked elements
    HashSet<Long> checked = new HashSet<>();
    findCircularPath(N, M, checked);
    return circularPath;
  }


  //HELPER 4: Create chain of circular dependencies in circularPath
  private boolean findCircularPath(Long N, Long M, HashSet<Long> checked){
    if(checked.contains(N)){
      return false;
    }
    circularPath.add(N);
    checked.add(N);

    if(N.equals(M) && circularPath.size() > 1) return true;

    if(!dependencies.contains(N)){
      circularPath.remove(N);
      return false;
    }
    //Check all dependencies recursively, each call for a key in the dependencies map
    List<Long> dependents = dependencies.get(N);
    for(Long dependent : dependents){
      if(!checked.contains(dependent)){
        if(findCircularPath(dependent, M, checked)){
          return true;
        }
      }
    }
    circularPath.remove(N);
    return false;
  }
}
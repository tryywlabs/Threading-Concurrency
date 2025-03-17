import java.lang.Thread;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Solution implements CommandRunner {

  /*
   * ATTRIBUTES
   */
  private ConcurrentHashMap<Long, Thread> runningThreads;   //Map of currently running threads
  private ConcurrentHashMap<Long, Integer> finishedThreads; //Map of calculation finished threads, Long and Result
  private List<Long> cancelledThreads; //Map of cancelled threads
  private HashMap<Long, List<Long>> dependencies; //Map of a thread and its dependencies as a list
  private ConcurrentLinkedQueue<Long> waiting; //Queue of waiting commands, input via After

  public boolean getRunning(){
    return runningThreads.isEmpty();
  }

  //METHOD: Solution Class Constructor
  public Solution(){
    runningThreads = new ConcurrentHashMap<>();
    finishedThreads = new ConcurrentHashMap<>();
    cancelledThreads = new ArrayList<>();
    dependencies = new HashMap<>();
    waiting = new ConcurrentLinkedQueue<>();
  }

  //METHOD: runCommand
  public String runCommand(String command){
    if(command == null || command.trim().isEmpty()){
      return "Invalid command";
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

  /* METHODS: COMMAND IMPLEMENTATIONS
   *
   * 1. START
   * 2. CANCEL
   * 3. RUNNING
   * 4. GET
   * 5. AFTER
   * 6. FINISH
   * 7. ABORT
   * 
   */

  //1. COMMAND: START
  public synchronized String start(Long N){
    if (runningThreads.contains(N)){
      return "Already running " + N;
    }
    if (finishedThreads.contains(N)){
      return "Finished running " + N;
    }
    SlowCalculator calc = new SlowCalculator(N);
    Thread thread = new Thread(() -> {
        calc.run();
        if(calc.getResult() == -2){
          cancelledThreads.add(N);
        }
        else{
          finishedThreads.put(N, calc.getResult());
        }
        if(waiting.contains(N)){
          waiting.remove(N);
        }
        runningThreads.remove(N);
        startDependentThreads(N);
    }, String.valueOf(N));

    runningThreads.put(N, thread);
    thread.start();
    return "started " + N;
  }

  //2. COMMAND: CANCEL
  public String cancel(Long N){
    Thread thread;

    synchronized (this) {

        // filter out finished calculations
        if (finishedThreads.contains(N) || cancelledThreads.contains(N)) {
            return "";
        }

        // filter out non-running calculations
        if (!runningThreads.containsKey(N)) {
          //cancel all waiting procedures
            for (List<Long> dependents : dependencies.values()) {
                if (dependents.contains(N)) {
                    dependents.remove(N);
                    waiting.remove(N);
                }
            }

            //Check and start all threads scheduled after N
            if (dependencies.containsKey(N)) {
                List<Long> dependents = dependencies.remove(N);
                for(Long dependent : dependents){
                  start(dependent);
                  waiting.remove(dependent);
                }
                return "cancelled " + N;
            }
            return "cancelled " + N;
        }
        cancelledThreads.add(N);
    }

    thread = runningThreads.get(N);
    thread.interrupt();

    try {
        thread.join();
    } catch (InterruptedException e) {
        e.getStackTrace();
    }

    return "cancelled " + N;
  }

  //3. COMMAND: RUNNING
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

  //4. COMMAND: GET
  public synchronized String get(Long N){
    if(cancelledThreads.contains(N)){
      return "cancelled";
    }

    else if(waiting.contains(N)){
      return "waiting";
    }


    else if(runningThreads.containsKey(N)){
      return "calculating";
    }

    else if (finishedThreads.containsKey(N)){
      return "result is " + finishedThreads.get(N);
    }
    return "No such thread found";
  }

  //5. COMMAND: AFTER
  public String after(Long N, Long M){
    //Check for circular dependency (Refer to Helper Methods)
    if(isCircularDependent(N, M)){
      String circularMessage = "circular dependency ";
      List<Long> circularPath = getCircularPath(N, M);

      for(Long path : circularPath){
        circularMessage += " " + path;
      }
      return circularMessage;
    }
    
    //Check if N is already completed or cancelled, immediately start M
    if(finishedThreads.containsKey(N) || cancelledThreads.contains(N)){
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

  //6. COMMAND: FINISH
  public synchronized String finish(){
    while(!runningThreads.isEmpty()){
      try {
        wait(1);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    return "finished";
  }

  //7. COMMAND: ABORT
  public String abort(){
    synchronized (this) {
      this.dependencies.clear();
  }

  Set<Thread> currentRunningThreads2;
  synchronized (this) {
      Set<Thread> currentRunningThreads = new HashSet<Thread>(this.runningThreads.values());
      currentRunningThreads2 = new HashSet<Thread>(this.runningThreads.values());
      for (Thread thread : currentRunningThreads) {
          if (thread.isAlive()) {
              thread.interrupt();
          }
      }
  }

  for (Thread thread : currentRunningThreads2) {
      try {
          thread.join();
      } catch (InterruptedException e) {
          e.printStackTrace();
      }
  }

  synchronized (this) {
      this.runningThreads.clear();
      this.dependencies.clear();
  }

  return "aborted";
  }


  /* METHODS: HELPER METHODS
   * 1. startDependentThreads
   * 2. isCircularDependent
   * 3. getCircularPath
   * 4. findCircularPath
   */

   //HELPER 1 (Start Command): Begins execution of dependent threads
  private synchronized void startDependentThreads(Long N) {
    if (dependencies.containsKey(N)) {
        List<Long> nextThreads = dependencies.get(N);
        dependencies.remove(N);
        for (Long M : nextThreads) {
            start(M);
            waiting.remove(M);
        }
    }
  }
  
  //HELPER 2 (After Command): Discern Circular Dependency
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

  //HELPER 3 (After Command): Build Path of Circular Dependencies
  private List<Long> getCircularPath(Long N, Long M){
    List<Long> path = new ArrayList<>();
    path.add(N);
    findCircularPathHelper(N, M, path);
    return path;
  }

  //HELPER 4 (After Command): Recursively search for circular dependencies, stop when value is found in key
  private void findCircularPathHelper(Long N, Long M, List<Long> path) {
    if (N == null){
      return;
    }

    if (N.equals(M)) {
        return;
    }
    // find the key of the value N
    Long key = null;
    for (Long k : dependencies.keySet()) {
        if (dependencies.get(k).contains(N)) {
            key = k;
            break;
        }
    }
    if (key != null){
      if (path.contains(key)){
        return;
      }
      path.add(key);
    }
    this.findCircularPathHelper(key, M, path);
  }
}
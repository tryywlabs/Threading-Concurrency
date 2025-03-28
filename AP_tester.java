public class AP_tester {
	public static void main(String[] args) {
		Solution solution = new Solution();
		// instructionTester(solution);
		// System.out.println(abortSpeedTester(solution));
		// successiveAfterTester(solution);
		// AfterNoRunnableThread(solution);
		// noCalculationTester(solution);
		// circularDependencyTester(solution);
		// cancelNonExistant(solution);
		// System.out.println(cancelSpeedTester(solution));
		// competingAfterTester(solution);
		// CancelThreadWhileWaitingTester(solution);
		// CancelMiddleOfMultipleWaitingThreadsTester(solution);
		// multipleGetStatusTester(solution);
		// finishCalculationTester(solution);
		// nothingToFinishTester(solution);
		wrongCommandsTester(solution);

		//---------//
		// testCancel(solution);
		// afterTesterRedo(solution);
		// finishTesterPlease(solution);
	}
	
	public static void instructionTester(Solution solution) {
		System.out.println(solution.runCommand("start 10456060"));
		System.out.println(solution.runCommand("running"));
		sleep();
		System.out.println(solution.runCommand("get 10456060"));
		System.out.println(solution.runCommand("start 72345680"));
		System.out.println(solution.runCommand("start 5432"));
		System.out.println(solution.runCommand("get 5432"));
		System.out.println(solution.runCommand("running"));
		System.out.println(solution.runCommand("cancel 72345680"));
		System.out.println(solution.runCommand("running"));
		System.out.println(solution.runCommand("finish"));
		System.out.println(solution.runCommand("get 5432"));
	}
	
	public static void cancelNonExistant(Solution solution) {
		System.out.println(solution.runCommand("cancel 10456060"));
	}
	
	public static void noCalculationTester(Solution solution) {
		System.out.println(solution.runCommand("running"));
	}
	
	
	public static String cancelSpeedTester(Solution solution) {
		System.out.println(solution.runCommand("start 1723456800"));
		System.out.println("should abort less than 100ms");
	    long startTime = System.currentTimeMillis();
		System.out.println(solution.runCommand("cancel 1723456800"));
		System.out.println(solution.runCommand("abort"));
	    long endTime = System.currentTimeMillis();
	    long duration = endTime - startTime;
	    return ("Method execution time: " + duration + " ms");
	}
	
	public static String abortSpeedTester(Solution solution) {
		System.out.println(solution.runCommand("start 1723456800"));
		System.out.println(solution.runCommand("start 2723456800"));
		System.out.println(solution.runCommand("start 3723456800"));
		System.out.println(solution.runCommand("start 4723456800"));
		System.out.println(solution.runCommand("start 5723456800"));
		System.out.println(solution.runCommand("start 6723456800"));
		System.out.println(solution.runCommand("start 7723456800"));
		System.out.println(solution.runCommand("start 8723456800"));
		System.out.println(solution.runCommand("start 9723456800"));
		System.out.println(solution.runCommand("start 10723456800"));
		System.out.println(solution.runCommand("start 11723456800"));
		System.out.println(solution.runCommand("start 12723456800"));
		System.out.println(solution.runCommand("start 13723456800"));
		System.out.println(solution.runCommand("start 14723456800"));
		System.out.println(solution.runCommand("start 15723456800"));
		System.out.println(solution.runCommand("start 16723456800"));
		System.out.println(solution.runCommand("start 17723456800"));
		System.out.println(solution.runCommand("start 18723456800"));
		System.out.println(solution.runCommand("start 19723456800"));
		System.out.println(solution.runCommand("start 21723456800"));
		System.out.println(solution.runCommand("start 22723456800"));
		System.out.println(solution.runCommand("start 23723456800"));
		System.out.println(solution.runCommand("start 24723456800"));
		System.out.println("should abort less than 100ms");
	    long startTime = System.currentTimeMillis();
		System.out.println(solution.runCommand("abort"));
	    long endTime = System.currentTimeMillis();
	    long duration = endTime - startTime;
		System.out.println(solution.getRunning());
	    return ("Method execution time: " + duration + " ms");
	}
	
	
	public static void successiveAfterTester(Solution solution) {
		System.out.println(solution.runCommand("start 1111111111"));
		System.out.println(solution.runCommand("after 1111111111 2"));
		System.out.println(solution.runCommand("after 2 3"));
		System.out.println(solution.runCommand("after 3 4"));
		System.out.println(solution.runCommand("after 4 5"));
		System.out.println(solution.runCommand("after 5 6"));
		System.out.println("only 1 thread should be running");
		System.out.println(solution.runCommand("running"));
		System.out.println(solution.runCommand("abort"));
		System.out.println(solution.runCommand("get 2"));
		System.out.println(solution.runCommand("get 3"));
		System.out.println(solution.runCommand("get 4"));
	}
	

	public static void AfterNoRunnableThread(Solution solution) {
		System.out.println(solution.runCommand("start 1111111111"));
		System.out.println(solution.runCommand("after 1111111111 22222"));
		System.out.println(solution.runCommand("running"));
		System.out.println(solution.runCommand("cancel 1111111111"));
		System.out.println("⇩immediately start calculation for 2");
		System.out.println(solution.runCommand("running"));
		sleep();
		System.out.println(solution.runCommand("get 22222"));
		System.out.println(solution.runCommand("abort"));
	}
	
	public static void circularDependencyTester(Solution solution) {
		System.out.println(solution.runCommand("start 1111111111"));
		System.out.println(solution.runCommand("after 1111111111 2"));
		System.out.println(solution.runCommand("after 2 3"));
		System.out.println(solution.runCommand("after 3 4"));
		System.out.println(solution.runCommand("after 4 5"));
		System.out.println(solution.runCommand("after 5 6"));
		System.out.println("command after 6 2 should create CD");
		System.out.println("⇩Circular Dependency 6 3 4 5 2");
		System.out.println(solution.runCommand("after 6 2"));
		System.out.println(solution.runCommand("abort"));
	}

	
	public static void competingAfterTester(Solution solution) {
		System.out.println(solution.runCommand("start 1111111111"));
		System.out.println(solution.runCommand("after 1111111111 2222222222"));
		System.out.println(solution.runCommand("after 1111111111 3333333333"));
		System.out.println(solution.runCommand("after 1111111111 4444444444"));
		System.out.println("currently only 1 thread (1111111111) should be running");
		System.out.println(solution.runCommand("running"));
		System.out.println(solution.runCommand("cancel 1111111111"));
		System.out.println("currently 3 threads(2*, 3*, 4*) should be running");
		System.out.println(solution.runCommand("running"));
		System.out.println(solution.runCommand("abort"));
	}
	
	public static void CancelThreadWhileWaitingTester(Solution solution) {
		System.out.println(solution.runCommand("start 1111111111"));
		System.out.println(solution.runCommand("after 1111111111 2222222222"));
		System.out.println(solution.runCommand("cancel 2222222222"));
		System.out.println(solution.runCommand("cancel 1111111111"));
		System.out.println("2222222222 should never run, running thread should be 0");
		System.out.println(solution.runCommand("running"));
		System.out.println(solution.runCommand("abort"));
	}
	
	public static void CancelMiddleOfMultipleWaitingThreadsTester(Solution solution) {
		System.out.println(solution.runCommand("start 1111111111"));
		System.out.println(solution.runCommand("after 1111111111 2"));
		System.out.println(solution.runCommand("after 2 33333333333"));
		System.out.println(solution.runCommand("after 33333333333 44444444444"));
		System.out.println(solution.runCommand("cancel 2"));
		System.out.println("3* and 4* should start immediately");
		System.out.println("there should be 2 running, 1*, 3*");
		System.out.println(solution.runCommand("running"));
		System.out.println("4* should be waiting 3* to finish");
		System.out.println(solution.runCommand("get 44444444444"));
		System.out.println(solution.runCommand("abort"));
	}
	
	public static void multipleGetStatusTester(Solution solution) {
		System.out.println(solution.runCommand("start 1111111111"));
		System.out.println(solution.runCommand("after 1111111111 2"));
		System.out.println(solution.runCommand("start 3"));
		System.out.println(solution.runCommand("start 4444444444"));
		System.out.println(solution.runCommand("cancel 4444444444"));
		System.out.println("⇩calculating");
		System.out.println(solution.runCommand("get 1111111111"));
		System.out.println("⇩waiting");
		System.out.println(solution.runCommand("get 2"));
		System.out.println("⇩result is 0");
		System.out.println(solution.runCommand("get 3")); //not working
		System.out.println("⇩cancelled");
		System.out.println(solution.runCommand("get 4444444444"));
		System.out.println(solution.runCommand("abort"));
	}
	
	public static void finishCalculationTester(Solution solution) {
		System.out.println(solution.runCommand("start 11111111"));
		System.out.println(solution.runCommand("after 11111111 22222"));
		System.out.println("takes about 15 seconds to finished");
		System.out.println(solution.runCommand("finish"));
		System.out.println(solution.runCommand("get 11111111"));
		System.out.println(solution.runCommand("get 22222"));
	}
	
	public static void nothingToFinishTester(Solution solution) {
		System.out.println(solution.runCommand("start 11111111"));
		System.out.println(solution.runCommand("start 22222222"));
		System.out.println(solution.runCommand("start 33333333"));
		System.out.println(solution.runCommand("start 44444444"));
		System.out.println(solution.runCommand("start 55555555"));
		System.out.println(solution.runCommand("cancel 11111111"));
		System.out.println(solution.runCommand("cancel 22222222"));
		System.out.println(solution.runCommand("cancel 33333333"));
		System.out.println(solution.runCommand("cancel 44444444"));
		System.out.println(solution.runCommand("cancel 55555555"));
		long start = System.currentTimeMillis();
		System.out.println("should immediately says finished");
		System.out.println(solution.runCommand("finish"));
		long end = System.currentTimeMillis();
		System.out.println("Time taken: " + (end - start) + "ms");
	}
	
	public static void wrongCommandsTester(Solution solution) {
		/*
		 * These cases are excluded from the cases as no pathological inputs for which the behaviour is not defined will not be tested 
		 * 1. After nonExistentThreadNumber 2
		 * 2. Cancel nonExistantThreadNumber
		 * 3. get nonExistantThreadNumber
		 */
		System.out.println("⇩all of them should say Invalid command");
		System.out.println(solution.runCommand("start"));
		System.out.println(solution.runCommand("start 11111111 2222"));
		System.out.println(solution.runCommand("after 11111111 2222 3333"));
		System.out.println(solution.runCommand("after 11111111"));
		System.out.println(solution.runCommand("after"));
		System.out.println(solution.runCommand("cancel 777 1212"));
		System.out.println(solution.runCommand("cancel"));
		System.out.println(solution.runCommand("abort 11111111"));
		System.out.println(solution.runCommand("finish 11111111"));
	}

	public static void sleep() {
		try {
		    Thread.sleep(2000); // Pauses for 1400 milliseconds (1.4 second)
		} catch (InterruptedException e) {
		    Thread.currentThread().interrupt(); // Restore interrupted status
		}
	}

	public static void testCancel(Solution solution){
		System.out.println(solution.runCommand("start 1111111111"));
		System.out.println(solution.runCommand("running"));
		System.out.println(solution.runCommand("start 22222222"));
		System.out.println(solution.runCommand("running"));
		System.out.println(solution.runCommand("start 33333333"));
		System.out.println(solution.runCommand("running"));
		System.out.println(solution.runCommand("cancel 1111111111"));
		System.out.println(solution.runCommand("running"));
		System.out.println(solution.runCommand("cancel 22222222"));
		System.out.println(solution.runCommand("running"));
		System.out.println(solution.runCommand("cancel 33333333"));
		System.out.println(solution.runCommand("running"));
	}

	public static void afterTesterRedo(Solution solution) {
		System.out.println(solution.runCommand("start 111111"));
		System.out.println(solution.runCommand("after 111111 2222222222"));
		System.out.println(solution.runCommand("after 111111 3333333333"));
		System.out.println(solution.runCommand("after 111111 4444444444"));
		System.out.println("currently only 1 thread (111111) should be running");
		System.out.println(solution.runCommand("running"));
		try {
			Thread.sleep(10000); // Pauses for 1400 milliseconds (1.4 second)
	} catch (InterruptedException e) {
			Thread.currentThread().interrupt(); // Restore interrupted status
	}

		System.out.println("currently 3 threads(2*, 3*, 4*) should be running");
		System.out.println(solution.runCommand("running"));
		System.out.println(solution.runCommand("abort"));
	}

	public static void finishTesterPlease(Solution solution) {
		System.out.println(solution.runCommand("start 1111111"));
		System.out.println(solution.runCommand("after 1111111 22222"));
		System.out.println(solution.runCommand("after 1111111 33333"));
		System.out.println(solution.runCommand("after 1111111 44444"));
		System.out.println("currently only 1 thread (1111111) should be running");
		System.out.println(solution.runCommand("running"));
		System.out.println(solution.runCommand("finish"));
		System.out.println(solution.runCommand("get 22222"));
		System.out.println(solution.runCommand("get 33333"));
		System.out.println(solution.runCommand("get 44444"));
	}
}

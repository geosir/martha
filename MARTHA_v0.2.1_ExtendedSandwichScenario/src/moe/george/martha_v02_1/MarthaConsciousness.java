/*===========================================
 * MARTHA Consciousness
 * ------------------------------------------
 * A endlessly looping thread that continuously
 * calls for evaluation and execution methods
 * in the MARTHA engine.
 *===========================================*/

package moe.george.martha_v02_1;

public class MarthaConsciousness implements Runnable {

	// Instantiate some variables

	private String name = "m_conscious";

	/*
	 * With this version of MARTHA, it is possible for MARTHA to enter different
	 * states of consciousness. State 0, or dreaming, is intended to be MARTHA's
	 * idle state in which it can freely think up LONG chains of actions, with
	 * possible utility in the future. State 1 is like dreaming, but with
	 * shorter chains with seeds based in what was recently queried or said.
	 * State 2 is a goal driven search that is just a complete backwards-chain,
	 * intended to be used to find actions that can be used to fulfill a goal.
	 * 
	 * Martha can change freely between these states, depending on recent idle
	 * cycles. There are mechanisms here to control this.
	 * 
	 * This is slated to be implemented in a future Martha Consciousness.
	 */
	// private int state = 2;
	/*
	 * 0 = dream 1 = contextual search 2 = goal driven search
	 */

	// Martha Conscioussness's reference to the main MARTHA Engine.
	private Martha martha;

	// The Martha Consciousness thread.
	private Thread t;
	
	//Boolean to toggle debug output
	public final static int debug = MainProcess.debug;

	// Get the Martha instance from the MainProcess.
	public MarthaConsciousness(Martha m) {
		martha = m;
	}

	// The consciousness loop.
	@Override
	public void run() {
		int counter = 0; // To keep track of cycles
		while (true) {
			counter++; // increment counter

			/*
			 * NOTE: Much of the things relating to state have been commented
			 * out from this implementation of Martha Conciousness, because it's
			 * not the focus here. To see the original text, check out
			 * TheoryOfMind1, or go back in a previous git version.
			 */

			//martha.planForGoals();
			martha.explore();

			/*
			 * If we've accumulated 10 cycles (DEBUG: just 1 cycle for now)
			 * worth of plans, Evaluate them and execute the results.
			 */
			if (counter % 1 == 0) {

				// Evaluate the queued plans
				martha.evaluatePlans();
				// Execute the queued plans.
				martha.execute();
				
				//Cleanup old foci
				martha.cleanup();

				// DEBUG: Dump of USER/MARTHA knowledge.
				if(debug>=2) System.out.println("USER believes:");
				if(debug>=2) martha.interpretFromUser("?(beliefs USER ?WHAT)");
				if(debug>=2) System.out.println("MARTHA believes:");
				if(debug>=2) martha.interpretFromUser("?(beliefs MARTHA ?WHAT)");

				// DEBUG: Pause to allow the operator to inspect debug code.
				// Print dots to show time.
				for (int i = 0; i < 3; i++) {
					// if(debug) System.out.print(".");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(debug>=2) System.out.println();
			}

			// Shift focus to next cycle
			Martha.incrementFocusTicker();
		}
	}

	// Spawn this new thread.
	public void start() {
		// Let the user know it's all starting up.
		if(debug>=1) System.out.println("Starting Martha Consciousness... " + name);
		if (t == null) {
			t = new Thread(this, name);
			t.start();
		}
	}

	public void setState(int s) {
		// state = s; //UNUSED for now.
	}

}

/*===========================================
 * MARTHA Consciousness
 * ------------------------------------------
 * A endlessly looping thread that continuously
 * calls for evaluation and execution methods
 * in the MARTHA engine.
 *===========================================*/

package moe.george.martha_theoryofmind2;

public class MarthaConsciousness implements Runnable {

	// Instantiate some variables
	private String name = "m_conscious";
	//private int state = 2;
	/* 0 = dream
	 * 1 = contextual search
	 * 2 = goal driven search
	 */
	private Martha martha;
	private Thread t;

	// Get the Martha instance from the MainProcess.
	public MarthaConsciousness(Martha m) {
		martha = m;
	}

	// The consciousness loop
	@Override
	public void run() {
		int counter = 0; // To keep track of cycles
		//int idlecounter = 0; //To keep a running total of consecutive idle cycles.
		//int stuckcounter = 0; //To keep a running total of consecutive non-execution cycles.
		while (true) {
			//idlecounter++;
			counter++; // increment counter
			//System.out.print(".");			
			
			/*//Check current situation and set state accordingly.
			//If there are goals that are immediate (non-persistent) goals, then plan directly for them.
			if(!martha.interpret("?(and (desires USER ?DESIRES) (unknownSentence (goalIsPersistent (desires USER ?DESIRES) True)))").isEmpty())
			{
				idlecounter = 0;
				state = 2;
			}
			else
			{
				idlecounter++;
				
				//If MARTHA has been idle for more than 10 cycles, then dream
				if(idlecounter > 10)
				{
					state = 0;
				}
				//Otherwise just do contextual search
				else
				{
					state = 1;
				}
			}
			
			//A state of 4 means to wake up,
			//whether the MainProcess just got user input or internal
			//processes say to wake up.
			//Whether it is planning or it's dreaming, kick it out the loop.
			if(state == 4 || stuckcounter > 3)
			{
				state = 1;
			}
			
			
			
			// Plan for goals
			switch(state)
			{
			case 0:
				System.out.println("DREAM");
				martha.dream();
				break;
			case 1:
				System.out.println("IDLE");
				martha.planGenerally();
				break;
			case 2:
				System.out.println("PLAN");
				martha.planForGoals();
				break;
			default:
				System.out.println("DEFAULT");
				martha.planGenerally();
			}*/
			
			//martha.planForGoals();
			martha.explore();
			

			// If we've accumulated 10 cycles worth of plans,
			// Evaluate them and execute the results.
			if (counter%1==0) {
				
				martha.execute();
				
				//Show what MARTHA knows
				System.out.println("USER knows:");
				martha.interpretFromUser("?(knows USER ?WHAT)");
				System.out.println("USER believes:");
				martha.interpretFromUser("?(beliefs USER ?WHAT)");
				System.out.println("MARTHA knows:");
				martha.interpretFromUser("?(knows MARTHA ?WHAT)");
				//System.out.println("MARTHA surmises:");
				//martha.interpretFromUser("?(surmises MARTHA ?WHAT)");

				
				//int results = martha.execute();
				/*if(results==99)
				{
					stuckcounter++;
				}
				else
				{
					stuckcounter = 0;
				}*/
				
				for(int i=0; i<5; i++)
				{
					System.out.print(".");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				System.out.println();
			}
			
			//Shift focus to next cycle
			Martha.incrementFocusTicker();
		}
	}

	// Spawn this new thread.
	public void start() {
		// Let the user know it's all starting up.
		System.out.println("Starting Martha Consciousness... " + name);
		if (t == null) {
			t = new Thread(this, name);
			t.start();
		}
	}
	
	public void setState(int s)
	{
		//state = s;
	}

}

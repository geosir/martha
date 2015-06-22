/*===========================================
 * MARTHA Consciousness
 * ------------------------------------------
 * A endlessly looping thread that continuously
 * calls for evaluation and execution methods
 * in the MARTHA engine.
 *===========================================*/

package moe.george.martha_multithreaded_fork;

public class MarthaConsciousness implements Runnable {
	
	//Instantiate some variables
	private String name = "m_conscious";
	private Martha martha;
	private Thread t;
	
	//Get the Martha instance from the MainProcess.
	public MarthaConsciousness(Martha m)
	{
		martha = m;
	}
	
	//The consciousness loop
	@Override
	public void run() {
		int counter = 0;	//To keep track of cycles
		while(true)
		{
			counter++;		//increment counter
			//System.out.print(".");
			
			//Plan for goals
			martha.planForGoals();
			
			//If we've accumulated 10 cycles worth of plans,
			//Evaluate them and execute the results.
			if(counter==10)
			{
				//System.out.println();
				martha.evaluatePlans();
				martha.execute();
				counter=0;		//reset the counter.
			}
		}
	}
	
	//Spawn this new thread.
	public void start()
	{
		//Let the user know it's all starting up.
		System.out.println("Starting Martha Consciousness... "+name);
		if (t == null)
		{
		   t = new Thread(this, name);
		   t.start ();
		}
	}
	
}

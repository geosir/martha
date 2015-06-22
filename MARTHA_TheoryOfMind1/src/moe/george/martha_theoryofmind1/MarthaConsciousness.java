package moe.george.martha_theoryofmind1;

public class MarthaConsciousness implements Runnable {
	
	private String name = "m_conscious";
	private Martha martha;
	private Thread t;
	
	public MarthaConsciousness(Martha m)
	{
		martha = m;
	}
	
	@Override
	public void run() {
		int counter = 0;
		while(true)
		{
			counter++;
			//System.out.print(".");
			martha.planForGoals();
			
			if(counter==10)
			{
				//System.out.println();
				martha.evaluatePlans();
				martha.execute();
				counter=0;
			}
		}
	}
	
	public void start()
	{
		System.out.println("Starting Martha Consciousness... "+name);
		if (t == null)
		{
		   t = new Thread(this, name);
		   t.start ();
		}
	}
	
}

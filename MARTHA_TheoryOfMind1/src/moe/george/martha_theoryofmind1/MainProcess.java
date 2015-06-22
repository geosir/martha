/* ===================================================
 * MARTHA - Multithreaded Fork of Weather Scenario
 * (but also with combined facts from Basic Scenario)
 * Mental-state Aware Real-time THinking Assistant
 * ------------------------------------------------
 * This fork implements a new concept of continuous planning.
 * MARTHA_consciousness loops indefinitely and all the time,
 * running the findRouteToGoals, evaluateGoals, and execute
 * methods non-stop. Also, rather than backwards searching
 * from a goal, MARTHA searches forward from the current situation,
 * starting with a randomly chosen known fact.
 * 
 * This enables MARTHA to plan in a forwards direction like
 * people normally do. Each step of the plan (actions and conditions)
 * are assigned a certain value (stateValue), which is used to evaluate
 * the plan. If the plan is the most desired in an evaluation set,
 * it is executed.
 * ------------------------------------------------ 
 * MARTHA is coded with facts from the weather scenario,
 * so she can be asked about those. 
 * MARTHA also knows the facts from the basic sandwich
 * sceario, so she can be asked about those too.
 * ------------------------------------------------
 * NOTE: Comments added 6/22/15. Last modifications to code
 * were late week of 6/15.
 * ===================================================*/


package moe.george.martha_theoryofmind1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.cyc.base.CycConnectionException;
import com.cyc.kb.exception.KBApiException;
import com.cyc.session.SessionCommunicationException;
import com.cyc.session.SessionConfigurationException;
import com.cyc.session.SessionInitializationException;


public class MainProcess {
	
	// Declare constants used throughout the program
	final static String init_file_path = "initfile.martha"; // The path at which
	// the init file
	// containing MARTHA
	// knowledge can be
	// found.
	final static String context = "BaseKB"; // The context in which all
	// assertions are made.
	
	// Declare variables used throughout
	public static Martha martha; // The MARTHA object, through which all operations
	// through the MARTHA engine are performed.
	
	public static void main(String[] args) throws SessionConfigurationException, SessionCommunicationException, SessionInitializationException, CycConnectionException, KBApiException {

		// Let the user know that we're starting up
		System.out.println("Initializing MARTHA...");
		 martha = new Martha(context); // Create a new instance of the MARTHA
			// engine, specifying the assert
			// context.
		
		// Initialize the Cyc database from the contents of the init file by
			// feeding it through the MARTHA interpreter.
		 try {
			martha.initFromFile(init_file_path); // This is done through a method in the MARTHA object
		} catch (IOException e) {
			// Catch problems initializing from the file; no big deal, don't
						// need to crash the program.
			e.printStackTrace();
			System.out.println("Error initializing from file.");
		}
		
		// Print friendly cool MARTHA banner
		System.out.println("\n\n===============================================");
		System.out.println("Welcome to MARTHA");
		System.out.println("Mental-state Aware Real-time THinking Assistant");
		System.out.println("Copyright (c) George Moe 2015");
		System.out.println("===============================================\n\n");
		
		//Start the MARTHA Consciousness. We create a new instance of it here,
		//and begin a new *thread* to have it loop in.
		MarthaConsciousness mc = new MarthaConsciousness(martha);
		System.out.println("Starting new MARTHA Consciousness...");
		mc.start();
		
		//Prompt loop; this is what presents and handles the "MARTHA: " prompt.
		//Rather than having to evaluate and execute commands here, 
		//Input here is asserted into the KB to be evaluated on the
		//next cycle of the consciousness.
		while(true)
		{
			String input = getInput("MARTHA: "); // Get input from user with the
												// "MARTHA: " prompt.
			martha.interpretFromUser(input); 	// Interpret user input
		}
	    
	}
	
	// Since console input from the traditional System.console.readline()
	// doesn't work in Eclipse,
	// I'm using this workaround instead.	
	//For details on operation, look at the earlier MARTHA MainProcess.java code.
	private static String getInput(String prompt)
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(prompt);
        String input = "";
		try {
			input = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return input;
	}
}

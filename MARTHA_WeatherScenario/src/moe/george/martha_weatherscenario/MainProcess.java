/* ===================================================
 * MARTHA - Weather Scenario
 * Mental-state Aware Real-time THinking Assistant
 * ------------------------------------------------
 * Here is the Weather Scenario:
 * The user would like to go to this Chicago Botanic Gardens,
 * but he does not know where it is. Also, he is aware that
 * it is sunny there, so he assumes it will be warm, but MARTHA
 * knows that it is cold. MARTHA, here, should first tell him
 * where the gardens are, and then, in way of fulfilling the
 * ulterior desire to be happy, alert him that despite it being
 * sunny, it is cold.
 * ------------------------------------------------
 * NOTE: Comments added 6/22/15. Last modifications to code
 * were mid week of 6/15.
 * ===================================================*/

package moe.george.martha_weatherscenario;

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
	static Martha martha;	// The MARTHA object, through which all operations
							// through the MARTHA engine are performed.
	
	//Object to store history, not implemented in this version.
	//ArrayList<Object> history = new ArrayList<Object>();
	
	public static void main(String[] args) throws SessionConfigurationException, SessionCommunicationException, SessionInitializationException, CycConnectionException, KBApiException {

		// Let the user know that we're starting up
		System.out.println("Initializing MARTHA...");
		 martha = new Martha(context); // Create a new instance of the MARTHA
										// engine, specifying the assert
										// context.
		
		// Initialize the Cyc database from the contents of the init file by
		// feeding it through the MARTHA interpreter.
		try {
			// This is done through a method in the MARTHA object
			martha.initFromFile(init_file_path);
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
		
		// Prompt loop; this is what presents and handles the "MARTHA: " prompt.
		while(true)
		{
			String input = getInput("MARTHA: "); // Get input from user with the
												// "MARTHA: " prompt.
			martha.interpretFromUser(input); // Interpret user input
			martha.planForGoals(); // Act on user input by find paths to the
									// specified goal, and executing them.
		}
	    
	}
	
	// Since console input from the traditional System.console.readline()
	// doesn't work in Eclipse,
	// I'm using this workaround instead.	
	private static String getInput(String prompt)
	{
		// Create a new buffer reader attached to the System input buffer
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
		// Print the prompt to let the user know to give input
		System.out.print(prompt);
        String input = "";
		
        // Read a line from the buffer, which is delimited when the user hits
 		// ENTER.
 		// Works just like System.console.readline().
        try {
			input = br.readLine();
		} catch (IOException e) {
			// Catch I/O errors without dying.
			e.printStackTrace();
		}
		
        // Return fetched input.
		return input;
	}
}

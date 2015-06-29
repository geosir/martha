/* ===================================================
 * MARTHA - Theory of Mind Attempt 1
 * Mental-state Aware Real-time THinking Assistant
 * ------------------------------------------------
 * This fork attempts to implement a theory of mind
 * on top of continuous consciousness.
 * ------------------------------------------------
 * NOTE: Comments added while working.
 * ===================================================*/

package moe.george.martha_theoryofmind2;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cyc.base.CycConnectionException;
import com.cyc.kb.client.ContextImpl;
import com.cyc.kb.exception.KBApiException;
import com.cyc.session.SessionCommunicationException;
import com.cyc.session.SessionConfigurationException;
import com.cyc.session.SessionInitializationException;

public class MainProcess {

	// Declare constants used throughout the program
	
	 // The path at which the init folder containing MARTHA knowledge can be found.
	public final static String init_folder = "hungry";
	public final static String history_file = "history.martha";
	public static String context = "RealTimeMt"; // The context in which all assertions are made.
	
	private static Boolean new_context_each_time = true;	//Create a new context each time so that everything asserted is flushed.

	// Declare variables used throughout
	public static Martha martha; // The MARTHA object, through which all operations

	// through the MARTHA engine are performed.

	public static void main(String[] args)
			throws SessionConfigurationException,
			SessionCommunicationException, SessionInitializationException,
			CycConnectionException, KBApiException {

		if(new_context_each_time)
		{
			SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmss");
			Date dt = new Date();
			String timestamp = sdf.format(dt); // formats to 09/23/2009 13:53:28.238
			context = timestamp+"Mt";
			System.out.println(context);
		}
		
		// Let the user know that we're starting up
		System.out.println("Initializing MARTHA...");
		martha = new Martha(context); // Create a new instance of the MARTHA engine, specifying the assert context.

		//Create MARTHA subcontext
		String subcontext = ContextImpl.findOrCreate("MARTHA_"+context).toString();
		martha.interpret(">(genlMt "+subcontext+" "+context+")");
		
		// Initialize the Cyc database from the contents of the init folder by
		// feeding them line by line through the MARTHA interpreter.
		Pattern p = Pattern.compile("\\.martha$");
		for(File f : (new File(init_folder)).listFiles())
		{
			//Make sure files have the ".martha" extension EXACTLY AT THE END
			Matcher m = p.matcher(f.toString());
			
			//If they do, load them.
			if(m.find())
			{
				System.out.print("Loading "+f.toString()+"... ");
				try {
					martha.initFromFile(f.toString()); // This is done through a
															// method in the MARTHA
															// object
					System.out.println("Done.");
				} catch (IOException e) {
					// Catch problems initializing from the file; no big deal, don't
					// need to crash the program.
					e.printStackTrace();
					System.out.println("Error initializing from file "+f+".");
				}
			}
		}

		//Move MARTHA to subcontext.
		martha.changeContext(subcontext);
		
		// Print friendly cool MARTHA banner
		System.out.println("\n\n===============================================");
		System.out.println("Welcome to MARTHA");
		System.out.println("Mental-state Aware Real-time THinking Assistant");
		System.out.println("Copyright (c) George Moe 2015");
		System.out.println("===============================================\n\n");

		martha.start();

		// Prompt loop; this is what presents and handles the "MARTHA: " prompt.
		// Rather than having to evaluate and execute commands here,
		// Input here is asserted into the KB to be evaluated on the
		// next cycle of the consciousness.
		while (true) {
			String input = getInput("MARTHA: "); // Get input from user with the
													// "MARTHA: " prompt.
			martha.interpretFromUser(input); // Interpret user input
		}

	}

	// Since console input from the traditional System.console.readline()
	// doesn't work in Eclipse,
	// I'm using this workaround instead.
	// For details on operation, look at the earlier MARTHA MainProcess.java
	// code.
	private static String getInput(String prompt) {
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

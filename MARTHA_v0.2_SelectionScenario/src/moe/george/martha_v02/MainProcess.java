/* ===================================================
 * MARTHA - Unified Assistive AI Attempt v0.1
 * Mental-state Aware Real-time THinking Assistant
 * ------------------------------------------------
 * This fork attempts to implement a theory of mind
 * on top of continuous consciousness.
 * ------------------------------------------------
 * NOTE: Comments added while working.
 * ===================================================*/
package moe.george.martha_v02;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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

	// ============ Declare constants used throughout the program ============

	/*
	 * The path at which the init folder containing MARTHA knowledge can be
	 * found.
	 */
	public final static String init_folder = "extended";
	public final static String history_file = "history.martha";

	// The context in which all assertions are made.
	public static String context = "RealTimeMt";

	/*
	 * Create a new context each time so that everything asserted before is
	 * flushed.
	 */
	private static Boolean new_context_each_time = true;
	
	//Boolean to toggle debug output
	public static int debug = 2;

	// ============ Declare variables used throughout ============
	// The MARTHA Engine, through which all operations are performed.
	public static Martha martha;

	// Main Process
	public static void main(String[] args)
			throws SessionConfigurationException,
			SessionCommunicationException, SessionInitializationException,
			CycConnectionException, KBApiException {

		/*
		 * Generate a new context name based on the exact time, down to the
		 * second. The purpose of this is so that I don't have to keep
		 * restarting Cyc whenever I make database changes, which is very slow.
		 */
		if (new_context_each_time) {
			SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmss");
			Date dt = new Date();

			// Formats to a string of numbers based on the date and time.
			String timestamp = sdf.format(dt);

			context = timestamp + "Mt";
			if(debug>=2) System.out.println(context);
		}

		// Let the user know that we're starting up
		if(debug>=1) System.out.println("Initializing MARTHA...");
		martha = new Martha(context); // Create a new instance of the MARTHA
										// engine, specifying the assert
										// context.

		// Create MARTHA subcontext
		String subcontext = ContextImpl.findOrCreate("MARTHA_" + context)
				.toString();
		martha.interpret(">(genlMt " + subcontext + " " + context + ")");

		// Initialize the Cyc database from the contents of the init folder by
		// feeding them line by line through the MARTHA interpreter.
		Pattern p = Pattern.compile("\\.martha$");
		File[] files = (new File(init_folder)).listFiles();
		Arrays.sort(files);
		for (File f : files) {
			// Make sure files have the ".martha" extension EXACTLY AT THE END
			Matcher m = p.matcher(f.toString());

			// If they do, load them.
			if (m.find()) {
				if(debug>=1) System.out.print("Loading " + f.toString() + "... ");
				try {
					// Each file is loaded through the Martha Engine by name.
					martha.initFromFile(f.toString());
					if(debug>=1) System.out.println("Done.");
				} catch (IOException e) {
					/*
					 * Catch problems initializing from the file; no big deal,
					 * don't need to crash the program.
					 */
					e.printStackTrace();
					System.out.println("Error initializing from file " + f
							+ ".");
				}
			}
		}

		// Move MARTHA to the MARTHA_ subcontext.
		martha.changeContext(subcontext);

		// Update the internal Java list of actions available to Martha.
		martha.updateActionSet();

		// Print friendly cool MARTHA banner
		System.out
				.println("\n\n===============================================");
		System.out.println("Welcome to MARTHA");
		System.out.println("Mental-state Aware Real-time THinking Assistant");
		System.out.println("Copyright (c) George Moe 2015");
		System.out
				.println("===============================================\n\n");

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

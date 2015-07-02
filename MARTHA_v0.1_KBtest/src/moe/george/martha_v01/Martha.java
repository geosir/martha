/*==================================================
 * MARTHA - Mental-state Aware Real-time THinking Assistant
 * The MARTHA Engine v0.0.3
 * -------------------------------------------------
 * This class is the main interface with MARTHA.
 * All useful methods can be found here, including
 * interpreter and planning methods.
 * This class is all-inclusive; the main method should
 * ONLY be accessing MARTHA through this class, as there
 * is no need to access any of the other modules directly. 
 * 
 * The MARTHA engine is responsible for handling and 
 * interpreting input, spawning search processes, as well 
 * as producing output from the results.
 *==================================================*/

package moe.george.martha_v01;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cyc.kb.Assertion;
import com.cyc.kb.Variable;
import com.cyc.kb.client.AssertionImpl;
import com.cyc.kb.client.ContextImpl;
import com.cyc.kb.client.FactImpl;
import com.cyc.kb.client.KBCollectionImpl;
import com.cyc.kb.client.KBIndividualImpl;
import com.cyc.kb.client.KBPredicateImpl;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KBTypeException;
import com.cyc.query.KBInferenceResultSet;
import com.cyc.query.Query;
import com.cyc.session.CycSessionManager;
import com.cyc.session.SessionCommunicationException;
import com.cyc.session.SessionConfigurationException;
import com.cyc.session.SessionInitializationException;

public class Martha {

	// ================= DECLARE VARIABLES ================= //
	// (MARTHA internal variables and their default values.) //

	/*
	 * The path at which the init file containing MARTHA knowledge can be found.
	 */
	private String initpath = "initfile.martha";

	// The context in which to make assertions to Cyc
	private String assrtctx = "BaseKB";

	// The default context (to revert to after the context changes)
	private String defaultctx = assrtctx;

	/*
	 * Parameters for the query interface. MAX-TRANDFORMATION-DEPTH 10 specifies
	 * that the inference engine should take 10 recursive steps when looking for
	 * query answers. By default, this is zero, which lead to it giving only
	 * one-step inferences!
	 */
	private String queryparams = ":MAX-TRANSFORMATION-DEPTH 1000";

	/*
	 * A comprehensive log of all calls to the interpret method, giving MARTHA a
	 * sense of memory
	 */
	private ArrayList<String[]> logbook = new ArrayList<String[]>();

	/*
	 * Queues to place actions that are TO BE executed, and plans to be
	 * evaluated. Allows for potential asynchronous planning development.
	 */
	private Queue<String> execution_queue = new LinkedList<String>();
	private Queue<LinkedHashSet<String>> evaluation_queue = new LinkedList<LinkedHashSet<String>>();

	// Minimum score needed for a plan to be even considered for execution.
	private int legitimacy_threshold = 35;

	// An object to run MARTHA's consciousness
	MarthaConsciousness mc;

	/*
	 * The starting depth of Martha's nested planning simulations. The count
	 * starts here, in the Martha Engine, and decrements to zero
	 */
	protected int depth = 3;

	/*
	 * A ticker tag that's incremented regularly to separate (focus ?X)
	 * statements from each other, so that MARTHA knows what the actual focus
	 * is. This has potential to be developed into a timer mechanism in the
	 * future.
	 */
	protected static int focus_ticker = 0;

	// A list of executable actions available to MARTHA
	protected static ArrayList<String> action_set;

	private int max_focus_shifts = 100;
	private int initial_focus;

	// Constructor for the MARTHA class
	public Martha(String context) throws SessionConfigurationException,
			SessionCommunicationException, SessionInitializationException,
			CreateException, KBTypeException {

		// Let everyone know that a new MARTHA is being instantiated
		System.out.println("Creating new MARTHA Engine...");

		// Connect to the Cyc server
		System.out.println("Acquiring a cyc server... "
				+ CycSessionManager.getCurrentSession().getServerInfo()
						.getCycServer());

		// Log into the Cyc server as "TheUser"
		System.out.print("Setting cyc user... ");
		CycSessionManager.getCurrentSession().getOptions()
				.setCyclistName("TheUser");
		System.out.println(CycSessionManager.getCurrentSession().getOptions()
				.getCyclistName());

		// Set the assertion context to the one specified by the constructor
		// call
		// Create this context in the Cyc KB.
		assrtctx = context;
		defaultctx = assrtctx; // Store the default context;
		ContextImpl.findOrCreate(context);

		// user = new Martha("userctx_"+context);
	}

	// Method to load pre-coded knowledge into MARTHA from file.
	public void initFromFile(String init_file_path) throws IOException {
		int linenumber = 0; // Counter for the line number.
		initpath = init_file_path; // Set file path to one specified
		Charset charset = Charset.forName("US-ASCII"); // Specify the charset
		// which the file uses
		BufferedReader reader = Files.newBufferedReader(Paths.get(initpath),
				charset); // Make a reader for the file

		// Feed the contents of the file line by line into the interpreter.
		String line = null;
		while ((line = reader.readLine()) != null) {
			linenumber++;

			if (interpret(line).contains("ERROR")) // Interpret the line AND
													// catch error, at the same
													// time!
			{
				System.out.println("initFromFile: Error at line " + linenumber
						+ ".");
			}

			System.out.println(line);
		}

		// Close the reader to free up resources.
		reader.close();
	}

	// Init from file with default path if no path is given.
	public void initFromFile() throws IOException {
		initFromFile(initpath);
	}

	// Update MARTHA's awareness of MARTHA Actions
	public void updateActionSet() {
		action_set = interpret("?(isa ?SOMETHING MarthaAction)");
	}

	/*********************
	 * THIS IS COOL! A Cyc interpreter I wrote so that *input from file* could
	 * be used, for purposes such as knowledge initialization. It interprets the
	 * line and makes the necessary interface with the Cyc API. I'm not aware of
	 * any Cyc file interpreter at the present, so this works well.
	 **********************/
	public ArrayList<String> interpret(String line) {
		return interpret(line, assrtctx);
	}

	public ArrayList<String> interpret(String line, String context) {

		// ArrayList to store output, which only comes from the query (for now).
		ArrayList<String> results = new ArrayList<String>();
		// Boolean indicating whether the entry should be logged or ignored.
		Boolean ignore = false;

		// Wrapped in try-catch in order to not die if there is bad input.
		try {

			// Interpret what to do with the input using the operator (FIRST
			// character in line).

			if (line.length() > 1) {
				switch (line.substring(0, 1)) {
				// > : assertion (variables allowed)
				case ">":
					String ops = line.substring(1);
					AssertionImpl.findOrCreate(ops, context);
					break;
				// = : fact (variables not allowed)
				case "=":
					ops = line.substring(1);
					FactImpl.findOrCreate(ops, context);
					break;
				// X : unassert assertion
				case "X":
					ops = line.substring(1);
					Assertion deleteassert = AssertionImpl.findOrCreate(ops,
							context);
					deleteassert.delete();
					System.out.println("DELETED");
					break;
				// + : create new constant (individual)
				case "+":
					ops = line.substring(1);
					KBIndividualImpl.findOrCreate(ops);
					break;
				// & : create new constant (collection)
				case "&":
					ops = line.substring(1);
					KBCollectionImpl.findOrCreate(ops);
					break;
				// * : create new predicate
				case "*":
					ops = line.substring(1);
					KBPredicateImpl.findOrCreate(ops);
					break;
				// ? : Perform query and return results
				case "?":
					ops = line.substring(1);

					// Create query in the context InferencePSC (the broadest
					// query context) with specified query parameters.
					Query q = new Query(ops, context, queryparams);

					// Get variables used in this query.
					Collection<Variable> queryVars = q.getQueryVariables();

					// Get query results
					KBInferenceResultSet queryResults = q.getResultSet();

					// Check if there are any query results.
					// If the list isn't empty, fetch results for the variables.
					if (!(queryResults.getCurrentRowCount() == 0)) {

						// For each entry in query results, and for each
						// variable
						// Get the result and put in the results ArrayList.
						while (queryResults.next()) {
							for (Variable v : queryVars) {
								String result = queryResults.getKBObject(v)
										.toString();
								results.add(result);
								// System.out.println(result);
							}
						}
					}

					// Close Cyc interface to free up resources.
					queryResults.close();
					q.close();
					break;
				// | : Query the truth of a proposition (returns true/false)
				case "|":
					String question = line.substring(1);

					// Formulate query
					q = new Query(question, context, queryparams);

					// Return truth of query
					results.add(String.valueOf(q.isTrue()));

					// Free up resources.
					q.close();
					break;
				// # : comment
				case "#":
					// It's a comment, do nothing.
					ignore = true; // Do not log into the history log
					break;
				// @ : Set new assertion and query context
				case "@":
					// "@@" for default context.
					if (line.substring(1, 2).equals("@")) {
						changeContext(defaultctx);
					} else {
						if (line.contains("?")) {
							changeContext(line.substring(1).replace("?",
									defaultctx));
						} else {
							changeContext(line.substring(1));
						}
					}
					break;
				// Give error and do nothing if there is a bad operator.
				default:
					System.out.println("Not understood: " + line);
					ignore = true; // Do not log into the history log
				}

				// UNLESS we are NOT to log it into the history...
				if (!ignore) {
					String[] new_entry = { line.substring(1),
							line.substring(0, 1) };
					logbook.add(new_entry); // Add the interpret prompt into the
											// history log.
				}
			}
		} catch (Exception e) { // Catch error and give debug info, but don't
								// die.

			// e.printStackTrace();
			System.out
					.println("Warning: Could not interpret \"" + line + "\".");
			results.add("ERROR");
			return (results);
		}

		// Return ArrayList with results.
		return (results);
	}

	// A method for external entities to increment the focus ticker.
	public static void incrementFocusTicker() {
		focus_ticker++;
	}

	/*
	 * The main function should use this method when collecting input from the
	 * user. It is a wrapper for the interpret() function which also asserts to
	 * the Cyc KB that the user should know the facts that he asserts himself.
	 */
	public ArrayList<String> interpretFromUser(String input) {

		// ArrayList to store results from interpet()
		ArrayList<String> results = new ArrayList<String>();

		// Check that the input is of a valid length, otherwise do nothing.
		if (input.length() > 1) {

			// Interpret the input.
			results = interpret(input);

			/*
			 * If the input was an assertion, assert that the user knows about
			 * the contents of the assertion. Also assert that the user said it.
			 * Then wonder why the user said it ("so, what?").
			 */
			if (input.substring(0, 1).equals(">")
					|| input.substring(0, 1).equals("=")) {
				interpret(">(beliefs USER " + input.substring(1) + ")");
				interpret(">(says USER " + input.substring(1) + ")");

				/*
				 * Explore possibilities for *why* the user said what he said.
				 * TODO: can be extended for queries too, so that Martha can
				 * discern goals from questions.
				 */
				interpret(">(focus " + (focus_ticker + 1)
						+ " (sowhat (says USER " + input.substring(1) + ")))");
				for(int i=1; i<4; i++)
				{
					interpret(">(focus " + (focus_ticker + i) + " (sowhat "+ input.substring(1) + "))");
				}
						
			}

			// ^ : special MARTHA escape command
			else if (input.substring(0, 1).equals("^")) {
				String command = input.substring(1);
				switch (command) {

				// toomuch : Use if Martha is giving too much information.
				case "toomuch":

					// If there is too much info, the threshold is too low.
					// Increase the legitimacy threshold.
					legitimacy_threshold += 5;
					System.out
							.println("New threshold: " + legitimacy_threshold);
					break;

				// toolittle : Use if Martha is giving too little information.
				case "toolittle":

					// If there is too little info, the threshold is too high.
					// Decrease the legitimacy threshold.
					legitimacy_threshold -= 5;
					System.out
							.println("New threshold: " + legitimacy_threshold);
					break;
				default:
				}
			}
		}
		// Otherwise, do nothing. No meaningful input.

		/*
		 * Print results so that the user can see them. (interpret no longer has
		 * native output.)
		 */
		for (String r : results) {
			System.out.println(r);
		}

		// Return interpret results.
		return (results);
	}

	/*****************
	 * THIS PART IS KEY Here are the core planning methods for MARTHA They are
	 * initiated by planGenerally, which fetches the goals from the user's
	 * desires and makes them a target of MARTHA's backwards search.
	 *****************/
	// Do a bunch of short forward searches based on content from recent
	// interactions with interpret.

	// This is an abstraction of a query that returns the importance of a goal,
	// as specified in the Cyc KB.
	public int getGoalImportance(String goal) {
		ArrayList<String> importance = interpret("?(goalImportance USER "
				+ goal + " ?IMPORTANCE)"); // Get importance values

		// Parse the importance value string into a quantity.
		int level = 1;
		if (!importance.isEmpty()) {
			switch (importance.get(0)) {
			case "highLevelOf":
				level = 0;
				break;
			case "mediumLevelOf":
				level = 1;
				break;
			case "lowLevelOf":
				level = 2;
				break;
			default:
				level = 1;
			}
		}

		// Return that quantity.
		return level;
	}

	/*
	 * This is the biggest feature of this particular version of the MARTHA
	 * engine. It is capable of using (focus ?X) statements and exploring
	 * possible the user's intentions and goals with those statements. For
	 * instance, in particular to the MARTHA test scenario, when the user said
	 * "Today is my birthday," MARTHA could find that the user expects MARTHA to
	 * say "Happy birthday!" because it would make him happy; thus MARTHA would
	 * say "Happy birthday!"
	 */

	/*
	 * Explore with no args; this Martha Engine method redirects to the Martha
	 * Process explore, with a few extra operations to set things up and handle
	 * returned information.
	 */
	public void explore() {

		// Using try-catch to silence fatal errors.
		try {
			/*
			 * Create a new Martha Process. Martha Processes are used to run
			 * planning processes. This particular process represents Martha's
			 * actual thinking process, so the contexts are preserved. The depth
			 * is one lower to differentiate between the Martha Process and the
			 * Martha Engine.
			 */
			MarthaProcess martha_p = new MarthaProcess(this, assrtctx,
					defaultctx, depth - 1, "MARTHA");

			// Use the Martha Process to plan
			martha_p.explore();

		} catch (Exception e) {
			// Catch errors and print a stack trace.
			e.printStackTrace();
		}

		/*
		 * The final layer of Martha Process's explore will queue several plan
		 * possibilities to be evaluated by the Martha Engine. We evalute those
		 * plans now.
		 */
		evaluatePlans();
	}

	// Explore possible intentions, seeded with a single string.
	public void explore(String s) {

		// Set the initial focus count, used for focus auditing and loop
		// prevention.
		initial_focus = focus_ticker;

		// A nice debug marker.
		System.out.println("MARTHA ===EXPLORE=== " + depth);

		// Try-catch to silence fatal errors.
		try {
			/*
			 * Create a Martha Process to run the explore process. This process
			 * represents Martha's actual thinking process, so the contexts are
			 * preserved. The depth is one lower to differentiate between the
			 * Martha Process and the Martha Engine.
			 */
			MarthaProcess martha_p = new MarthaProcess(this, assrtctx,
					defaultctx, depth - 1, "MARTHA");

			// Explore the string s within the Martha Process.
			martha_p.explore(s);

		} catch (Exception e) {
			// Catch errors and print a stack trace.
			e.printStackTrace();
		}

		// A nice debug marker.
		System.out.println("MARTHA ===EXPLORE=== " + depth);
	}

	/*
	 * Given a goal, find a path of action that gets to the goal. This is
	 * largely a wrapper for the planForGoals method in Martha Process.
	 */
	public void planForGoals() {

		// A nice debug marker.
		System.out.println("MARTHA ============== " + depth);

		// Try-catch to silence fatal errors.
		try {
			// Spawn a new Martha Process to take care of the planning
			// algorithm.
			MarthaProcess martha_p = new MarthaProcess(this, assrtctx,
					defaultctx, depth - 1, "MARTHA");

			// Run planForGoals in the Martha Process.
			martha_p.planForGoals();
		} catch (Exception e) {
			// Catch errors and prince a stack trace.
			e.printStackTrace();
		}

		// A nice debug marker.
		System.out.println("MARTHA ============== " + depth);
	}

	/***********************
	 * NOTE! THE ACTUAL MARTHA SEARCH FUNCTIONS, WITH THEIR DEPENDENCIES, HAVE
	 * BEEN MOVED TO MARTHA PROCESS. All planning is to be done through a Martha
	 * Process. Execution and evaluation is still needed by the engine. *
	 ***********************/

	// Method to see if a goal is persistent (should not be unasserted
	// automatically)
	public Boolean goalIsPersistent(String goal) {
		return (getTruthOf("(goalIsPersistent " + goal + " True)"));
	}

	// An abstraction for the use of interpret("|"+...), returns the true/false
	// of a statement.
	public Boolean getTruthOf(String statement) {
		return (interpret("|" + statement).get(0).equals("true"));
	}

	// Given a lisp expression, return the "key words," i.e. the function names
	// inside the entire expression. (For instance, "knows" in
	// "(knows USER...").
	public ArrayList<String> getKeyWords(String statement) {

		// ArrayList to store results.
		ArrayList<String> results = new ArrayList<String>();

		// A regular expression to match the key words in the given statement.
		Pattern p = Pattern.compile("\\(([\\-.\\w]+)");
		Matcher m = p.matcher(statement);
		int start = 0;

		// Find the groups captured by the regular expression.
		while (m.find(start)) {

			// Add the results to the group, and set up the search
			// to continue at the end of this one.
			results.add(m.group(1));
			start = m.end();
		}

		// Return found key words.
		return results;
	}

	// Queue actions to be executed by the execute() function.
	// Enables actions to be executed all at once (non-blocking).
	public int queueExecution(String action) {
		execution_queue.add(action);
		return 0;
	}

	// Queue action chains to be evaluated. Useful to compare a large
	// set of possible actions.
	public int queueEvaluation(LinkedHashSet<String> path) {
		evaluation_queue.add(path);
		return 0;
	}

	// Get the next action in the execution queue.
	public String getEnqueuedAction() {
		return (execution_queue.poll());
	}

	/**********************
	 * MARTHA functions are Cyc constants that have special functions in the
	 * MARTHA engine. The one here is say-TheMARTHAFunction, which, when
	 * executed, prints the specified a line of output for the user to see.
	 ***********************/
	public int execute() {

		// Store the state of the operation. 0 is suceessful.
		// 99 means nothing executed.
		int state = 99;

		// Get the first action from the execution queue.
		String action = getEnqueuedAction();

		// While there are still actions in the queue...execute them.
		while (action != null) {

			// Some nice debug output.
			System.out.println("ACTION(" + getUtility(action) + "): " + action);

			// Boolean to store whether or not the action is to be asserted.
			boolean shouldassert = true;

			// A list of key functional words extracted from the action.
			ArrayList<String> keywords = getKeyWords(action);

			// If there are keywords in the action...
			if (!keywords.isEmpty()) {
				// ...identify them by the first keyword.

				// XXX: DEPRECATED. USE (says MARTHA ...) INSTEAD.
				// If the function expression is say-TMF...
				// SYNTAX: (say-TMF <thing to be said>)
				// Where TMF is short for "TheMARTHAFunction"
				if (keywords.get(0).equals("say-TMF")) {

					// Parse the action for this Martha Function using a regular
					// expression.
					Pattern p = Pattern
							.compile("\\(([-.\\w]+)\\s*(\\([\\w\\s-.\\(\\)]+\\))\\)");
					Matcher m = p.matcher(action);

					if (m.matches()) {

						// Say what needs to be said.
						System.out.println();
						System.out.println("=================================");
						System.out.println("MARTHA>>> " + m.group(2));
						System.out.println("=================================");
						System.out.println();

						// Action executed completely.
						state = 0;
					}

				}

				/*
				 * If the function is says MARTHA... (identical to say-TMF, but
				 * can also handle quotations) SYNTAX: (say MARTHA <thing to be
				 * said>)
				 */
				else if (keywords.get(0).equals("says")) {

					// Parse the action for this Martha Function using a regular
					// expression.
					Pattern p = Pattern
							.compile("\\(says MARTHA ([\\(\\)\\w\\s]+)\\)");
					Matcher m = p.matcher(action);

					// If this particular call to says uses quotation marks, use
					// this regex instead.
					if (!m.matches()) {
						// Regular expression to handle quotation marks.
						p = Pattern
								.compile("\\(says MARTHA \\\"([\\(\\)\\w\\s.,!?;:]+)\\\"\\)");
						m = p.matcher(action);
					}

					if (m.matches()) {
						// Say what needs to be said.
						/*
						 * System.out.println();
						 * System.out.println("================================="
						 * ); System.out.println("MARTHA>>> " + m.group(1));
						 * System
						 * .out.println("=================================");
						 * System.out.println();
						 */

						/*
						 * XXX: (says MARTHA ...) and (say-TMF ...) are
						 * conflicting! says is supposed to replace say-TMF
						 * soon, but because of the global scope of says,
						 * "knows" preconditions in the precondition builder are
						 * prematurely fulfilled by it. For now, says MARTHA
						 * will just redirect to say-TMF at execution, while
						 * say-TMF will be used for final MARTHA statements.
						 */
						queueExecution("(say-TMF " + m.group(1) + ")");

						// Action redirected.
						state = 2;
					}
				}
				/*
				 * If the function expression is query-TMF... Ask the user a
				 * question. SYNTAX: (query-TMF <thing to be asked>)
				 */
				else if (keywords.get(0).equals("query-TMF")) {

					// Parse the action for this Martha Function using a regular
					// expression.
					Pattern p = Pattern
							.compile("\\(([-.\\w]+)\\s*(\\([\\w\\s-.\\(\\)]+\\))\\)");
					Matcher m = p.matcher(action);

					if (m.matches()) {
						// Ask the user what needs to be asked.
						System.out.println();
						System.out.println("=================================");
						System.out.println("MARTHA>>> " + m.group(2) + "?");
						System.out.println("=================================");
						System.out.println();

						purgeQueue(execution_queue); // Purge the queue to halt
														// execution and let the
														// user respond to the
														// question.
						state = 1; // Pending user input state.
					}
				}

				/*
				 * If the function expression is contradict-TMG... Alert the
				 * user that something is not true. SYNTAX: (contradict-TMF
				 * <thing to contradict>)
				 */
				else if (keywords.get(0).equals("contradict-TMF")) {

					// Parse the action for this Martha Function using a regular
					// expression.
					Pattern p = Pattern
							.compile("\\(([-.\\w]+)\\s*(\\([\\w\\s-.\\(\\)]+\\))\\)");
					Matcher m = p.matcher(action);

					if (m.matches()) {
						// Ask the user what needs to be asked.
						System.out.println();
						System.out.println("=================================");
						System.out.println("MARTHA>>> Hey! " + m.group(2)
								+ " is not true!");
						System.out.println("=================================");
						System.out.println();

						state = 0;
					}
				} else {
					// No match, don't assert.
					shouldassert = false;
				}

				// DEBUG: Pause so I can read the action.
				if (shouldassert) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			} else {
				// no match, don't assert.
				shouldassert = false;
			}

			/*
			 * Should this statement be asserted; i.e., did this function
			 * actually happen, or is it just a metafunction?
			 */

			if (shouldassert) {
				// Assert that it has been done.
				interpret(">" + action);

				// Assert the execution time of this action as NOW.
				interpret(">(exactAssertTime " + action
						+ " (IndexicalReferentFn Now-Indexical))");
			}

			// Get the next action
			action = getEnqueuedAction();
		}

		// Return the state of the execution.
		return state;
	}

	// Deepclone an LinkedHashSet, rather than just copy a reference.
	// This returns an entirely new ArrayList that is disconnected
	// from the given one.
	protected LinkedHashSet<String> deepClone(LinkedHashSet<String> original) {

		// For each element in the original, make an identical copy in the new
		// independent LinkedHashSet.
		LinkedHashSet<String> a = new LinkedHashSet<String>(original.size());
		for (String o : original) {
			a.add(new String(o));
		}
		return a;
	}

	// Evaluate the desirability of proposed plans in the evaluation queue.
	public void evaluatePlans() {
		try {

			// Get first plan to be evaluated
			LinkedHashSet<String> a = evaluation_queue.poll();

			// Evaluation premise:
			// Current premise is to select series of actions
			// With highest sum value.

			// Instantiate some variables
			LinkedHashSet<String> candidate = a;
			float highest_value = 0;

			while (a != null) {

				// Cleanup a to remove all ROOT value flags.
				Iterator<String> it = a.iterator();
				while (it.hasNext()) {
					String test = it.next();
					if (test.contains("ROOT")) {
						it.remove();
					}
				}

				// Running total of the plan's sum outcome
				float current_value = 0;

				// Add up the value of all actions in the plan
				for (String s : a) {
					current_value = getUtility(s) + current_value;
					System.out.println("ACTION(" + getUtility(s) + "): " + s);
				}

				// If the plan is of equal or higher value of the highest value,
				// Add it to the candidate list.
				if (current_value > highest_value) {
					candidate = a;
					highest_value = current_value;
				} else if (current_value == highest_value) {
					candidate.addAll(a);
				}
				if (current_value != 0 || true) {
					System.out.println("<" + current_value + "> " + a);
				}
				a = evaluation_queue.poll();
			}

			// If the highest value exceeds the minimum score needed,
			// Go ahead an queue for execution.
			if (highest_value >= legitimacy_threshold) {
				System.out.println("APPROVED: " + candidate);
				System.out.println("SCORE: " + highest_value);

				// Queue all actions in the approved plan for execution.
				for (String c : candidate) {
					queueExecution(c);
				}
			}
			// If the legitimacy threshold has not been met...
			else {
				// Some neat debug output.
				System.out.println("THRESHOLD UNMET: " + candidate + " "
						+ highest_value);

				/*
				 * Create a temporary indicator to see if the focus ticker has
				 * incremented. this is used with the for-loop below so that
				 * it's just incremented once; all focus statements produced
				 * here are on the same level.
				 */
				int next_focus = focus_ticker;

				// If there is a candidate at all...
				if (candidate != null) {
					/*
					 * For each item in the candidate, create a focus statement
					 * to explore if it refers to an valid MARTHA action.
					 */
					for (String c : candidate) {
						// If the key word of the action is in the MARTHA action
						// set...
						ArrayList<String> keywords = getKeyWords(c);
						if (action_set.contains(keywords.get(0))
								&& !areWeLost()) {
							// Get the next focus tick
							next_focus = focus_ticker + 1;

							// Assert the focus statement with the next focus
							// tick.
							interpret(">(focus " + next_focus + " (sowhat " + c
									+ "))");
						} else if (keywords.get(0).equals("desires")) {
							interpret(">" + c);
							interpret(">(carryover " + c+")");
						}
					}

					/*
					 * If the focus ticker has changed, this means there are
					 * valid focus statements. Go ahead and explore these.
					 */
					if (focus_ticker != next_focus) {
						focus_ticker = next_focus;
						explore();
					}
				}
			}
		} catch (Exception e) {
			// Catch errors and print a report.
			e.printStackTrace();
			System.out.println("Warning: Evaluation failed.");
		}
	}

	// Clear all the contents of a queue.
	private void purgeQueue(Queue<?> q) {
		q.clear();
	}

	// Get the base utility value of an action or condition, as stated in the
	// Cyc KB.
	public Float getBaseUtility(String state) {
		try {
			// Query the base utility value from the Cyc KB.
			ArrayList<String> utility_value = interpret("?(baseUtilityValue USER "
					+ state + " ?VALUE)");

			// Return the most recent result.
			return (new Float(utility_value.get(utility_value.size() - 1)));

		} catch (Exception e) {
			// If the query makes no sense, or if there's an error, then default
			// to zero.
			return 0f;
		}
	}

	// Get the utility yield of an action or condition, based on a sigmoid
	// function.
	public Float getUtilityYield(String state) {
		try {

			// Get the scheduled time for an action
			ArrayList<String> exactasserttime = interpret("?(exactAssertTime "
					+ state + " ?VALUE)");

			// If the scheduled time exists...
			if (!exactasserttime.isEmpty()
					&& !exactasserttime.contains("ERROR")) {

				// Parse the scheduled time and get the current time
				SimpleDateFormat sdf = new SimpleDateFormat(
						"EEE MMM dd HH:mm:ss z yyyy");
				Date event = sdf.parse(exactasserttime.get(exactasserttime
						.size() - 1));
				Date now = new Date();

				Float yield = 1f;

				// Calculate the time until the event (can be negative)
				long timeuntil = (event.getTime() - now.getTime()) / 1000;

				if (timeuntil > 0) {
					// Sigmoid function if event is in the future (yield
					// increases as event approaches)
					yield = (float) (1 / (1 + Math.pow(2.71828,
							(timeuntil - 45) / 3)));
				} else {
					// Sigmoid function if event is in the past (yield increases
					// as event recedes)
					yield = (float) (1 / (1 + Math.pow(2.71828,
							(timeuntil + 45) / 5)));
				}

				return yield;

			} else {
				return 1f;
			}
		} catch (Exception e) {
			// If the query makes no sense, or if there's an error, then default
			// to 1 (full utility).
			return 1f;
		}
	}

	// Get the total utility value of an action or condition, the product of
	// baseUtiliityValue and utilityYield.
	public Float getUtility(String state) {
		try {
			// Total utility is the product of the base utility and the utility
			// yield.
			return (getBaseUtility(state) * getUtilityYield(state));
		} catch (Exception e) {
			// If the query makes no sense, or if there's an error, then default
			// to zero.
			return 0f;
		}
	}

	// Method to change the context of assertions. To be used in the future
	// for changing timescale consciousness.
	public void changeContext(String context) {
		try {
			ContextImpl.findOrCreate(context);
			assrtctx = context;
		} catch (Exception e) {
			System.out.println("ERROR: Could not change context to " + context
					+ ".");
		}

	}

	// A method to change the default context (the context returned to by @@)
	public void changeDefaultContext(String context) {
		defaultctx = context;
		System.out.println(depth + " " + defaultctx);
	}

	// Start MARTHA's consciousness
	public void start() {
		// Start the MARTHA Consciousness. We create a new instance of it here,
		// and begin a new *thread* to have it loop in.
		mc = new MarthaConsciousness(this);
		System.out.println("Starting new MARTHA Consciousness...");
		mc.start();
	}

	// A way for the Main Process to "wake" Martha Consciousness from a dream
	// state.
	public void wake() {
		mc.setState(4);
	}

	public boolean areWeLost() {
		if ((focus_ticker - initial_focus) > max_focus_shifts) {
			return true;
		}
		return false;
	}
}

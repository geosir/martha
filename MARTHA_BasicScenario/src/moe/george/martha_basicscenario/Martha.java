/*==================================================
 * MARTHA - Mental-state Aware Real-time THinking Assistant
 * Core engine
 * -------------------------------------------------
 * This class is the main interface with MARTHA.
 * All useful methods can be found here, including
 * interpreter and planning methods.
 * In the future, this should be all inclusive; 
 * the main method should ONLY be accessing MARTHA
 * through this class.
 *==================================================*/

package moe.george.martha_basicscenario;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cyc.kb.Assertion;
import com.cyc.kb.Variable;
import com.cyc.kb.client.AssertionImpl;
import com.cyc.kb.client.ContextImpl;
import com.cyc.kb.client.FactImpl;
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

	// MARTHA internal variables and their default values.
	private String initpath = "initfile.martha"; // The path at which
													// the init file
													// containing MARTHA
													// knowledge can be
													// found.
	private String assrtctx = "BaseKB"; // The context in which to make
										// assertions to Cyc

	// Parameters for the query interface.
	// MAX-TRANDFORMATION-DEPTH 10 specifies
	// that the inference engine should take 10 recursive steps when looking for
	// query answers. By default, this is zero, which lead to it giving only
	// one-step inferences!
	private String queryparams = ":MAX-TRANSFORMATION-DEPTH 10";

	// Constructor for the MARTHA class
	public Martha(String context) throws SessionConfigurationException,
			SessionCommunicationException, SessionInitializationException,
			CreateException, KBTypeException {
		// Let everyone know that a new MARTHA is being instantiated
		System.out.println("Creating new MARTHA...");

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
		ContextImpl.findOrCreate(context);
	}

	// Method to load pre-coded knowledge into MARTHA from file.
	public void initFromFile(String init_file_path) throws IOException {
		initpath = init_file_path; // Set file path to one specified
		Charset charset = Charset.forName("US-ASCII"); // Specify the charset
														// which the file uses
		BufferedReader reader = Files.newBufferedReader(Paths.get(initpath),
				charset); // Make a reader for the file

		// Feed the contents of the file line by line into the interpreter.
		String line = null;
		while ((line = reader.readLine()) != null) {
			interpret(line); // Interpret the line
		}

		// Close the reader to free up resources.
		reader.close();
	}

	// Init from file with default path if no path is given.
	public void initFromFile() throws IOException {
		initFromFile(initpath);
	}

	/*********************
	 * THIS IS COOL! A Cyc interpreter I wrote so that *input from file* could
	 * be used, for purposes such as knowledge initialization. It interprets the
	 * line and makes the necessary interface with the Cyc API. I'm not aware of
	 * any Cyc file interpreter at the present, so this works well.
	 **********************/
	public ArrayList<String> interpret(String line) {
		
		//ArrayList to store output, which only comes from the query (for now).
		ArrayList<String> results = new ArrayList<String>();

		//Wrapped in try-catch in order to not die if there is bad input.
		try {
			
			//Make sure there is something meaningful in the line; size > 1
			if (line.length() > 1) {
				
				//Interpret what to do with the input using the operator (FIRST character in line).
				
				// > : assertion (variables allowed)
				if (line.substring(0, 1).equals(">")) {
					String assertion = line.substring(1);
					// System.out.println(assertion);
					AssertionImpl.findOrCreate(assertion, assrtctx);	//Action performed through Cyc API.
				} 
				
				// = : fact (variables not allowed)
				else if (line.substring(0, 1).equals("=")) {
					String assertion = line.substring(1);
					// System.out.println(assertion);
					FactImpl.findOrCreate(assertion, assrtctx); //Action performed through Cyc API.
				} 
				
				// X : unassert assertion
				else if (line.substring(0, 1).equals("X")) {
					String assertion = line.substring(1);
					// System.out.println(assertion);
					Assertion deleteassert = AssertionImpl.findOrCreate(
							assertion, assrtctx);
					deleteassert.delete(); //Action performed through Cyc API. etc. etc.
					System.out.println("DELETED");
				} 
				
				// + : create new constant
				else if (line.substring(0, 1).equals("+")) {
					String addition = line.substring(1);
					// System.out.println(addition);
					KBIndividualImpl.findOrCreate(addition);
				} 
				
				// * : create new predicate
				else if (line.substring(0, 1).equals("*")) {
					String addition = line.substring(1);
					// System.out.println(addition);
					KBPredicateImpl.findOrCreate(addition);
				} 
				
				// ? : Perform query and return results
				else if (line.substring(0, 1).equals("?")) {
					String question = line.substring(1);
					// System.out.println(question);

					//Some old stuff using regex to parse variables from the query string.
					//This is deprecated; It's cleaner to use getQueryVariables() instead.
					// new ArrayList<String>();
					/*
					 * Matcher matcher =
					 * Pattern.compile("(\\?\\w+)").matcher(question); while
					 * (matcher.find()) { String variable = matcher.group(1);
					 * Variable queryVar = new VariableImpl(variable);
					 * queryVars.add(variable); }
					 */

					//Create query in the context InferencePSC (the broadest query context) with specified query parameters.
					Query q = new Query(question, "InferencePSC", queryparams);
					
					//Get variables used in this query.
					Collection<Variable> queryVars = q.getQueryVariables();

					//Check if there are any query variables.
					//If the list isn't empty, fetch results for those queries.
					if (queryVars.isEmpty()) {
						// Silenced.
						// System.out.println("EMPTY");
						// System.out.println(q.isTrue());
					} else {

						//Get query results
						KBInferenceResultSet queryResults = q.getResultSet();
						// System.out.println(q.getInferenceParameters());
						
						//If there are any query results...
						if (!(queryResults.getCurrentRowCount() == 0)) {
							
							//For each entry in query results, and for each variable
							//Get the result and put in the results ArrayList.
							while (queryResults.next()) {
								for (Variable v : queryVars) {
									System.out.print(v + ": ");
									String result = queryResults.getKBObject(v)
											.toString();
									results.add(result);
									System.out.println(result);
								}
							}
						} else {
							
							//Otherwise, let the user know that there were no results.
							System.out.println("NO RESULTS");
							/*
							 * if(q.isProvable()) {
							 * System.out.println("But can be proven?"); } else
							 * { System.out.println("Cannot be proven."); }
							 */
						}
						
						//Close Cyc interface to free up resources.
						queryResults.close();
					}
					
					//Close Cyc interface to free up resources.
					q.close();
				} 
				
				// # : comment
				else if (line.substring(0, 1).equals("#")) {
					// It's a comment, do nothing.
				} 
				
				//Give error and do nothing if there is a bad operator.
				else {
					System.out.println("Not understood: " + line);
				}
				// System.out.println("Done.");
			}
		} catch (Exception e) { //Catch error and give debug info, but don't die.
			e.printStackTrace();
			System.out
					.println("Warning: Could not interpret \"" + line + "\".");
		}

		//Return ArrayList with results.
		return (results);
	}

	//The main function should use this method when collecting input from the user.
	//It is a wrapper for the interpret() function which also asserts to the Cyc KB
	//that the user should know the facts that he asserts himself.
	public ArrayList<String> interpretFromUser(String input) {
		
		//ArrayList to store results from interpet()
		ArrayList<String> results = new ArrayList<String>();
		if (input.length() > 1) {
			results = interpret(input);		//Interpret input
			
			// Do something so that MARTHA asserts that the user knows that the
			// user asserted the assertion asserted here.
			// If the input was an assertion, assert that the user knows about the contents of the assertion.
			if (input.substring(0, 1).equals(">")) {
				interpret(">(knows USER " + input.substring(1) + ")");
			}
		} else {
			// Do nothing, no meaningful input.
		}
		
		//Return interpret results.
		return (results);
	}

	/*****************
	 * THIS PART IS KEY
	 * Here are the core planning methods for MARTHA
	 * They are initiated by planForGoals, which fetches the goals
	 * from the user's desires and makes them a target of MARTHA's backwards search.
	 *****************/	
	public void planForGoals() {
		
		//Query the database to find the user's desires.
		ArrayList<String> goals = interpret("?(desires USER ?DESIRES)");
		
		//For each desire (which is a goal in this case), evaluate the possible paths to the goal.
		for (String g : goals) {
			Boolean fulfilled = evaluateGoal(g);	//Find paths to the goal
			
			//If the goal is fulfilled, unassert the desire from the KB to show that it is past.
			if (fulfilled) {

				interpret("X(desires USER " + g + ")");
			}
		}
	}

	//RECURSIVELY find action chains that lead to goals through backwards search.
	//Note that the goal doesn't need to be a user desire (i.e., primary goal).
	//It can also be target condition that needs to be reached, or an action
	//that needs preconditions
	public Boolean evaluateGoal(String goal) {
		
		//Has the goal been fulfilled?
		Boolean fulfilled = false;

		System.out.println(">>> GOAL        : " + goal);
		
		//For a certain goal, find actions that result in the goal
		//(usually done with preconditions, but nothing stops an action from having these too).
		ArrayList<String> actions = getActionsForPostconditions(goal);
		
		//For each action found above, get the preconditions for the action.
		for (String a : actions) {
			System.out.println(">>> ACTION      : " + a);
			
			//Get preconditions
			ArrayList<String> preconditions = getPreconditionsForAction(a);
			
			//If there are no preconditions, then the action can be executed.
			//If the execution leads to the fulfillment of a goal, then return
			//that the goal has been fulfilled.
			if (preconditions.isEmpty()) {
				fulfilled = execute(a);		//Execute action
				if (fulfilled) {
					break;
				}
			} 
			
			//Otherwise, if there are preconditions, evaluate them as goals to
			//find paths that can fulfill them.
			else {
				
				//RECUSRIVE COMPONENT: For each precondition, find paths that lead to them.
				for (String p : preconditions) {
					System.out.println(">>> PRECONDITION: " + p);
					fulfilled = evaluateGoal(p);
					
					//If the goal has been fulfilled previously, break out of recursion.
					if (fulfilled) {
						break;
					}
				}
			}
			
			//Break out of recursion if the action has been fulfilled.
			if (fulfilled) {
				break;
			}

		}

		//Return whether or not the action has been fulfilled.
		return fulfilled;

	}

	//This is an abstraction of a query that gets a list of actions which cause the
	//specified postconditions.
	public ArrayList<String> getActionsForPostconditions(String postconditions) {
		ArrayList<String> actions = interpret("?(causes-PropProp ?ACTIONS "
				+ postconditions + ")");
		return actions;
	}
	
	//This is an abstraction of a query that gets a list of preconditions that
	//are required by the specified action.
	public ArrayList<String> getPreconditionsForAction(String action) {
		ArrayList<String> preconditions = interpret("?(preconditionFor-Props ?CONDITION "
				+ action + ")");
		return preconditions;
	}
	
	/**********************
	 * MARTHA functions are Cyc constants that have special functions
	 * in the MARTHA engine. The one here is say-TheMARTHAFunction, which, when
	 * executed, prints the specified a line of output for the user to see.
	 */
	public Boolean execute(String action) {
		
		//Was the action sucessful?
		Boolean successful = false;

		//Let the user know that action is being executed
		System.out.println("Execute " + action);
		
		//Regular expression to extract 1) the functional statement of the action and
		//2) its parameters. Matcher applies this regular expression to the action.
		Pattern p = Pattern
				.compile("\\(([-.\\w]+)\\s*(\\([\\w\\s-.\\(\\)]+\\))");
		Matcher m = p.matcher(action);
		
		//If there is a match...
		if (m.matches()) {
			
			//Debug INFO output.
			System.out.println("MATCHER: " + m.group(1));
			
			//If the function expression is say-TheMARTHAFunction...
			//SYNTAX: (say-TheMARTHAFunction <thing to be said>)
			if (m.group(1).equals("say-TheMARTHAFunction")) {
				
				//Print a nicely formatted and well-denoted output
				System.out.println();
				System.out.println("=================================");
				System.out.println("MARTHA>>> " + m.group(2));				//m.group(2) corresponds to <thing to be said>
				System.out.println("=================================");
				System.out.println();
				successful = true;	//Execution was successful
			}
		}
		
		//Return the success of an execution, which corresponds to the fulfillment of a goal.
		return successful;

	}
}

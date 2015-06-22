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

package moe.george.martha_weatherscenario;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
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
	private String assrtctx = "BaseKB";			// The context in which to make
												// assertions to Cyc
	
	// Parameters for the query interface.
	// MAX-TRANDFORMATION-DEPTH 10 specifies
	// that the inference engine should take 10 recursive steps when looking for
	// query answers. By default, this is zero, which lead to it giving only
	// one-step inferences!
	private String queryparams = ":MAX-TRANSFORMATION-DEPTH 10";
	
	//A comprehensive log of all calls to the interpret method, giving MARTHA
	//a sense of memory
	private ArrayList<String> logbook = new ArrayList<String>();
	
	//Queues to place actions that are to be executed, and plans to be evaluated
	private Queue<String> execution_queue = new LinkedList<String>();
	private Queue<ArrayList<String>> evaluation_queue = new LinkedList<ArrayList<String>>();

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
		initpath = init_file_path;	 // Set file path to one specified
		Charset charset = Charset.forName("US-ASCII"); // Specify the charset
		// which the file uses
		BufferedReader reader = Files.newBufferedReader(Paths.get(initpath),
				charset);	// Make a reader for the file
		
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
		logbook.add(line);

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
					AssertionImpl.findOrCreate(assertion, assrtctx);
				} 
				// = : fact (variables not allowed)
				else if (line.substring(0, 1).equals("=")) {
					String assertion = line.substring(1);
					// System.out.println(assertion);
					FactImpl.findOrCreate(assertion, assrtctx);
				} 
				// X : unassert assertion
				else if (line.substring(0, 1).equals("X")) {
					String assertion = line.substring(1);
					// System.out.println(assertion);
					Assertion deleteassert = AssertionImpl.findOrCreate(
							assertion, assrtctx);
					deleteassert.delete();
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
					
					//Create query in the context InferencePSC (the broadest query context) with specified query parameters.
					Query q = new Query(question, "InferencePSC", queryparams);
					
					//Get variables used in this query.
					Collection<Variable> queryVars = q.getQueryVariables();
					
					//Get query results
					KBInferenceResultSet queryResults = q.getResultSet();
					
					//Check if there are any query results.
					//If the list isn't empty, fetch results for the variables.
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
						//Silenced.
						//System.out.println("None.");
					}
					
					//Close Cyc interface to free up resources.
					queryResults.close();
					q.close();
				} 
				// | : Query the truth of a proposition (returns true/false)
				else if (line.substring(0, 1).equals("|")) {
					String question = line.substring(1);
					
					//Formulate query
					Query q = new Query(question, "InferencePSC", queryparams);
					
					//Return truth of query
					results.add(String.valueOf(q.isTrue()));
					
					//Free up resources.
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
		} catch (Exception e) {	//Catch error and give debug info, but don't die.
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
			results = interpret(input);
			
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
		
		//Goals now have the concept of levels (a rudimentary implementation of priority).
		//There are three distinct levels of goals, stored in this array.
		ArrayList<ArrayList<String>> goalsAtLevel = new ArrayList<ArrayList<String>>();
		
		//Initialize the goalsAtLevel array.
		for (int i = 0; i < 3; i++) {
			goalsAtLevel.add(new ArrayList<String>());
		}
		
		//Queue for goals to be unasserted after fulfillment,
		//So that the goal array does not change length during iteration
		LinkedList<String> delete_queue = new LinkedList<String>();
		
		//For each goal, get the importance of the goal and sort it into the appropriate location
		for (String g : goals) {
			int level = getGoalImportance(g);	//Get goal importance
			goalsAtLevel.get(level).add(g);		//Add the goal to the goalsAtLevel array
		}
		
		//Instead of the fulfilled boolean used before,
		//Now there is a state integer to store the status of the
		//Goal fulfillment operation.
		//State 0 represents the fulfilled state.
		int state = 0;
		
		//For each desire (which is a goal in this case), evaluate the possible paths to the goal.
		for (int i = 0; i < goalsAtLevel.size(); i++) {
			ArrayList<String> s = goalsAtLevel.get(i);
			for (String g : s) {
				findRouteToGoal(g, new ArrayList<String>());	//Find paths to the goal
				evaluatePlans();	//Evaluate the plans generated by enqueued by the search.
				state = execute();	//Execute the actions approved and enqueued by the evaluation.
				
				//If the state is not standard (fulfilled, delayed, etc.)
				//Purge the evaluation queue to halt the line of search.
				if(state != 0)
				{
					purgeQueue(evaluation_queue);
					break;
				}
				
				//If goal is fulfilled, and the goal is not a persistent goal (like happiness),
				//Then queue the goal for unassertion.
				else if(state == 0 && !goalIsPersistent(g))
				{
					delete_queue.add("X(desires USER " + g + ")");
				}
					
			}
			
			//If the state is unusual, then stop.
			if(state != 0)
			{
				break;
			}
		}

		//Unassert goals queued for deletion.
		for (String d : delete_queue) {
			interpret(d);
		}
	}

	//RECURSIVELY find action chains that lead to goals through backwards search.
	//Note that the goal doesn't need to be a user desire (i.e., primary goal).
	//It can also be target condition that needs to be reached, or an action
	//that needs preconditions
	public void findRouteToGoal(String goal, ArrayList<String> path) {
		//System.out.println(">>> GOAL        : "+goal);
		
		//An ArrayList to store the current cumulative chain of actions found thus far.
		//This is deepcloned from the given path in order to start a new branch in the recursion tree.
		ArrayList<String> newpath = deepClone(path);
		
		//Get preconditions for the goal.
		ArrayList<String> preconditions = getPreconditionsForAction(goal);
		
		//For each preconditions, find a route to them in order to fulfill them.
		for (String p : preconditions) {
			//System.out.println(">>> PRECONDITION: "+p);
			//RECUSRIVE COMPONENT: For each precondition, find paths that lead to them.
			findRouteToGoal(p, newpath);
		}
		
		//For a certain goal, find actions that result in the goal
		ArrayList<String> actions = getActionsForPostconditions(goal);
		
		//For each action found above, get the preconditions for the action.
		for (String a : actions) {
			//System.out.println(">>> ACTION      : "+a);
			newpath.add(a);		//Add action to the path before branching.
			//RECUSRIVE COMPONENT: For each action, find paths that lead to them.
			findRouteToGoal(a, newpath);
		}
		
		//If there are no preconditions, then the action chain is ready to be evaluated.
		if(actions.isEmpty() && preconditions.isEmpty())
		{
			queueEvaluation(newpath);	//Queue action for evaluation
		}
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

	//This is an abstraction of a query that returns the importance of a goal,
	//as specified in the Cyc KB.
	public int getGoalImportance(String goal) {
		ArrayList<String> importance = interpret("?(goalImportance USER "
				+ goal + " ?IMPORTANCE)");	//Get importance values
		
		//Parse the importance value string into a quantity.
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
		
		//Return that quantity.
		return level;
	}

	//Method to see if a goal is persistent (should not be unasserted automatically)
	public Boolean goalIsPersistent(String goal) {
		return (getTruthOf("(goalIsPersistent " + goal + " True)"));
	}

	//An abstraction for the use of interpret("|"+...), returns the true/false
	//of a statement.
	public Boolean getTruthOf(String statement) {
		return (interpret("|" + statement).get(0).equals("true"));
	}
	
	//Given a lisp expression, return the "key words," i.e. the function names
	//inside the entire expression. (For instance, "knows" in "(knows USER...").
	public ArrayList<String> getKeyWords(String statement)
	{
		//ArrayList to store results.
		ArrayList<String> results = new ArrayList<String>();
		
		//A regular expression to match the key words in the given statement.
		Pattern p = Pattern.compile("\\(([\\-.\\w]+)");
		Matcher m = p.matcher(statement);
		int start = 0;
		
		//Find the groups captured by the regular expression.
		while(m.find(start))
		{
			//System.out.println(statement);
			//System.out.println(m.group(1));
			
			//Add the results to the group, and set up the search
			//to continue at the end of this one.
			results.add(m.group(1));
			start = m.end();
		}
		
		//Return found key words.
		return results;		
	}
	
	//Queue actions to be executed by the execute() function.
	//Enables actions to be executed all at once (non-blocking).
	public int queueExecution(String action)
	{
		execution_queue.add(action);
		System.out.println("EXEC-QUEUED: "+action);
		return 0;
	}
	
	//Queue action chains to be evaluated. Useful to compare a large
	//set of possible actions.
	public int queueEvaluation(ArrayList<String> path)
	{
		evaluation_queue.add(path);
		System.out.println("EVAL-QUEUED: "+path);
		return 0;
	}
	
	//Get the next action in the execution queue.
	public String getEnqueuedAction()
	{
		return(execution_queue.poll());
	}
	
	/**********************
	 * MARTHA functions are Cyc constants that have special functions
	 * in the MARTHA engine. The one here is say-TheMARTHAFunction, which, when
	 * executed, prints the specified a line of output for the user to see.
	 */
	public int execute() {
		//Store the state of the operation. 0 is suceessful.
		int state = 0;
		
		//Get the first action from the execution queue.
		String action = getEnqueuedAction();
		
		//While there are still actions in the queue...execute them.
		while(action != null)
		{
			//Let the user know that action is being executed
			System.out.println("Execute " + action);
			
			//Regular expression to extract 1) the functional statement of the action and
			//2) its parameters. Matcher applies this regular expression to the action.
			Pattern p = Pattern
					.compile("\\(([-.\\w]+)\\s*(\\([\\w\\s-.\\(\\)]+\\))\\)");
			Matcher m = p.matcher(action);
			
			//If there is a match...
			if (m.matches()) {
				//Debug INFO output.
				// System.out.println("MATCHER: "+m.group(1));
				
				//If the function expression is say-TheMARTHAFunction...
				//SYNTAX: (say-TheMARTHAFunction <thing to be said>)
				if (m.group(1).equals("say-TheMARTHAFunction")) {
					//If MARTHA doesn't know if the user knows a fact, then ask the user first.
					if (!getTruthOf("(knows USER " + m.group(2) + ")") && !getTruthOf("(query-TheMARTHAFunction (knows USER " + m.group(2) + "))")) {
						queueExecution("(query-TheMARTHAFunction (knows USER " + m.group(2) + "))");
					}
					
					//If martha knows for sure that the user doesn't know a fact, and if MARTHA already asked,
					//the go ahead and state the fact.
					else if(!getTruthOf("(knows USER " + m.group(2) + ")") && getTruthOf("(query-TheMARTHAFunction (knows USER " + m.group(2) + "))"))
					{	
						//Print a nicely formatted and well-denoted output
						System.out.println();
						System.out.println("=================================");
						System.out.println("MARTHA>>> " + m.group(2));
						System.out.println("=================================");
						System.out.println();
						interpret(">"+action);	//Assert the action so that it has happened.
					} else {
						System.out.println("USER already knows " + m.group(2));
					}
				}
				
				//If the function expression is query-TheMARTHAFunction...
				//SYNTAX: (query-TheMARTHAFunction <thing to be asked>)
				else if(m.group(1).equals("query-TheMARTHAFunction"))
				{
					//Print a nicely formatted and well-denoted output
					System.out.println();
					System.out.println("=================================");
					System.out.println("MARTHA>>> " + getKeyWords(m.group(2)).get(0) + " " +getKeyWords(m.group(2)).get(1) + "?");
					System.out.println("=================================");
					System.out.println();
					
					interpret(">"+action);	//Assert the action so that it has happened.
					purgeQueue(execution_queue);	//Purge the queue to halt execution and let the user respond to the question.
					return 1;	//Pending user input state.
				}
			}
			
			//Get the next action
			action = getEnqueuedAction();
		}
		
		//Return the state of the execution.
		return state;
	}
	
	//Deepclone an ArrayList, rather than just copy a reference.
	//This returns an entirely new ArrayList that is disconnected
	//from the given one.
	private ArrayList<String> deepClone(ArrayList<String> original)
	{
		
		//For each element in the original, make an identical copy in the new
		//independent ArrayList.
		ArrayList<String> a = new ArrayList<String>(original.size());
		for(String o : original)
		{
			a.add(new String(o));
		}
		return a;
	}
	
	//Evaluate the desirability of proposed plans in the evaluation queue.
	public void evaluatePlans()
	{
		//Get first plan to be evaluated
		ArrayList<String> a = evaluation_queue.poll();
		
		//TODO: Create actual plan selection premise.
		//Current premise is to selected shortest path
		
		//Instantiate some variables
		ArrayList<String> candidate = a;
		int shortest = a.size();
		
		//While there are still plans to be evaluated...
		while(a != null)
		{
			//If a is shorter than the shortest plan, then make a the candidate.
			if(a.size() < shortest)
			{
				candidate = a;
				shortest = a.size();
				System.out.print("*");
			}
			System.out.println(a);
			a = evaluation_queue.poll();
		}
		
		//Queue all actions in the approved plan for execution.
		for(String c : candidate)
		{
			queueExecution(c);
		}
	}
	
	//Clear all the contents of a queue.
	private void purgeQueue(Queue<?> q)
	{
		q.clear();
	}
}

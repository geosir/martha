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


package moe.george.martha_multithreaded_fork;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
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
	
	//A comprehensive log of all calls to the interpret method, giving MARTHA
		//a sense of memory
	private ArrayList<String[]> logbook = new ArrayList<String[]>();
	
	//Queues to place actions that are to be executed, and plans to be evaluated
	private Queue<String> execution_queue = new LinkedList<String>();
	private Queue<LinkedHashSet<String>> evaluation_queue = new LinkedList<LinkedHashSet<String>>();
	
	//Constants used in the recursive planning search.
	private int max_forwards_depth = 10;	//Maximum forward steps in the plan
	private int max_backwards_depth = -5;	//Maximum backwards dependencies in the plan
	private int legitimacy_threshold = 15;	//Minimum score needed for a plan to be even considered for execution.

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
		//Boolean indicating whether the entry should be logged or ignored.
		Boolean ignore = false;

		//Wrapped in try-catch in order to not die if there is bad input.
		try {
			
			//Interpret what to do with the input using the operator (FIRST character in line).
			
			if (line.length() > 1) {
				// > : assertion (variables allowed)
				if (line.substring(0, 1).equals(">")) {
					String assertion = line.substring(1);
					AssertionImpl.findOrCreate(assertion, assrtctx);
				} 
				// = : fact (variables not allowed)
				else if (line.substring(0, 1).equals("=")) {
					String assertion = line.substring(1);
					FactImpl.findOrCreate(assertion, assrtctx);
				} 
				// X : unassert assertion
				else if (line.substring(0, 1).equals("X")) {
					String assertion = line.substring(1);
					Assertion deleteassert = AssertionImpl.findOrCreate(
							assertion, assrtctx);
					deleteassert.delete();
					System.out.println("DELETED");
				} 
				// + : create new constant
				else if (line.substring(0, 1).equals("+")) {
					String addition = line.substring(1);
					KBIndividualImpl.findOrCreate(addition);
				} 
				// * : create new predicate
				else if (line.substring(0, 1).equals("*")) {
					String addition = line.substring(1);
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
								String result = queryResults.getKBObject(v)
										.toString();
								results.add(result);
							}
						}
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
					ignore = true;	//Do not log into the history log
				} 
				//Give error and do nothing if there is a bad operator.
				else {
					System.out.println("Not understood: " + line);
					ignore = true;	//Do not log into the history log
				}
				
				//UNLESS we are NOT to log it into the history...
				if (!ignore) {
					String[] new_entry = { line.substring(1),
							line.substring(0, 1) };
					logbook.add(new_entry);	//Add the interpret prompt into the history log.
				}
			}
		} catch (Exception e) { //Catch error and give debug info, but don't die.
			
			e.printStackTrace();
			System.out
					.println("Warning: Could not interpret \"" + line + "\".");
		}

		//Return ArrayList with results.
		return (results);
	}

	// Start for a separate query method, but it doesn't seem worth it.
	/*
	 * private HashMap<String,ArrayList<String>> query(String question) {
	 * HashMap<String,ArrayList<String>> results = new
	 * HashMap<String,ArrayList<String>>(); try { Query q = new Query(question,
	 * "InferencePSC", queryparams); Collection<Variable> queryVars =
	 * q.getQueryVariables(); KBInferenceResultSet queryResults =
	 * q.getResultSet();
	 * 
	 * if (!(queryResults.getCurrentRowCount() == 0)) { while
	 * (queryResults.next()) { for (Variable v : queryVars) {
	 * 
	 * System.out.print(v + ": "); String result =
	 * queryResults.getKBObject(v).toString(); if(!results.keySet().contains(v))
	 * { ArrayList<String> newone = results.get(v); newone.add(result);
	 * results.put(v.toString(), newone); } else { results.get(v).add(result); }
	 * System.out.println(result); } } } else { //System.out.println("None."); }
	 * queryResults.close(); q.close(); } catch (Exception e) {} return results;
	 * }
	 */

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
		}
		// Otherwise, do nothing. No meaningful input.
		
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
		
		//Possible actions that MARTHA can do to start a line of planning
		String[] possible_actions = { "(say-TMF ?SOMETHING)",
				"(query-TMF ?SOMETHING)" };
		
		//Possible facts that can feed into the actions to generate a valid inital action
		//Unused.
		ArrayList<String> feed = new ArrayList<String>();
		//Get these facts from the last 10 entries in the logbook.
		//Unused. But should be used! Using generate() instead.
		for (int i = 1; i <= 10 && i < logbook.size(); i++) {
			feed.add(logbook.get(logbook.size() - i)[0]);
		}
		
		//For each possible action, perform a forwards search with the action-fact pair generated above.
		for (String p : possible_actions) {
			forwardsSearch(
					"(" + getKeyWords(p).get(0) + " " + generate() + ")",
					new LinkedHashSet<String>(), 0);
		}

		/*OLD STUFF FROM PREVIOUS VERSIONS THAT IS NOW OBSELETE.
		 * ArrayList<String> goals = interpret("?(desires USER ?DESIRES)");
		 * ArrayList<ArrayList<String>> goalsAtLevel = new
		 * ArrayList<ArrayList<String>>(); for (int i = 0; i < 3; i++) {
		 * goalsAtLevel.add(new ArrayList<String>()); } LinkedList<String>
		 * delete_queue = new LinkedList<String>(); for (String g : goals) { int
		 * level = getGoalImportance(g); goalsAtLevel.get(level).add(g); }
		 * 
		 * int state = 0;
		 * 
		 * for (int i = 0; i < goalsAtLevel.size(); i++) { ArrayList<String> s =
		 * goalsAtLevel.get(i); for (String g : s) { findRouteToGoal(g, new
		 * ArrayList<String>()); evaluatePlans(); state = execute(); if(state !=
		 * 0) { purgeQueue(evaluation_queue); break; } else if(state == 0 &&
		 * !goalIsPersistent(g)) { delete_queue.add("X(desires USER " + g +
		 * ")"); }
		 * 
		 * } if(state != 0) { break; } }
		 * 
		 * for (String d : delete_queue) { interpret(d); }
		 */
	}

	//Generate feed facts for initial actions.
	public String generate() {
		String[] feed = { "ChicagoBotanicGardens", "FiveDollarSteakSandwich" };
		return (generate(feed[new Random().nextInt(feed.length)]));
	}

	//Generate feed facts for initial actions -- unused.
	public String generate(String feed) {

		//Formulate query to find facts about the subject in feed.
		String input = "(?PRED " + feed + " ?FACT)";
		//List to store facts.
		ArrayList<String> facts = new ArrayList<String>();
		
		try {
			
			//Look for facts and predicates about the subject, using the
			//expressions from the query interpreter. Modifed to replace
			//?PRED and ?FACT above with actual results.
			Query q = new Query(input, "InferencePSC", queryparams);
			Collection<Variable> queryVars = q.getQueryVariables();
			KBInferenceResultSet queryResults = q.getResultSet();
			if (!(queryResults.getCurrentRowCount() == 0)) {
				while (queryResults.next()) {
					String temp = input;
					for (Variable v : queryVars) {

						String result = queryResults.getKBObject(v).toString();
						temp = temp.replace(v.toString(), result);	//Replace ?PRED and ?FACT with actual results.
					}
					facts.add(temp);
				}
			}
			
			//Of the facts, get a random one.
			input = facts.get(new Random().nextInt(facts.size()));
			
			//Free up resources.
			queryResults.close();
			q.close();
		} catch (Exception e) {
		}
		
		//Return random seed fact.
		return input;
	}

	//VERY IMPORTANT: This is the recursive backwards search algorithm. It recursively looks for dependenceies for actions and goals.
	public LinkedHashSet<String> backwardsSearch(String goal, LinkedHashSet<String> path, int depth) {
		
		
		
		//System.out.println(">>> BACKWARDS   : " + goal + " " + depth);
		
		//An ArrayList to store the current cumulative chain of actions found thus far.
		//This is deepcloned from the given path in order to start a new branch in the recursion tree.
		LinkedHashSet<String> newpath = deepClone(path);
		
		//Add the current goal to the path.
		newpath.add(goal);
		
		//Get actions leadig to the goal, and get preconditions for the goal.
		ArrayList<String> actions = getActionsForPostconditions(goal);
		ArrayList<String> preconditions = getPreconditionsForAction(goal);
		
		//If we aren't exceeding the max dependency depth...
		if(depth >= max_backwards_depth)
		{
			//For each action found, find dependencies for those.
			for (String a : actions) {
				newpath = backwardsSearch(a, newpath, depth-1);
			}
			
			//For each precondition found...
			for (String p : preconditions) {
				
				//If the precondition isn't already fulfilled...
				if(!getTruthOf(p))
				{
				
					//Do a backwards search to find actions to fulfill
					//those preconditions.
					newpath = backwardsSearch(p, newpath, depth-1);
					
					//If the result indicates that this is the root of the search,
					//that is, there are no actions to fulfill this precondition...
					if(newpath.contains("ROOT"+(depth-1)))
					{
						newpath.add("IMPOSSIBLE");	//Mark this line of search as impossible
						
						//System.out.println(">>>>>> IMPOSSIBLE - PRECONDITION CANNOT BE MET <<<<<<     "+p);
						return newpath;	//Abort the recursion!
					}
				}
			}
			
			//If there are no more actions and no more preconditions, 
			//We've reached a root.
			if(actions.isEmpty() && preconditions.isEmpty())
			{
				newpath.add("ROOT"+depth);
				//Just curious what a new forward search would do here.
				//Need to put this somewhere useful
				//it would be nice to spawn forward searches from found roots,
				//but it spawns an endless amount through looping!
				//forwardsSearch(goal, new LinkedHashSet<String>(), 0);

			}
		}
		else
		{
			//If there are still unfilfilled preconditions
			if(!preconditions.isEmpty())
			{
				newpath.add("IMPOSSIBLE");	//Mark this line of search as impossible.
				//System.out.println(">>>>>> IMPOSSIBLE - REACHED MAX DEPTH <<<<<<");
			}
		}
		
		//Return results.
		return newpath;
	}

	//Forwards search algorithm. Given a goal, this looks for what actions can stem from it into the future.
	public void forwardsSearch(String goal, LinkedHashSet<String> path, int depth) {
		//System.out.println(">>> FORWARDS    : " + goal + " " + depth);
		
		//An ArrayList to store the current cumulative chain of actions found thus far.
				//This is deepcloned from the given path in order to start a new branch in the recursion tree.
		LinkedHashSet<String> newpath = deepClone(path);
		//Add the current goal to the chain.
		newpath.add(goal);
		
		//If we haven't exceeded the max forward search length...
		if(depth <= max_forwards_depth)
		{
			//Get the conditions fulfilled by the current situation.
			ArrayList<String> conditions = resultsInConditions(goal);
	
			//Find the actions enabled by the current situation
			ArrayList<String> actions = enablesActions(goal);
			
			//For each action, do a backwards search for dependencies.
			for (String a : actions) {
				newpath = backwardsSearch(a, newpath, depth-1);
				
				//If this line of search is impossible, abort!
				if(newpath.contains("IMPOSSIBLE"))
				{
					return;
				}
				
				//For each possible action chain, find where it can lead.
				forwardsSearch(a, newpath, depth+1);
				
				/*TODO: There seems to be a mistake here!
				 * This search should only abort for actions that cannot be fulfilled,
				 * not for all actions, like it is here! I.e., once we hit an impossible action,
				 * we abort everything; that's not necessary. Need to fix the logic here later.
				 */
			}
			
			//For each condition, do a forwards search, e.g. for actions that they enable.
			for (String c : conditions) {
				forwardsSearch(c, newpath, depth+1);
			}
			
			//If there are no more actions to take,
			//We've reached the end. Queue the path for evaluation.
			if (actions.isEmpty()) {
				queueEvaluation(newpath);
			}
		}
		
		//If we've reached the end, and the chain isn't impossible, queue it for evaluation.
		else
		{
			if(!newpath.contains("IMPOSSIBLE")) queueEvaluation(newpath);
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
	
	//This is an abstraction of a query that gets a list of conditions that
		//result from a specific situation (action).
	public ArrayList<String> resultsInConditions(String action) {
		ArrayList<String> postconditions = interpret("?(causes-PropProp "
				+ action + " ?CONDITIONS)");
		return postconditions;
	}

	//This is an abstraction of a query that gets a list of actions that
		//are enabled by a specific situation (condition).
	public ArrayList<String> enablesActions(String precondition) {
		ArrayList<String> preconditions = interpret("?(preconditionFor-Props "
				+ precondition + " ?ACTIONS)");
		return preconditions;
	}

	//This is an abstraction of a query that returns the importance of a goal,
		//as specified in the Cyc KB.
	public int getGoalImportance(String goal) {
		ArrayList<String> importance = interpret("?(goalImportance USER "
				+ goal + " ?IMPORTANCE)"); //Get importance values
		
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
	public ArrayList<String> getKeyWords(String statement) {
		
		//ArrayList to store results.
		ArrayList<String> results = new ArrayList<String>();
		
		//A regular expression to match the key words in the given statement.
		Pattern p = Pattern.compile("\\(([\\-.\\w]+)");
		Matcher m = p.matcher(statement);
		int start = 0;
		
		//Find the groups captured by the regular expression.
		while (m.find(start)) {
			
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
	public int queueExecution(String action) {
		execution_queue.add(action);
		//System.out.println("EXEC-QUEUED: " + action);
		return 0;
	}

	//Queue action chains to be evaluated. Useful to compare a large
		//set of possible actions.
	public int queueEvaluation(LinkedHashSet<String> path) {
		evaluation_queue.add(path);
		//System.out.println("EVAL-QUEUED: " + path);
		return 0;
	}

	//Get the next action in the execution queue.
	public String getEnqueuedAction() {
		return (execution_queue.poll());
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
		while (action != null) {
			//Let the user know that action is being executed
			//Silenced.
			//System.out.println("Execute " + action);
			
			//Regular expression to extract 1) the functional statement of the action and
			//2) its parameters. Matcher applies this regular expression to the action.
			Pattern p = Pattern
					.compile("\\(([-.\\w]+)\\s*(\\([\\w\\s-.\\(\\)]+\\))\\)");
			Matcher m = p.matcher(action);
			
			//If there is a match...
			if (m.matches()) {
				
				//If the function expression is say-TMF...
				//SYNTAX: (say-TMF <thing to be said>)
				//Where TMF is short for "TheMARTHAFunction"
				if (m.group(1).equals("say-TMF")) {
					
					//Say what needs to be said.
					System.out.println();
					System.out.println("=================================");
					System.out.println("MARTHA>>> " + m.group(2));
					System.out.println("=================================");
					System.out.println();
				} 
				//If the function expression is query-TMF...
				//SYNTAX: (query-TMF <thing to be asked>)
				else if (m.group(1).equals("query-TMF")) {
					
					//Ask the user what needs to be asked.
					System.out.println();
					System.out.println("=================================");
					System.out.println("MARTHA>>> " + m.group(2) + "?");
					System.out.println("=================================");
					System.out.println();

					//Assert that it has been asked
					interpret(">" + action);
					purgeQueue(execution_queue); //Purge the queue to halt execution and let the user respond to the question.
					return 1; // Pending user input state.
				}
			}
			
			//Get the next action
			action = getEnqueuedAction();
		}
		
		//Return the state of the execution.
		return state;
	}

	//Deepclone an LinkedHashSet, rather than just copy a reference.
		//This returns an entirely new ArrayList that is disconnected
		//from the given one.
	private LinkedHashSet<String> deepClone(LinkedHashSet<String> original) {
		
		//For each element in the original, make an identical copy in the new
				//independent LinkedHashSet.
		LinkedHashSet<String> a = new LinkedHashSet<String>(original.size());
		for (String o : original) {
			a.add(new String(o));
		}
		return a;
	}

	//Evaluate the desirability of proposed plans in the evaluation queue.
	public void evaluatePlans() {
		try{
			
			//Get first plan to be evaluated
			LinkedHashSet<String> a = evaluation_queue.poll();
	
			//Evaluation premise:
			// Current premise is to select series of actions
			// With highest sum value.
						
	
			//Instantiate some variables
			LinkedHashSet<String> candidate = a;
			int highest_value = 0;
	
			while (a != null) {
				
				//Cleanup a to remove all ROOT value flags.
				Iterator<String> it = a.iterator();
				while(it.hasNext())
				{
					String test = it.next();
					if(test.contains("ROOT"))
					{
						it.remove();
					}
				}
				
				//Running total of the plan's sum outcome
				int current_value = 0;
				
				//Add up the value of all actions in the plan
				for(String s : a)
				{
					current_value = getValueOfState(s) + current_value;
				}
				
				//If the plan is of equal or higher value of the highest value,
				//Add it to the candidate list.
				if(current_value > highest_value)
				{
					candidate = a;
					highest_value = current_value;
				}
				else if(current_value == highest_value)
				{
					candidate.addAll(a);
				}
				//System.out.println("<"+current_value+"> "+a);
				a = evaluation_queue.poll();
			}
			
			//If the highest value exceeds the minimum score needed,
			//Go ahead an queue for execution.
			if(highest_value >= legitimacy_threshold)
			{
				System.out.println("APPROVED: "+candidate);
				System.out.println("SCORE: "+highest_value);
				
				//Queue all actions in the approved plan for execution.
				for (String c : candidate) {
					queueExecution(c);
				}
			}
			else
			{
				//System.out.println("THRESHOLD UNMET: " + candidate + " " + highest_value);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Warning: Evaluation failed.");
		}
	}

	//Clear all the contents of a queue.
	private void purgeQueue(Queue<?> q) {
		q.clear();
	}
	
	//Get the value of an action or condition, as stated in the Cyc KB.
	public int getValueOfState(String state)
	{
		try{
			//Return the value from a query.
			return(new Integer(interpret("?(stateValue USER "+state+" ?VALUE)").get(0)));
		} catch(Exception e)
		{
			//System.out.println("STATE VALUE ERROR: "+state);
			//If the query makes no sense, or if there's an error, then default to zero.
			return 0;
		}
	}
	
	//Method to change the context of assertions. To be used in the future
	//for changing timescale consciousness.
	public void changeContext(String context)
	{
		assrtctx = context;
	}
}

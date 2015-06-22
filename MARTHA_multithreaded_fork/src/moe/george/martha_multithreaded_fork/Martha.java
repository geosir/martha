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

	private String initpath = "initfile.martha";
	private String assrtctx = "BaseKB";
	private String queryparams = ":MAX-TRANSFORMATION-DEPTH 10";
	private ArrayList<String[]> logbook = new ArrayList<String[]>();
	private Queue<String> execution_queue = new LinkedList<String>();
	private Queue<LinkedHashSet<String>> evaluation_queue = new LinkedList<LinkedHashSet<String>>();
	private int max_forwards_depth = 10;
	private int max_backwards_depth = -5;
	private int legitimacy_threshold = 15;

	public Martha(String context) throws SessionConfigurationException,
			SessionCommunicationException, SessionInitializationException,
			CreateException, KBTypeException {
		System.out.println("Creating new MARTHA...");

		System.out.println("Acquiring a cyc server... "
				+ CycSessionManager.getCurrentSession().getServerInfo()
						.getCycServer());

		System.out.print("Setting cyc user... ");
		CycSessionManager.getCurrentSession().getOptions()
				.setCyclistName("TheUser");
		System.out.println(CycSessionManager.getCurrentSession().getOptions()
				.getCyclistName());

		assrtctx = context;
		ContextImpl.findOrCreate(context);
	}

	public void initFromFile(String init_file_path) throws IOException {
		initpath = init_file_path;
		Charset charset = Charset.forName("US-ASCII");
		BufferedReader reader = Files.newBufferedReader(Paths.get(initpath),
				charset);
		String line = null;
		while ((line = reader.readLine()) != null) {
			interpret(line);
		}
		reader.close();
	}

	public void initFromFile() throws IOException {
		initFromFile(initpath);
	}

	public ArrayList<String> interpret(String line) {
		ArrayList<String> results = new ArrayList<String>();
		Boolean ignore = false;

		try {
			if (line.length() > 1) {
				if (line.substring(0, 1).equals(">")) {
					String assertion = line.substring(1);
					AssertionImpl.findOrCreate(assertion, assrtctx);
				} else if (line.substring(0, 1).equals("=")) {
					String assertion = line.substring(1);
					FactImpl.findOrCreate(assertion, assrtctx);
				} else if (line.substring(0, 1).equals("X")) {
					String assertion = line.substring(1);
					Assertion deleteassert = AssertionImpl.findOrCreate(
							assertion, assrtctx);
					deleteassert.delete();
					System.out.println("DELETED");
				} else if (line.substring(0, 1).equals("+")) {
					String addition = line.substring(1);
					KBIndividualImpl.findOrCreate(addition);
				} else if (line.substring(0, 1).equals("*")) {
					String addition = line.substring(1);
					KBPredicateImpl.findOrCreate(addition);
				} else if (line.substring(0, 1).equals("?")) {
					String question = line.substring(1);
					Query q = new Query(question, "InferencePSC", queryparams);
					Collection<Variable> queryVars = q.getQueryVariables();
					KBInferenceResultSet queryResults = q.getResultSet();
					if (!(queryResults.getCurrentRowCount() == 0)) {
						while (queryResults.next()) {
							for (Variable v : queryVars) {
								String result = queryResults.getKBObject(v)
										.toString();
								results.add(result);
							}
						}
					}
					queryResults.close();
					q.close();
				} else if (line.substring(0, 1).equals("|")) {
					String question = line.substring(1);
					Query q = new Query(question, "InferencePSC", queryparams);
					results.add(String.valueOf(q.isTrue()));
					q.close();
				} else if (line.substring(0, 1).equals("#")) {
					// It's a comment, do nothing.
					ignore = true;
				} else {
					System.out.println("Not understood: " + line);
					ignore = true;
				}
				if (!ignore) {
					String[] new_entry = { line.substring(1),
							line.substring(0, 1) };
					logbook.add(new_entry);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out
					.println("Warning: Could not interpret \"" + line + "\".");
		}

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

	public ArrayList<String> interpretFromUser(String input) {
		ArrayList<String> results = new ArrayList<String>();
		if (input.length() > 1) {
			results = interpret(input);
			// Do something so that MARTHA asserts that the user knows that the
			// user asserted the assertion asserted here.

			if (input.substring(0, 1).equals(">")) {
				interpret(">(knows USER " + input.substring(1) + ")");
			}
		}
		// Otherwise, do nothing. No meaningful input.
		return (results);
	}

	public void planForGoals() {
		String[] possible_actions = { "(say-TMF ?SOMETHING)",
				"(query-TMF ?SOMETHING)" };
		ArrayList<String> feed = new ArrayList<String>();
		for (int i = 1; i <= 10 && i < logbook.size(); i++) {
			feed.add(logbook.get(logbook.size() - i)[0]);
		}
		for (String p : possible_actions) {
			forwardsSearch(
					"(" + getKeyWords(p).get(0) + " " + generate() + ")",
					new LinkedHashSet<String>(), 0);
		}

		/*
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

	
	public String generate() {
		String[] feed = { "ChicagoBotanicGardens", "FiveDollarSteakSandwich" };
		return (generate(feed[new Random().nextInt(feed.length)]));
	}

	public String generate(String feed) {

		String input = "(?PRED " + feed + " ?FACT)";
		ArrayList<String> facts = new ArrayList<String>();
		try {
			Query q = new Query(input, "InferencePSC", queryparams);
			Collection<Variable> queryVars = q.getQueryVariables();
			KBInferenceResultSet queryResults = q.getResultSet();
			if (!(queryResults.getCurrentRowCount() == 0)) {
				while (queryResults.next()) {
					String temp = input;
					for (Variable v : queryVars) {

						String result = queryResults.getKBObject(v).toString();
						temp = temp.replace(v.toString(), result);
					}
					facts.add(temp);
				}
			}
			input = facts.get(new Random().nextInt(facts.size()));
			queryResults.close();
			q.close();
		} catch (Exception e) {
		}
		return input;
	}

	public LinkedHashSet<String> backwardsSearch(String goal, LinkedHashSet<String> path, int depth) {
		
		
		
		//System.out.println(">>> BACKWARDS   : " + goal + " " + depth);
		LinkedHashSet<String> newpath = deepClone(path);
		newpath.add(goal);
		ArrayList<String> actions = getActionsForPostconditions(goal);
		ArrayList<String> preconditions = getPreconditionsForAction(goal);
		if(depth >= max_backwards_depth)
		{
			for (String a : actions) {
				newpath = backwardsSearch(a, newpath, depth-1);
			}
			for (String p : preconditions) {
				if(!getTruthOf(p))
				{
				
					newpath = backwardsSearch(p, newpath, depth-1);
					if(newpath.contains("ROOT"+(depth-1)))
					{
						newpath.add("IMPOSSIBLE");
						
						//System.out.println(">>>>>> IMPOSSIBLE - PRECONDITION CANNOT BE MET <<<<<<     "+p);
						return newpath;
					}
				}
			}
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
			if(!preconditions.isEmpty())
			{
				newpath.add("IMPOSSIBLE");
				//System.out.println(">>>>>> IMPOSSIBLE - REACHED MAX DEPTH <<<<<<");
			}
		}
		
			
		
		
		return newpath;
	}

	public void forwardsSearch(String goal, LinkedHashSet<String> path, int depth) {
		//System.out.println(">>> FORWARDS    : " + goal + " " + depth);
		LinkedHashSet<String> newpath = deepClone(path);
		newpath.add(goal);
		if(depth <= max_forwards_depth)
		{
			ArrayList<String> conditions = resultsInConditions(goal);
	
			ArrayList<String> actions = enablesActions(goal);
			for (String a : actions) {
				newpath = backwardsSearch(a, newpath, depth-1);
				if(newpath.contains("IMPOSSIBLE"))
				{
					return;
				}
				forwardsSearch(a, newpath, depth+1);
			}
			for (String c : conditions) {
				forwardsSearch(c, newpath, depth+1);
			}
			if (actions.isEmpty()) {
				queueEvaluation(newpath);
			}
		}
		else
		{
			if(!newpath.contains("IMPOSSIBLE")) queueEvaluation(newpath);
		}
	}

	public ArrayList<String> getActionsForPostconditions(String postconditions) {
		ArrayList<String> actions = interpret("?(causes-PropProp ?ACTIONS "
				+ postconditions + ")");
		return actions;
	}

	public ArrayList<String> getPreconditionsForAction(String action) {
		ArrayList<String> preconditions = interpret("?(preconditionFor-Props ?CONDITION "
				+ action + ")");
		return preconditions;
	}

	public ArrayList<String> resultsInConditions(String action) {
		ArrayList<String> postconditions = interpret("?(causes-PropProp "
				+ action + " ?CONDITIONS)");
		return postconditions;
	}

	public ArrayList<String> enablesActions(String precondition) {
		ArrayList<String> preconditions = interpret("?(preconditionFor-Props "
				+ precondition + " ?ACTIONS)");
		return preconditions;
	}

	public int getGoalImportance(String goal) {
		ArrayList<String> importance = interpret("?(goalImportance USER "
				+ goal + " ?IMPORTANCE)");
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
		return level;
	}

	public Boolean goalIsPersistent(String goal) {
		return (getTruthOf("(goalIsPersistent " + goal + " True)"));
	}

	public Boolean getTruthOf(String statement) {
		return (interpret("|" + statement).get(0).equals("true"));
	}

	public ArrayList<String> getKeyWords(String statement) {
		ArrayList<String> results = new ArrayList<String>();
		Pattern p = Pattern.compile("\\(([\\-.\\w]+)");
		Matcher m = p.matcher(statement);
		int start = 0;
		while (m.find(start)) {
			results.add(m.group(1));
			start = m.end();
		}
		return results;
	}

	public int queueExecution(String action) {
		execution_queue.add(action);
		//System.out.println("EXEC-QUEUED: " + action);
		return 0;
	}

	public int queueEvaluation(LinkedHashSet<String> path) {
		evaluation_queue.add(path);
		//System.out.println("EVAL-QUEUED: " + path);
		return 0;
	}

	public String getEnqueuedAction() {
		return (execution_queue.poll());
	}

	public int execute() {
		int state = 0;
		String action = getEnqueuedAction();
		while (action != null) {
			//System.out.println("Execute " + action);
			Pattern p = Pattern
					.compile("\\(([-.\\w]+)\\s*(\\([\\w\\s-.\\(\\)]+\\))\\)");
			Matcher m = p.matcher(action);
			if (m.matches()) {
				if (m.group(1).equals("say-TMF")) {
					System.out.println();
					System.out.println("=================================");
					System.out.println("MARTHA>>> " + m.group(2));
					System.out.println("=================================");
					System.out.println();
				} else if (m.group(1).equals("query-TMF")) {
					System.out.println();
					System.out.println("=================================");
					System.out.println("MARTHA>>> " + m.group(2) + "?");
					System.out.println("=================================");
					System.out.println();

					interpret(">" + action);
					purgeQueue(execution_queue);
					return 1; // Pending user input state.
				}
			}
			action = getEnqueuedAction();
		}
		return state;
	}

	private LinkedHashSet<String> deepClone(LinkedHashSet<String> original) {
		LinkedHashSet<String> a = new LinkedHashSet<String>(original.size());
		for (String o : original) {
			a.add(new String(o));
		}
		return a;
	}

	public void evaluatePlans() {
		try{
			LinkedHashSet<String> a = evaluation_queue.poll();
	
			// TODO: Create actual plan selection premise.
			// Current premise is select series of actions
			// With highest sum value.
						
	
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
				
				int current_value = 0;
				for(String s : a)
				{
					current_value = getValueOfState(s) + current_value;
				}
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
			if(highest_value >= legitimacy_threshold)
			{
				System.out.println("APPROVED: "+candidate);
				System.out.println("SCORE: "+highest_value);
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

	private void purgeQueue(Queue<?> q) {
		q.clear();
	}
	
	public int getValueOfState(String state)
	{
		try{
			return(new Integer(interpret("?(stateValue USER "+state+" ?VALUE)").get(0)));
		} catch(Exception e)
		{
			//System.out.println("STATE VALUE ERROR: "+state);
			return 0;
		}
	}
	
	public void changeContext(String context)
	{
		assrtctx = context;
	}
}

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

	private String initpath = "initfile.martha";
	private String assrtctx = "BaseKB";
	private String queryparams = ":MAX-TRANSFORMATION-DEPTH 10";
	private ArrayList<String> logbook = new ArrayList<String>();
	private Queue<String> execution_queue = new LinkedList<String>();
	private Queue<ArrayList<String>> evaluation_queue = new LinkedList<ArrayList<String>>();

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
			// System.out.println(line);
			interpret(line);
		}
		reader.close();
	}

	public void initFromFile() throws IOException {
		initFromFile(initpath);
	}

	public ArrayList<String> interpret(String line) {
		logbook.add(line);

		ArrayList<String> results = new ArrayList<String>();

		try {
			if (line.length() > 1) {
				if (line.substring(0, 1).equals(">")) {
					String assertion = line.substring(1);
					// System.out.println(assertion);
					AssertionImpl.findOrCreate(assertion, assrtctx);
				} else if (line.substring(0, 1).equals("=")) {
					String assertion = line.substring(1);
					// System.out.println(assertion);
					FactImpl.findOrCreate(assertion, assrtctx);
				} else if (line.substring(0, 1).equals("X")) {
					String assertion = line.substring(1);
					// System.out.println(assertion);
					Assertion deleteassert = AssertionImpl.findOrCreate(
							assertion, assrtctx);
					deleteassert.delete();
					System.out.println("DELETED");
				} else if (line.substring(0, 1).equals("+")) {
					String addition = line.substring(1);
					// System.out.println(addition);
					KBIndividualImpl.findOrCreate(addition);
				} else if (line.substring(0, 1).equals("*")) {
					String addition = line.substring(1);
					// System.out.println(addition);
					KBPredicateImpl.findOrCreate(addition);
				} else if (line.substring(0, 1).equals("?")) {
					String question = line.substring(1);
					Query q = new Query(question, "InferencePSC", queryparams);
					Collection<Variable> queryVars = q.getQueryVariables();
					KBInferenceResultSet queryResults = q.getResultSet();
					if (!(queryResults.getCurrentRowCount() == 0)) {
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
						//System.out.println("None.");
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
				} else {
					System.out.println("Not understood: " + line);
				}
				// System.out.println("Done.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out
					.println("Warning: Could not interpret \"" + line + "\".");
		}

		return (results);
	}

	public ArrayList<String> interpretFromUser(String input) {
		ArrayList<String> results = new ArrayList<String>();
		if (input.length() > 1) {
			results = interpret(input);
			// Do something so that MARTHA asserts that the user knows that the
			// user asserted the assertion asserted here.

			if (input.substring(0, 1).equals(">")) {
				interpret(">(knows USER " + input.substring(1) + ")");
			}
		} else {
			// Do nothing, no meaningful input.
		}
		return (results);
	}

	public void planForGoals() {
		ArrayList<String> goals = interpret("?(desires USER ?DESIRES)");
		ArrayList<ArrayList<String>> goalsAtLevel = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < 3; i++) {
			goalsAtLevel.add(new ArrayList<String>());
		}
		LinkedList<String> delete_queue = new LinkedList<String>();
		for (String g : goals) {
			int level = getGoalImportance(g);
			goalsAtLevel.get(level).add(g);
		}
		
		int state = 0;
		
		for (int i = 0; i < goalsAtLevel.size(); i++) {
			ArrayList<String> s = goalsAtLevel.get(i);
			for (String g : s) {
				findRouteToGoal(g, new ArrayList<String>());
				evaluatePlans();
				state = execute();
				if(state != 0)
				{
					purgeQueue(evaluation_queue);
					break;
				}
				else if(state == 0 && !goalIsPersistent(g))
				{
					delete_queue.add("X(desires USER " + g + ")");
				}
					
			}
			if(state != 0)
			{
				break;
			}
		}

		for (String d : delete_queue) {
			interpret(d);
		}
	}

	public void findRouteToGoal(String goal, ArrayList<String> path) {
		//System.out.println(">>> GOAL        : "+goal);
		ArrayList<String> newpath = deepClone(path);
		ArrayList<String> preconditions = getPreconditionsForAction(goal);
		for (String p : preconditions) {
			//System.out.println(">>> PRECONDITION: "+p);
			findRouteToGoal(p, newpath);
		}
		ArrayList<String> actions = getActionsForPostconditions(goal);
		for (String a : actions) {
			//System.out.println(">>> ACTION      : "+a);
			newpath.add(a);
			findRouteToGoal(a, newpath);
		}
		if(actions.isEmpty() && preconditions.isEmpty())
		{
			queueEvaluation(newpath);
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
	
	public ArrayList<String> getKeyWords(String statement)
	{
		ArrayList<String> results = new ArrayList<String>();
		Pattern p = Pattern.compile("\\(([\\-.\\w]+)");
		Matcher m = p.matcher(statement);
		int start = 0;
		while(m.find(start))
		{
			//System.out.println(statement);
			//System.out.println(m.group(1));
			results.add(m.group(1));
			start = m.end();
		}
		return results;		
	}
	
	public int queueExecution(String action)
	{
		execution_queue.add(action);
		System.out.println("EXEC-QUEUED: "+action);
		return 0;
	}
	
	public int queueEvaluation(ArrayList<String> path)
	{
		evaluation_queue.add(path);
		System.out.println("EVAL-QUEUED: "+path);
		return 0;
	}
	
	public String getEnqueuedAction()
	{
		return(execution_queue.poll());
	}
	
	public int execute() {
		int state = 0;
		String action = getEnqueuedAction();
		while(action != null)
		{
			System.out.println("Execute " + action);
			Pattern p = Pattern
					.compile("\\(([-.\\w]+)\\s*(\\([\\w\\s-.\\(\\)]+\\))\\)");
			Matcher m = p.matcher(action);
			if (m.matches()) {
				// System.out.println("MATCHER: "+m.group(1));
				if (m.group(1).equals("say-TheMARTHAFunction")) {
					if (!getTruthOf("(knows USER " + m.group(2) + ")") && !getTruthOf("(query-TheMARTHAFunction (knows USER " + m.group(2) + "))")) {
						queueExecution("(query-TheMARTHAFunction (knows USER " + m.group(2) + "))");
					}
					else if(!getTruthOf("(knows USER " + m.group(2) + ")") && getTruthOf("(query-TheMARTHAFunction (knows USER " + m.group(2) + "))"))
					{	
						System.out.println();
						System.out.println("=================================");
						System.out.println("MARTHA>>> " + m.group(2));
						System.out.println("=================================");
						System.out.println();
						interpret(">"+action);
					} else {
						System.out.println("USER already knows " + m.group(2));
					}
				}
				else if(m.group(1).equals("query-TheMARTHAFunction"))
				{
					System.out.println();
					System.out.println("=================================");
					System.out.println("MARTHA>>> " + getKeyWords(m.group(2)).get(0) + " " +getKeyWords(m.group(2)).get(1) + "?");
					System.out.println("=================================");
					System.out.println();
					
					interpret(">"+action);
					purgeQueue(execution_queue);
					return 1;	//Pending user input state.
				}
			}
			action = getEnqueuedAction();
		}
		return state;
	}
	
	private ArrayList<String> deepClone(ArrayList<String> original)
	{
		ArrayList<String> a = new ArrayList<String>(original.size());
		for(String o : original)
		{
			a.add(new String(o));
		}
		return a;
	}
	
	public void evaluatePlans()
	{
		ArrayList<String> a = evaluation_queue.poll();
		
		//TODO: Create actual plan selection premise.
		//Current premise is to selected shortest path
		
		ArrayList<String> candidate = a;
		int shortest = a.size();
		
		while(a != null)
		{
			if(a.size() < shortest)
			{
				candidate = a;
				shortest = a.size();
				System.out.print("*");
			}
			System.out.println(a);
			a = evaluation_queue.poll();
		}
		
		for(String c : candidate)
		{
			queueExecution(c);
		}
	}
	
	private void purgeQueue(Queue<?> q)
	{
		q.clear();
	}
}

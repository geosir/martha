package moe.george.martha_theoryofmind2;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cyc.kb.Variable;
import com.cyc.kb.client.ContextImpl;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KBTypeException;
import com.cyc.query.KBInferenceResultSet;
import com.cyc.query.Query;
import com.cyc.session.CycSessionManager;
import com.cyc.session.SessionCommunicationException;
import com.cyc.session.SessionConfigurationException;
import com.cyc.session.SessionInitializationException;

public class MarthaProcess extends Martha{
	private String assrtctx = "BaseKB"; // The context in which to make
	// assertions to Cyc
	private String defaultctx = assrtctx; // The default context (to revert to
											// after the context changes)

	// Parameters for the query interface.
	// MAX-TRANDFORMATION-DEPTH 10 specifies
	// that the inference engine should take 10 recursive steps when looking for
	// query answers. By default, this is zero, which lead to it giving only
	// one-step inferences!
	private String queryparams = ":MAX-TRANSFORMATION-DEPTH 1000";

	// Constants used in the recursive planning search.
	private int max_forwards_depth = 10; // Maximum forward steps in the plan
	private int max_backwards_depth = -5; // Maximum backwards dependencies in
											// the plan

	private int depth;

	private Martha parent;
	
	private String agent;

	private Queue<String> execution_queue = new LinkedList<String>();
	private Queue<LinkedHashSet<String>> evaluation_queue = new LinkedList<LinkedHashSet<String>>();

	private int legitimacy_threshold = 30; // Minimum score needed for a plan to

	// be even considered for execution.

	public MarthaProcess(Martha parent_martha, String context, String defaultcontext, int init_depth, String target_agent)
			throws SessionConfigurationException,
			SessionCommunicationException, SessionInitializationException,
			CreateException, KBTypeException {

		super(context);
		
		depth = init_depth;

		parent = parent_martha;
		
		agent = target_agent;

		// Let everyone know that a new MARTHA is being instantiated
		System.out.println("Creating new MARTHA Process...");

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
		defaultctx = defaultcontext; // Store the default context;
		ContextImpl.findOrCreate(context);

		// user = new Martha("userctx_"+context);
	}

	public void planGenerally() {

		// Possible actions that MARTHA can do to start a line of planning
		String[] possible_actions = { "(say-TMF ?SOMETHING)",
				"(query-TMF ?SOMETHING)", "(contradict-TMF ?SOMETHING)" };

		// Possible facts that can fed into the actions to generate a valid
		// inital action
		// Unused.
		// ArrayList<String> feed = new ArrayList<String>();

		// Get feed from the last few entries in the logbook.
		/*
		 * for (int i = 1; i <= 20 && i < logbook.size(); i++) {
		 * feed.addAll(Arrays.asList((logbook.get(logbook.size() -
		 * i)[0].split("\\s|\\(|\\)")))); }
		 */

		// Clean up feed, removing all query variables, ERROR, and IMPOSSIBLE
		// messages.
		/*
		 * Iterator<String> it = feed.iterator(); while(it.hasNext()) {
		 * if(it.next().equals("IMPOSSIBLE|ERROR|\\?\\w+")) { it.remove(); } }
		 */

		// For each possible action, perform a forwards search with the
		// action-fact pair generated above.

		/*
		 * for(String f : feed) { for (String p : possible_actions) {
		 * //System.out.println(f); String seed = "(?PRED " + f + " ?FACT)";
		 * //System.out.println(generate(seed)); forwardsSearch( "(" +
		 * getKeyWords(p).get(0) + " " + generate() + ")", new
		 * LinkedHashSet<String>(), 0); }
		 * 
		 * }
		 */

		for (String p : possible_actions) {
			// System.out.println(f);
			// System.out.println(generate(seed));
			forwardsSearch(
					"(" + getKeyWords(p).get(0) + " " + generate() + ")",
					new LinkedHashSet<String>(), 0);
		}
	}

	// Do long and deep searches based on random facts within Cyc.
	public void dream() {
		forwardsSearch(generate("(?PRED ?THING ?FACT)"),
				new LinkedHashSet<String>(), 0);
	}

	public void planForGoals() {

		try {
			System.out.println("DEPTH ============== "+depth);
			if(depth>0)
			{
				
				
				String next_agent = "MARTHA";
				if(agent.equals("MARTHA"))
				{
					next_agent = "USER";
				}
				
				String hypothetical = constructHypotheticalContext(next_agent);
				
				MarthaProcess martha_p = new MarthaProcess(this, hypothetical, defaultctx, depth-1, next_agent);
				martha_p.planForGoals();
				execute();
			}
			
		} catch (Exception e)
		{
			
		}
		
		System.out.println("DEPTH ============== "+depth);
		// Query the database to find the user's desires.
		ArrayList<String> goals = interpret("?(desires "+agent+" ?DESIRES)", assrtctx);
		// For each desire (which is a goal in this case), evaluate the possible
		// paths to the goal.
		for (String g : goals) {
			ArrayList<LinkedHashSet<String>> paths = backwardsSearch(g,
					new LinkedHashSet<String>(), 0);

			for (LinkedHashSet<String> p : paths) {
				if (!(paths.contains("ERROR|IMPOSSIBLE"))) {
					queueEvaluation(p);
				}
			}
		}

		evaluatePlans();
	}

	/*
	 * public void emulateUser() { user.planGenerallyFromKnows(); }
	 */

	// Generate feed facts for initial actions.
	public String generate() {
		String[] feed = { "ChicagoBotanicGardens", "FiveDollarSteakSandwich" };
		return (generate("(?PRED " + feed[new Random().nextInt(feed.length)]
				+ " ?FACT)"));
	}

	public String generate(String seed) {
		// List to store facts.
		ArrayList<String> facts = new ArrayList<String>();

		try {

			// Look for facts and predicates about the subject, using the
			// expressions from the query interpreter. Modifed to replace
			// ?PRED and ?FACT above with actual results.
			Query q = new Query(seed, "InferencePSC", queryparams);
			Collection<Variable> queryVars = q.getQueryVariables();
			KBInferenceResultSet queryResults = q.getResultSet();
			if (!(queryResults.getCurrentRowCount() == 0)) {
				while (queryResults.next()) {
					String temp = seed;
					for (Variable v : queryVars) {
						// Replace ?PRED and ?FACT with actual results.
						String result = queryResults.getKBObject(v).toString();
						temp = temp.replace(v.toString(), result);
					}
					facts.add(temp);
				}
			}

			// Of the facts, get a random one.
			seed = facts.get(new Random().nextInt(facts.size()));

			// Free up resources.
			queryResults.close();
			q.close();
		} catch (Exception e) {
		}

		// Return random seed fact.
		return seed;
	}

	// VERY IMPORTANT: This is the recursive backwards search algorithm. It
	// recursively looks for dependenceies for actions and goals.
	public ArrayList<LinkedHashSet<String>> backwardsSearch(String goal,
			LinkedHashSet<String> path, int depth) {

		//System.out.println(">>> BACKWARDS   : " + goal + " " + depth);

		// An ArrayList to store the current cumulative chain of actions found
		// thus far.
		// This is deepcloned from the given path in order to start a new branch
		// in the recursion tree.
		ArrayList<LinkedHashSet<String>> newpaths = new ArrayList<LinkedHashSet<String>>();
		LinkedHashSet<String> newpath = deepClone(path);

		newpath.add(goal);

		// Add the current goal to the path.
		// newpath.add(goal);

		// Get actions leading to the goal, and get preconditions for the goal.
		ArrayList<String> actions = getActionsForPostconditions(goal);
		ArrayList<String> preconditions = getPreconditionsForAction(goal);

		// If we aren't exceeding the max dependency depth...
		if (depth >= max_backwards_depth) {

			// Find and add preconditions of this state.
			for (String p : preconditions) {
				newpath.add(p);
			}
			for (String p : preconditions) {

				// If the precondition isn't already fulfilled...
				if (!getTruthOf(p)) {

					// Do a backwards search to find actions to fulfill
					// those preconditions.
					ArrayList<LinkedHashSet<String>> results = backwardsSearch(
							p, newpath, depth - 1);
					if (results.get(0).contains("ROOT" + (depth - 1))) {
						//newpath.add("IMPOSSIBLE");
						
						String newagent = "MARTHA";
						if(agent.equals("MARTHA"))
						{
							newagent = "USER";
						}
						
						newpath.add("(desires "+newagent+" "+p+")");
						newpaths.add(newpath);
						// System.out.println(">>>>>> IMPOSSIBLE - PRECONDITION CANNOT BE MET <<<<<<     "+p);
						return newpaths;
					} else {
						newpaths.addAll(results);
					}
				}
			}

			// For each action found, find dependencies for those.
			for (String a : actions) {
				/*
				 * XXX: THERE IS A LOGICAL PROBLEM HERE! Not all actions are
				 * necessary for a precondition to be fulfilled. Only one is.
				 * This FOR loop here loops through all of them and necessitates
				 * them. The new path should therefore be placed elsewhere, this
				 * would just be a backwards branch. (evidence: buys always goes
				 * with steals. Not optimal...)
				 */

				// Search backwards for action dependencies, starting a new
				// branch each time.
				// newpath = backwardsSearch(a, newpath, depth - 1);
				ArrayList<LinkedHashSet<String>> results = backwardsSearch(a,
						newpath, depth - 1);
				newpaths.addAll(results);
				/*
				 * TODO: Still poor logic here! Each action needs to spawn a
				 * whole new search tree!Right now it doesn't! Right now it just
				 * chooses the first action!!!
				 */
			}

			// If there are no more actions and no more preconditions,
			// We've reached a root.
			if (actions.isEmpty() && preconditions.isEmpty()) {
				newpath.add("ROOT" + depth);
				newpaths.add(newpath);
				// Just curious what a new forward search would do here.
				// Need to put this somewhere useful
				// it would be nice to spawn forward searches from found roots,
				// but it spawns an endless amount through looping!
				// forwardsSearch(goal, new LinkedHashSet<String>(), 0);

			}
		} else {
			// If there are still unfilfilled preconditions
			if (!preconditions.isEmpty()) {
				newpath.add("IMPOSSIBLE"); // Mark this line of search as
											// impossible.
				// System.out.println(">>>>>> IMPOSSIBLE - REACHED MAX DEPTH <<<<<<");
				newpaths.add(newpath);
			}
		}

		// Return results.

		System.out.println(newpaths);
		return newpaths;
	}

	// Forwards search algorithm. Given a goal, this looks for what actions can
	// stem from it into the future.
	public void forwardsSearch(String goal, LinkedHashSet<String> path,
			int depth) {
		// System.out.println(">>> FORWARDS    : " + goal + " " + depth);

		// An ArrayList to store the current cumulative chain of actions found
		// thus far.
		// This is deepcloned from the given path in order to start a new branch
		// in the recursion tree.
		LinkedHashSet<String> newpath = deepClone(path);
		// Add the current goal to the chain.
		newpath.add(goal);

		// If we haven't exceeded the max forward search length...
		if (depth <= max_forwards_depth) {
			// Get the conditions fulfilled by the current situation.
			ArrayList<String> conditions = resultsInConditions(goal);

			// Find the actions enabled by the current situation
			ArrayList<String> actions = enablesActions(goal);

			// Abort if path is impossible.
			if (newpath.contains("IMPOSSIBLE")) {
				return;
			}

			// For each action, do a backwards search for dependencies.
			for (String a : actions) {
				// For each possible action chain, find dependencies, then find
				// where it can lead.
				for (LinkedHashSet<String> possible : backwardsSearch(a,
						newpath, depth + 1)) {
					forwardsSearch(a, possible, depth + 1);
				}
			}

			// For each condition, do a forwards search, e.g. for actions that
			// they enable.
			for (String c : conditions) {
				forwardsSearch(c, newpath, depth + 1);
			}

			// If there are no more actions to take,
			// We've reached the end. Queue the path for evaluation.
			if (actions.isEmpty()) {
				queueEvaluation(newpath);
			}
		}

		// If we've reached the end, and the chain isn't impossible, queue it
		// for evaluation.
		else {
			if (!newpath.contains("IMPOSSIBLE"))
				queueEvaluation(newpath);
		}
	}

	// This is an abstraction of a query that gets a list of actions which cause
	// the
	// specified postconditions.
	public ArrayList<String> getActionsForPostconditions(String postconditions) {
		ArrayList<String> actions = interpret("?(causes-PropProp ?ACTIONS "
				+ postconditions + ")", assrtctx);
		return actions;
	}

	// This is an abstraction of a query that gets a list of preconditions that
	// are required by the specified action.
	public ArrayList<String> getPreconditionsForAction(String action) {
		ArrayList<String> preconditions = interpret("?(preconditionFor-Props ?CONDITION "
				+ action + ")", assrtctx);
		return preconditions;
	}

	// This is an abstraction of a query that gets a list of conditions that
	// result from a specific situation (action).
	public ArrayList<String> resultsInConditions(String action) {
		ArrayList<String> postconditions = interpret("?(causes-PropProp "
				+ action + " ?CONDITIONS)", assrtctx);
		return postconditions;
	}

	// This is an abstraction of a query that gets a list of actions that
	// are enabled by a specific situation (condition).
	public ArrayList<String> enablesActions(String precondition) {
		ArrayList<String> preconditions = interpret("?(preconditionFor-Props "
				+ precondition + " ?ACTIONS)", assrtctx);
		return preconditions;
	}

	public int queueExecution(String action) {
		execution_queue.add(action);
		return 0;
	}

	public String constructHypotheticalContext(String target_agent)
			throws CreateException, KBTypeException {
		System.out.print("Contructing hypothetical context... ");
		System.out.println(defaultctx);
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmss");
		Date dt = new Date();
		String timestamp = sdf.format(dt); // formats to 09/23/2009 13:53:28.238
				
		String hypothetical_context = ContextImpl.findOrCreate(
				"HYPOTHETICAL_" + timestamp +"_" + target_agent + "_" + defaultctx).toString();
		System.out.println(hypothetical_context);
		interpret(">(genlMt " + hypothetical_context + " " + defaultctx + ")");
		ArrayList<String> hypothetical_facts = interpret("?(knows "
				+ target_agent + " ?FACTS)", assrtctx);
		for (String f : hypothetical_facts) {
			System.out.println(hypothetical_context + ": " + f);
			interpret(">" + f, hypothetical_context);
		}
		return hypothetical_context;
	}
	
	// Get the next action in the execution queue.
		public String getEnqueuedAction() {
			return (execution_queue.poll());
		}
	
	public int execute() {

		// Store the state of the operation. 0 is suceessful.
		// 99 means nothing executed.
		int state = 99;

		// Get the first action from the execution queue.
		String action = getEnqueuedAction();
		
		

		// While there are still actions in the queue...execute them.
		while (action != null) {
			// Let the user know that action is being executed
			// Silenced.
			// System.out.println("Execute " + action);
			
			ArrayList<String> keywords = getKeyWords(action);

			// Boolean to store whether or not the action is to be asserted.
			boolean shouldassert = true;

			// If there is a match...
			if (!keywords.isEmpty()) {

				// If the function expression is say-TMF...
				// SYNTAX: (say-TMF <thing to be said>)
				// Where TMF is short for "TheMARTHAFunction"
				if (keywords.get(0).equals("desires")) {
					shouldassert=false;
					interpret(">" + action, assrtctx);
					state=0;
				} else {
					// No match, don't assert.
					shouldassert = false;
				}
			} else {
				// no match, don't assert.
				shouldassert = false;
			}

			if (shouldassert) {
				// Assert that it has been done.
				interpret(">" + action, assrtctx);
				interpret(">(exactAssertTime " + action
						+ " (IndexicalReferentFn Now-Indexical))", assrtctx);
			}

			// Get the next action
			action = getEnqueuedAction();
		}

		// Return the state of the execution.
		return state;
	}
	
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
					// System.out.println("ACTION("+getUtility(s)+"): "+s);
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
					parent.queueExecution(c);
				}
			} else {
				// System.out.println("THRESHOLD UNMET: " + candidate + " " +
				// highest_value);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Warning: Evaluation failed.");
		}
		
	}
	
	// Queue action chains to be evaluated. Useful to compare a large
		// set of possible actions.
		public int queueEvaluation(LinkedHashSet<String> path) {
			evaluation_queue.add(path);
			// System.out.println("EVAL-QUEUED: " + path);
			return 0;
		}
}
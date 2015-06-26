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

public class MarthaProcess {
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

	private int current_martha_depth;

	private Martha parent;

	private Queue<LinkedHashSet<String>> evaluation_queue = new LinkedList<LinkedHashSet<String>>();

	private int legitimacy_threshold = 30; // Minimum score needed for a plan to

	// be even considered for execution.

	public MarthaProcess(Martha parent_martha, String context, int depth)
			throws SessionConfigurationException,
			SessionCommunicationException, SessionInitializationException,
			CreateException, KBTypeException {

		current_martha_depth = depth;

		parent = parent_martha;

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
		defaultctx = assrtctx; // Store the default context;
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

	public ArrayList<String> getKeyWords(String p) {
		return parent.getKeyWords(p);
	}

	public ArrayList<String> interpret(String input) {
		return parent.interpret(input);
	}

	// Queue action chains to be evaluated. Useful to compare a large
	// set of possible actions.
	public int queueEvaluation(LinkedHashSet<String> path) {
		evaluation_queue.add(path);
		// System.out.println("EVAL-QUEUED: " + path);
		return 0;
	}

	// Do long and deep searches based on random facts within Cyc.
	public void dream() {
		forwardsSearch(generate("(?PRED ?THING ?FACT)"),
				new LinkedHashSet<String>(), 0);
	}

	public void planForGoals() {

		// Query the database to find the user's desires.
		ArrayList<String> goals = interpret("?(desires USER ?DESIRES)");

		// For each desire (which is a goal in this case), evaluate the possible
		// paths to the goal.
		for (String g : goals) {
			ArrayList<LinkedHashSet<String>> paths = backwardsSearch(g,
					new LinkedHashSet<String>(), 0);

			if (!(paths.contains("ERROR") || paths.contains("IMPOSSIBLE"))) {
				for (LinkedHashSet<String> p : paths) {
					System.out.println("Queue: " + p);
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

		System.out.println(">>> BACKWARDS   : " + goal + " " + depth);

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
						newpath.add("IMPOSSIBLE");
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
				+ postconditions + ")");
		return actions;
	}

	// This is an abstraction of a query that gets a list of preconditions that
	// are required by the specified action.
	public ArrayList<String> getPreconditionsForAction(String action) {
		ArrayList<String> preconditions = interpret("?(preconditionFor-Props ?CONDITION "
				+ action + ")");
		return preconditions;
	}

	// This is an abstraction of a query that gets a list of conditions that
	// result from a specific situation (action).
	public ArrayList<String> resultsInConditions(String action) {
		ArrayList<String> postconditions = interpret("?(causes-PropProp "
				+ action + " ?CONDITIONS)");
		return postconditions;
	}

	// This is an abstraction of a query that gets a list of actions that
	// are enabled by a specific situation (condition).
	public ArrayList<String> enablesActions(String precondition) {
		ArrayList<String> preconditions = interpret("?(preconditionFor-Props "
				+ precondition + " ?ACTIONS)");
		return preconditions;
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
					queueExecution(c);
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

	// Clear all the contents of a queue.
	private void purgeQueue(Queue<?> q) {
		q.clear();
	}

	public int queueExecution(String action) {
		// System.out.println("EXEC-QUEUED1 : " + action);
		return parent.queueExecution(action);
	}

	// Deepclone a LinkedHashSet, rather than just copy a reference.
	// This returns an entirely new ArrayList that is disconnected
	// from the given one.
	private LinkedHashSet<String> deepClone(LinkedHashSet<String> original) {

		// For each element in the original, make an identical copy in the new
		// independent LinkedHashSet.
		LinkedHashSet<String> a = new LinkedHashSet<String>(original.size());
		for (String o : original) {
			a.add(new String(o));
		}
		return a;
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

				// System.out.println("<<<"+yield+">>>");
				return yield;

			} else {
				return 1f;
			}
		} catch (Exception e) {
			// System.out.println("STATE VALUE ERROR: "+state);
			// If the query makes no sense, or if there's an error, then default
			// to one (full utility).
			return 1f;
		}
	}

	public Float getBaseUtility(String state) {
		try {
			// Return the value from a query.

			// Lots of work here to try to get a temporal utility value...
			// But I want to get it working internal in CycL, rather than in
			// Java.
			// interpretFromUser("?(exactAssertTime "+state+" ?TIME)");

			ArrayList<String> utility_value = interpret("?(baseUtilityValue USER "
					+ state + " ?VALUE)");
			// System.out.println("[[[["+utility_value+"]]]]");
			return (new Float(utility_value.get(utility_value.size() - 1)));
		} catch (Exception e) {
			// System.out.println("STATE VALUE ERROR: "+state);
			// If the query makes no sense, or if there's an error, then default
			// to zero.
			// System.out.println("ERROR!!!!");
			return 0f;
		}
	}

	// Get the total utility value of an action or condition, the product of
	// baseUtiliityValue and utilityYield.
	public Float getUtility(String state) {
		try {
			return (getBaseUtility(state) * getUtilityYield(state));
			// return 0f;
		} catch (Exception e) {
			// System.out.println("STATE VALUE ERROR: "+state);
			// If the query makes no sense, or if there's an error, then default
			// to zero.
			return 0f;
		}
	}

	// An abstraction for the use of interpret("|"+...), returns the true/false
	// of a statement.
	public Boolean getTruthOf(String statement) {
		return (interpret("|" + statement).get(0).equals("true"));
	}
}

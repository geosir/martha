package moe.george.martha_v01;

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

/* This is a very improper use of extends, but I'd like MarthaProcesses and the Martha Engine
 * to work interchangeable when called as a parent by a MarthaProcess.
 * This is achieved by extending Martha, but overloading most of the functions.
 */
public class MarthaProcess extends Martha {

	// ============== DEFINE VARIABLES ============= //

	// The context in which to make assertions to Cyc
	private String assrtctx = "BaseKB";

	// The default context (to revert to after the context changes)
	private String defaultctx = assrtctx;

	// Parameters for the query interface.
	// MAX-TRANDFORMATION-DEPTH 10 specifies
	// that the inference engine should take 10 recursive steps when looking for
	// query answers. By default, this is zero, which lead to it giving only
	// one-step inferences!
	private String queryparams = ":MAX-TRANSFORMATION-DEPTH 1000";

	// Constants used in the recursive planning search.
	// Maximum forward steps in the plan
	private int max_forwards_depth = 10;
	// Maximum backwards dependencies in the plan
	private int max_backwards_depth = -5;

	// The current depth of this Martha Process in the nested simulation.
	private int depth;

	// The process/engine that spawned this Martha process.
	private Martha parent;

	// The name of the agent who is the subject of this Martha process.
	private String agent;

	// The current search mode of this Martha process
	/*
	 * TODO: This is very awkward, and a potential for conflicts (e.g.,
	 * simultaneous backwards/forwards search). Should integrate into functions
	 * in the future.
	 */
	private String mode = "backwards";

	/*
	 * Queues to place actions that are TO BE executed, and plans to be
	 * evaluated. Allows for potential asynchronous planning development.
	 */
	private Queue<String> execution_queue = new LinkedList<String>();
	private Queue<LinkedHashSet<String>> evaluation_queue = new LinkedList<LinkedHashSet<String>>();

	// Minimum score needed for a plan to be even considered for execution.
	private int legitimacy_threshold = 10;

	// Percentage threshold by which an action is obviously better than another.
	// private double obviously_better_threshold = 1.2;

	// The main constructor.
	public MarthaProcess(Martha parent_martha, String context, String defaultcontext, int init_depth,
			String target_agent) throws SessionConfigurationException, SessionCommunicationException,
					SessionInitializationException, CreateException, KBTypeException {

		// Superclass constructor (Martha Engine)
		super(context);

		// Set the depth from the constructor parameter
		depth = init_depth;

		// Set the parent from the constructor parameter
		parent = parent_martha;

		// Set the subject agent from the constructor parameter
		agent = target_agent;

		// Let everyone know that a new MARTHA is being instantiated
		System.out.println("Creating new MARTHA Process...");

		// Connect to the Cyc server
		System.out.println(
				"Acquiring a cyc server... " + CycSessionManager.getCurrentSession().getServerInfo().getCycServer());

		// Log into the Cyc server as "TheUser"
		System.out.print("Setting cyc user... ");
		CycSessionManager.getCurrentSession().getOptions().setCyclistName("TheUser");
		System.out.println(CycSessionManager.getCurrentSession().getOptions().getCyclistName());

		/*
		 * Set the assertion context to the one specified by the constructor
		 * call Create this context in the Cyc KB.
		 */
		assrtctx = context;
		defaultctx = defaultcontext; // Store the default context;
		ContextImpl.findOrCreate(context);
	}

	/*
	 * Plan Generally is intended to use recent context to create plans to
	 * fulfill long term goals. It is not implemented in this scenario. Most of
	 * what is below pertains to random seeding of facts, and will probably
	 * change with the success of the explore function.
	 */
	/*
	 * public void planGenerally() {
	 * 
	 * // Possible actions that MARTHA can do to start a line of planning
	 * String[] possible_actions = { "(say-TMF ?SOMETHING)",
	 * "(query-TMF ?SOMETHING)", "(contradict-TMF ?SOMETHING)" };
	 * 
	 * // Possible facts that can fed into the actions to generate a valid //
	 * inital action // Unused. // ArrayList<String> feed = new
	 * ArrayList<String>();
	 * 
	 * // Get feed from the last few entries in the logbook.
	 * 
	 * for (int i = 1; i <= 20 && i < logbook.size(); i++) {
	 * feed.addAll(Arrays.asList((logbook.get(logbook.size() -
	 * i)[0].split("\\s|\\(|\\)")))); }
	 * 
	 * 
	 * // Clean up feed, removing all query variables, ERROR, and IMPOSSIBLE //
	 * messages.
	 * 
	 * Iterator<String> it = feed.iterator(); while(it.hasNext()) {
	 * if(it.next().equals("IMPOSSIBLE|ERROR|\\?\\w+")) { it.remove(); } }
	 * 
	 * 
	 * // For each possible action, perform a forwards search with the //
	 * action-fact pair generated above.
	 * 
	 * 
	 * for(String f : feed) { for (String p : possible_actions) {
	 * //System.out.println(f); String seed = "(?PRED " + f + " ?FACT)";
	 * //System.out.println(generate(seed)); forwardsSearch( "(" +
	 * getKeyWords(p).get(0) + " " + generate() + ")", new
	 * LinkedHashSet<String>(), 0); }
	 * 
	 * }
	 * 
	 * 
	 * for (String p : possible_actions) { // System.out.println(f); //
	 * System.out.println(generate(seed)); forwardsSearch( "(" +
	 * getKeyWords(p).get(0) + " " + generate() + ")", new
	 * LinkedHashSet<String>(), 0); } }
	 */

	// Do long and deep searches based on random facts within Cyc.
	/*
	 * public void dream() { forwardsSearch(generate("(?PRED ?THING ?FACT)"),
	 * new LinkedHashSet<String>(), 0); }
	 */

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
	 * Explore with no args; Create and explore seed statements using focus
	 * statements in Martha's focus window.
	 */
	public void explore() {

		// Set the mode for this process to a forwards search.
		mode = "forwards";

		System.out.println(agent + " ===EXPLORE=== " + depth);

		// Some nice debug output.
		System.out.println("FOCUS TICKER: " + focus_ticker);
		interpretFromUser("?(focus ?WHAT ?SOMETHING)");

		// Get the focus statements for this particular tick
		for (String s : interpret("?(focus " + focus_ticker + " ?SOMETHING)")) {
			// Some nice debig output.
			System.out.println(">>> " + s);

			// Explore the given focus statement.
			explore(s);
		}

		// If this isn't the bottom layer...
		if (depth > 0) {

			// Determine the subject agent of the next layer down beforehand.
			String next_agent = "MARTHA";
			if (agent.equals("MARTHA")) {
				next_agent = "USER";
			}

			/*
			 * Execute everything that's queued from the explore session above.
			 * In forwards mode, this results in assertions that will be carried
			 * over to the next simulation by the hypothetical context
			 * constructor.
			 */
			execute();

			// Try-catch to silent fatal errors.
			try {
				// Create a HYPOTHETICAL context to store Martha simulation of
				// mental processes.
				String hypothetical = constructHypotheticalContext(next_agent);

				/*
				 * Create a Martha Process in this HYPOTHETICAL context. It's
				 * sandboxed for things done here will not contaminate the
				 * higher knowledge base.
				 */
				MarthaProcess martha_p = new MarthaProcess(this, hypothetical, defaultctx, depth - 1, next_agent);

				// Recursively explore possbilities in the next layer down.
				martha_p.explore();
			} catch (Exception e) {
				// Catch errors and silence them.
				// DEBUG:
				// System.out.println("ERROR WHILE SPAWNING NEW EXPLORER.");
			}
		}
	}

	// Explore possible intentions, with string as a seed.
	public void explore(String inquiry) {

		// Set the search mode to forwards.
		mode = "forwards";

		try {
			// A nice debug flag.
			System.out.println(agent + " ===EXPLORE=== " + depth);

			// The forwads search itself! This is what does the simulation at
			// each level.
			forwardsSearch(inquiry, new LinkedHashSet<String>(), 0);

			// Evaluate the plans produced by the forwards search.
			evaluatePlans();

		} catch (Exception e) {
			// Silence errors.
		}
	}

	// Plan direct paths to explicitly given goals.
	public void planForGoals() {
		// Set the mode to a backwards search.
		mode = "backwards";

		System.out.println("USER believes:");
		interpretFromUser("?(beliefs USER ?WHAT)");
		System.out.println("MARTHA believes:");
		interpretFromUser("?(beliefs MARTHA ?WHAT)");

		try {
			// A nice debug marker
			System.out.println(agent + " ============== " + depth);

			// If this isn't the bottom layer...
			if (depth > 0) {

				// Determine the subject agent of the next layer before hand.
				String next_agent = "MARTHA";
				if (agent.equals("MARTHA")) {
					next_agent = "USER";
				}

				// Construct a HYPOTHETICAL context to run the simulations of
				// the next layers.
				String hypothetical = constructHypotheticalContext(next_agent);

				// Create a MarthaProcess in this hypothetical context to run
				// the simulations.
				MarthaProcess martha_p = new MarthaProcess(this, hypothetical, defaultctx, depth - 1, next_agent);

				/*
				 * Recursively run goal planning simulations. Placing this here
				 * causes the nested searches to wait until the last search
				 * returns results before acting upon those results.
				 */
				martha_p.planForGoals();

				// Execute the queued results from the planning.
				execute();
			}

		} catch (Exception e) {
			// Silence errors.
		}

		// A nice marker for debug.
		System.out.println(agent + " ============== " + depth);

		//
		// Query the database to find the user's desires.
		ArrayList<String> goals = interpret("?(desires " + agent + " ?DESIRES)", assrtctx);

		System.out.println("GOALS: " + goals);

		// THE ACTUAL SEARCHING ITSELF!
		// For each desire (which is a goal in this case), evaluate the possible
		// paths to the goal.
		for (String g : goals) {

			// An ArrayList to store paths to the goals found by the backwards
			// search.
			ArrayList<LinkedHashSet<String>> paths = backwardsSearch(g, new LinkedHashSet<String>(), 0);

			// Unless the path is bad, queue each found path for evaluation.
			for (LinkedHashSet<String> p : paths) {
				if (!(paths.contains("ERROR|IMPOSSIBLE"))) {
					queueEvaluation(p);
				}
			}
		}

		// Evaluate the plans. In backwards mode, these get queued for execution
		// in the parent.
		evaluatePlans();
	}

	/*
	 * public void emulateUser() { user.planGenerallyFromKnows(); }
	 */

	// Generate feed facts for initial actions.
	public String generate() {
		String[] feed = { "ChicagoBotanicGardens", "FiveDollarSteakSandwich" };
		return (generate("(?PRED " + feed[new Random().nextInt(feed.length)] + " ?FACT)"));
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
	public ArrayList<LinkedHashSet<String>> backwardsSearch(String goal, LinkedHashSet<String> path, int search_depth) {

		System.out.println(">>> BACKWARDS   : " + goal + " " + depth);

		// An ArrayList to store the current cumulative chain of actions found
		// thus far.
		// This is deepcloned from the given path in order to start a new branch
		// in the recursion tree.
		ArrayList<LinkedHashSet<String>> newpaths = new ArrayList<LinkedHashSet<String>>();
		LinkedHashSet<String> newpath = deepClone(path);

		// Add the current goal to the plan.
		newpath.add(goal);

		// Add the current goal to the path.
		// newpath.add(goal);

		// Get actions leading to the goal, and get preconditions for the goal.
		ArrayList<String> actions = getActionsForPostconditions(goal);
		ArrayList<String> preconditions = getPreconditionsForAction(goal);

		// If we aren't exceeding the max dependency depth...
		if (search_depth >= max_backwards_depth) {

			// Find and add preconditions of this state.
			for (String p : preconditions) {
				newpath.add(p);
			}
			for (String p : preconditions) {

				// If the precondition isn't already fulfilled...
				if (!getTruthOf(p)) {

					// Do a backwards search to find actions to fulfill
					// those preconditions.
					ArrayList<LinkedHashSet<String>> results = backwardsSearch(p, newpath, search_depth - 1);

					// If we've reached a root on a precondition, we've reach an
					// unfulfillable precondition.
					if (results.get(0).contains("ROOT" + (search_depth - 1))) {

						// Tag this agent with "desires" to say it needs to be
						// fulfilled in the next layer up.
						String newagent = "MARTHA";
						if (agent.equals("MARTHA")) {
							newagent = "USER";
						}
						newpath.add("(desires " + newagent + " " + p + ")");
						newpaths.add(newpath);
					} else {
						// In all other cases, just add all preconditions to the
						// plan.
						newpaths.addAll(results);
					}
				}
			}

			// For each action found, start a new tree and find dependencies for
			// those.

			// If any actions have already happened, you can just use that and
			// trim the tree.
			String trueaction = "";
			for (String a : actions) {
				if (getTruthOf(a)) {
					trueaction = a;
				}
			}

			// So, if no actions have already happened,
			if (trueaction.isEmpty()) {
				// Start a new search with each of the actions.

				for (String a : actions) {

					ArrayList<LinkedHashSet<String>> results = backwardsSearch(a, newpath, search_depth - 1);
					newpaths.addAll(results);

					/*
					 * LinkedHashSet<String> temp = deepClone(newpath);
					 * temp.add("(say-TMF " + assumption + ")");
					 * 
					 * ArrayList<LinkedHashSet<String>> results =
					 * backwardsSearch( a, temp, search_depth - 1);
					 * newpaths.addAll(results);
					 */
				}
			} else {
				// Otherwise, just search using the action that DID happen.
				ArrayList<LinkedHashSet<String>> results = backwardsSearch(trueaction, newpath, search_depth - 1);
				newpaths.addAll(results);
			}

			// If there are no more actions and no more preconditions,
			// We've reached a root.
			if (actions.isEmpty() && preconditions.isEmpty()) {
				newpath.add("ROOT" + search_depth);
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
				// System.out.println(">>>>>> IMPOSSIBLE - REACHED MAX DEPTH
				// <<<<<<");
				newpaths.add(newpath);
			}
		}

		// Return results.
		System.out.println(newpaths);
		return newpaths;
	}

	// Forwards search algorithm. Given a goal, this looks for what actions can
	// stem from it into the future.
	public void forwardsSearch(String goal, LinkedHashSet<String> path, int search_depth) {
		System.out.println(">>> FORWARDS    : " + goal + " " + search_depth);

		// An ArrayList to store the current cumulative chain of actions found
		// thus far.
		// This is deepcloned from the given path in order to start a new branch
		// in the recursion tree.
		LinkedHashSet<String> newpath = deepClone(path);
		// Add the current goal to the chain.
		newpath.add(goal);

		// If we haven't exceeded the max forward search length...
		if (search_depth <= max_forwards_depth) {
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
				forwardsSearch(a, newpath, search_depth + 1);
			}

			// For each condition, do a forwards search, e.g. for actions that
			// they enable.
			for (String c : conditions) {
				forwardsSearch(c, newpath, search_depth + 1);
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

	public void mixedSearch(String goal, LinkedHashSet<String> path, int search_depth) {
		System.out.println(">>> FORWARDS    : " + goal + " " + search_depth);

		// An ArrayList to store the current cumulative chain of actions found
		// thus far.
		// This is deepcloned from the given path in order to start a new branch
		// in the recursion tree.
		LinkedHashSet<String> newpath = deepClone(path);
		// Add the current goal to the chain.
		newpath.add(goal);

		// If we haven't exceeded the max forward search length...
		if (search_depth <= max_forwards_depth) {
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
				for (LinkedHashSet<String> possible : backwardsSearch(a, newpath, search_depth + 1)) {
					forwardsSearch(a, possible, search_depth + 1);
				}
			}

			// For each condition, do a forwards search, e.g. for actions that
			// they enable.
			for (String c : conditions) {
				forwardsSearch(c, newpath, search_depth + 1);
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
		ArrayList<String> actions = interpret("?(causes-PropProp ?ACTIONS " + postconditions + ")", assrtctx);
		return actions;
	}

	// This is an abstraction of a query that gets a list of preconditions that
	// are required by the specified action.
	public ArrayList<String> getPreconditionsForAction(String action) {
		ArrayList<String> preconditions = interpret("?(preconditionFor-Props ?CONDITION " + action + ")", assrtctx);
		return preconditions;
	}

	// This is an abstraction of a query that gets a list of conditions that
	// result from a specific situation (action).
	public ArrayList<String> resultsInConditions(String action) {
		ArrayList<String> postconditions = interpret("?(causes-PropProp " + action + " ?CONDITIONS)", assrtctx);
		return postconditions;
	}

	// This is an abstraction of a query that gets a list of actions that
	// are enabled by a specific situation (condition).
	public ArrayList<String> enablesActions(String precondition) {
		ArrayList<String> preconditions = interpret("?(preconditionFor-Props " + precondition + " ?ACTIONS)", assrtctx);
		return preconditions;
	}

	// Add an action to the execution queue.
	public int queueExecution(String action) {
		System.out.println("EXEC-QUEUED(" + depth + "): " + action);
		execution_queue.add(action);
		return 0;
	}

	/*
	 * Take facts in the current context and create a HYPOTHETICAL simulation
	 * context. It's hypothetical because it is a SUBCONTEXT of the default
	 * context, but changes to it cannot affect the main Martha context or any
	 * other sibling contexts. Facts in the context are derived from inherited
	 * facts from the default context, and facts that are passed on through
	 * (beliefs <subject agent> ...) or (carryover ...)
	 */
	public String constructHypotheticalContext(String target_agent) throws CreateException, KBTypeException {

		// Let everyone know we're making a new context.
		System.out.print("Contructing hypothetical context... ");
		// Debug output
		System.out.println(defaultctx);

		// Get the date; the context is named based on the time and date.
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmss");
		Date dt = new Date();
		String timestamp = sdf.format(dt); // formats to a string of numbers.

		// Create the hypothetical context with a long unique name.
		String hypothetical_context = ContextImpl
				.findOrCreate("HYPOTHETICAL_" + timestamp + "_" + target_agent + "_" + assrtctx).toString();

		// Debug output
		System.out.println(hypothetical_context);

		// Assert that this hypothetical context inherits facts from the
		// highest, default context.
		interpret(">(genlMt " + hypothetical_context + " " + defaultctx + ")");

		/*
		 * Query all statements that are in the form (beliefs <next agent> ...)
		 * and (carryover ...). These are to be carried over to the hypothetical
		 * context.
		 */
		ArrayList<String> ops = interpret("?(beliefs " + target_agent + " ?FACTS)", assrtctx);
		ArrayList<String> hypothetical_facts = new ArrayList<String>();
		hypothetical_facts.addAll(ops);
		for (String o : ops) {
			hypothetical_facts.add("(beliefs " + target_agent + " " + o + ")");
		}

		hypothetical_facts.addAll(interpret("?(carryover ?CARRYOVER)"));
		System.out.println("TT: " + target_agent);

		/*
		 * Assert the (...) part as facts above in the hypothetical context
		 * because those are what <next agent> believes.
		 */
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

	/*
	 * Execute, analogous to the execute function in the Martha Engine. However,
	 * the functions here are a little different and pertain mostly to what gets
	 * carried over.
	 */

	public int execute() {

		// Store the state of the operation. 0 is suceessful.
		// 99 means nothing executed.
		int state = 99;
		int new_focus = focus_ticker;

		// Get the first action from the execution queue.
		String action = getEnqueuedAction();

		// While there are still actions in the queue...execute them.
		while (action != null) {
			// Let the user know that action is being executed
			// Silenced.
			System.out.println("Execute " + action);

			ArrayList<String> keywords = getKeyWords(action);

			// Boolean to store whether or not the action is to be asserted.
			boolean shouldassert = true;

			// If there is a match...
			if (!keywords.isEmpty()) {

				// If the function expression is say-TMF...
				// SYNTAX: (say-TMF <thing to be said>)
				// Where TMF is short for "TheMARTHAFunction"
				if (keywords.get(0).equals("desires")) {
					shouldassert = false;
					interpret(">" + action, assrtctx);
					state = 0;
				}
				/*
				 * Beliefs get carried over with a special focus wrapper to call
				 * attention to this. This may or may not be the best way to
				 * approach this
				 */
				else if (keywords.get(0).equals("beliefs")) {
					new_focus = focus_ticker + 1;
					if (action.contains("(beliefs USER")) {
						action = action.replace("(beliefs USER ", "(beliefs USER (focus " + new_focus + " ");
						action = action + ")";
					} else if (action.contains("(beliefs MARTHA")) {
						action = action.replace("(beliefs MARTHA ", "(beliefs MARTHA (focus " + new_focus + " ");
						action = action + ")";
						shouldassert = false;
					}
					System.out.println("Exec debug: " + action);
					interpret(">" + action, assrtctx);
					state = 0;
				}
				/*
				 * "sowhat" queries are carried over with the why wrapper
				 * removed and with a focus wrapper added.
				 */
				else if (keywords.get(0).equals("sowhat")) {
					new_focus = focus_ticker + 1;
					action = action.replace("(sowhat ", "(carryover (focus " + new_focus + " ");
					action = action + ")";
					shouldassert = false;
					System.out.println("Exec debug: " + action);
					interpret(">" + action, assrtctx);
					state = 0;
				} else if (keywords.get(0).equals("query-TMF")) {
					LinkedHashSet<String> query = new LinkedHashSet<String>();
					query.add(action);
					MainProcess.martha.queueEvaluation(query);
					shouldassert = false;
					state = 2;
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
				interpret(">(exactAssertTime " + action + " (IndexicalReferentFn Now-Indexical))", assrtctx);
				System.out.println(action);
			}

			// Get the next action
			action = getEnqueuedAction();
		}

		focus_ticker = new_focus;

		// Return the state of the execution.
		return state;
	}

	/*
	 * Evaluate plans much like the Martha Engine does. Here, however, the
	 * legitimacy threshold is significantly lower because these plans are often
	 * partial steps, restricted to a single layer.
	 */
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

				/*
				 * If something is, hypothetically, so bad that it's worse than
				 * the legitimacy threshold, it is worth exploring the
				 * possibilities to avoid the bad thing. Note that the main
				 * MARTHA engine doesn't execute these because that would make
				 * MARTHA inclined to do bad things too; it merely explores them
				 * and see the results.
				 * 
				 * For that reason, it is only available in FORWARDS mode.
				 */
				if (mode.equals("forwards") && current_value <= -legitimacy_threshold) {
					for (String c : a) {
						queueExecution("(sowhat " + c + ")");
					}
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
					/*
					 * This part gets interesting. If the mode is backwards
					 * (i.e., we are looking for a path to a goal, the plan will
					 * be queued for execution in the PARENT process; we go
					 * backwards! If the mode is forwards, we queue in the
					 * present process for execution, which will carry over the
					 * appropriate facts to the CHILD process.
					 */
					System.out.println("Q+EXEC: " + c);
					if (mode.equals("backwards")) {
						parent.queueExecution(c);
					} else {
						queueExecution(c);
					}

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
	/*
	 * Once again, this function is a little different than in the Martha
	 * Engine. If the mode is forwards, not only does the evaluation get queued
	 * in the present process, but it also gets queued in the main process, so
	 * that it can take a look at chains at different stages of completeness.
	 */
	public int queueEvaluation(LinkedHashSet<String> path) {
		evaluation_queue.add(path);
		if (mode == "forwards") {
			MainProcess.martha.queueEvaluation(path);
		}
		System.out.println("EVAL-QUEUED: " + path);
		return 0;
	}
}
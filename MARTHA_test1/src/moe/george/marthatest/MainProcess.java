//MARTHA Test
//Mental-state Aware Real-time THinking Assistant
//===============================================
//This is just a test that I did to get to learn the 
//components of the Cyc API. Here, I've done assertions
//and queries into the Knowledge Base through the API.
//NOTE: Comments added 6/20/15. Last modifications to code
//were early week of 6/15.

//The package for this MARTHA test
package moe.george.marthatest;

//Import necessary libraries
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.cyc.base.CycConnectionException;
import com.cyc.kb.Sentence;
import com.cyc.kb.Variable;
import com.cyc.kb.client.BinaryPredicateImpl;
import com.cyc.kb.client.Constants;
import com.cyc.kb.client.KBTermImpl;
import com.cyc.kb.client.SentenceImpl;
import com.cyc.kb.client.VariableImpl;
import com.cyc.kb.exception.KBApiException;
import com.cyc.query.KBInferenceResultSet;
import com.cyc.query.Query;
import com.cyc.query.exception.QueryConstructionException;
import com.cyc.session.CycSessionManager;
import com.cyc.session.SessionCommunicationException;
import com.cyc.session.SessionConfigurationException;
import com.cyc.session.SessionInitializationException;

public class MainProcess {

	public static void main(String[] args)
			throws SessionConfigurationException,
			SessionCommunicationException, SessionInitializationException,
			CycConnectionException, KBApiException {

		// Print cool MARTHA stamp.
		System.out.println("===============================================");
		System.out.println("Initializing MARTHA");
		System.out.println("Mental-state Aware Real-time THinking Assistant");
		System.out.println("Copyright (c) George Moe 2015");
		System.out.println("===============================================\n");

		// Access Cyc through the api for the first time, which prompts the Cyc
		// connection dialog to appear.
		System.out.println("Acquiring a cyc server... "
				+ CycSessionManager.getCurrentSession().getServerInfo()
						.getCycServer());

		// Log into Cyc as "CycAdministrator"
		System.out.print("Setting cyc user... ");
		CycSessionManager.getCurrentSession().getOptions()
				.setCyclistName("CycAdministrator");
		System.out.println(CycSessionManager.getCurrentSession().getOptions()
				.getCyclistName());

		// Everything is ready, print welcome statement.
		System.out.println("\n\nWelcome to MARTHA.\n\n");

		//Looping prompt - this is what makes the prompt appear continuously.
		while (true) {
			
			// Get query input from user
			String input = getInput("What things are a: ");

			// Construct query sentence
			BinaryPredicateImpl isa = BinaryPredicateImpl.get("isa");	//Get the "isa" predicate and store its handle in a variable
			Sentence queries;	//Declare a new sentence
			Variable queryVar = new VariableImpl("?THING"); //Define a new variable to be used in the query
			
			//Lots of try and catch blocks used here in order to not have the program die due to poor user input.
			try {
				queries = new SentenceImpl(isa, queryVar, KBTermImpl.get(input));	//Query: "(isa ?THING <user input>)"
																					//Find out what things fall under the collection specified by the user.
			} catch (Exception e) {
				//The the user input isn't a collection, just let the user know and continue.
				System.out.println("Bad name!");
				continue;
			}
			
			//Make a second copy of the sentence. This actually isn't necessary, I don't know why this is here.
			Sentence querySentence = queries;

			// Test Query
			Query q;
			try {
				//Perform actual query into the Cyc Knowledge Base
				q = new Query(querySentence, Constants.inferencePSCMt());
			} catch (QueryConstructionException e) {
				//Just catch bad queries, no big deal.
				// e.printStackTrace();
				System.out.println("Bad query.");
				continue;
			}
			
			//INFO output of what the query ended up being.
			System.out.println("Querying \"" + q.getQuerySentence() + "\"...");

			//Get the results that the Cyc inference engine fond for our variable "?THING".
			KBInferenceResultSet results = q.getResultSet();		//Get result set
			while (results.next()) {								//While there are still results...
				System.out.println(results.getKBObject("?THING"));	//Print the result corresponding to "?THING"
			}

			//Done
			System.out.println("Done.\n\n");
			
			//Close query so that Cyc can free up those resources.
			q.close();

		}
	}
	
	//Since console input from the traditional System.console.readline() doesn't work in Eclispe,
	//I'm using this workaround instead.
	private static String getInput(String prompt) {
		//Create a new buffer reader attached to the System input buffer
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		//Print the prompt to let the user know to give input
		System.out.print(prompt);
		String input = "";
		
		//Read a line from the buffer, which is delimited when the user hits ENTER.
		//Works just like System.console.readline().
		try {
			input = br.readLine();
		} catch (IOException e) {
			//Catch I/O errors without dying.
			e.printStackTrace();
		}

		//Return fetched input.
		return input;
	}

}

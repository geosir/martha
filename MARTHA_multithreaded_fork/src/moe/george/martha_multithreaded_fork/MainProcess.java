package moe.george.martha_multithreaded_fork;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.cyc.base.CycConnectionException;
import com.cyc.kb.exception.KBApiException;
import com.cyc.session.SessionCommunicationException;
import com.cyc.session.SessionConfigurationException;
import com.cyc.session.SessionInitializationException;


public class MainProcess {
	
	final static String init_file_path = "initfile.martha";
	final static String context = "BaseKB";
	public static Martha martha;
	
	//ArrayList<Object> history = new ArrayList<Object>();
	
	public static void main(String[] args) throws SessionConfigurationException, SessionCommunicationException, SessionInitializationException, CycConnectionException, KBApiException {

		System.out.println("Initializing MARTHA...");
		 martha = new Martha(context);
		
		try {
			martha.initFromFile(init_file_path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Error initializing from file.");
		}
		
		System.out.println("\n\n===============================================");
		System.out.println("Welcome to MARTHA");
		System.out.println("Mental-state Aware Real-time THinking Assistant");
		System.out.println("Copyright (c) George Moe 2015");
		System.out.println("===============================================\n\n");
		
		MarthaConsciousness mc = new MarthaConsciousness(martha);
		System.out.println("Starting new MARTHA Consciousness...");
		mc.start();
		
		while(true)
		{
			String input = getInput("MARTHA: ");
			martha.interpretFromUser(input);
			//martha.planForGoals();
		}
	    
	}
	
	private static String getInput(String prompt)
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(prompt);
        String input = "";
		try {
			input = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return input;
	}
}

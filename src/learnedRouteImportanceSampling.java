//==============================================================================
// learnedRouteImportanceSampling: an IS experiment for CTMC models
//    Concept: A CTMC is specified, and a target state property 
//             During stochastic simulation, if the target is reached, then all
//             states from the successful path are added to a catalog.
//             During path generation, at each transition, if the next state 
//             is in the catalog, then the transition rate is enhanced by a 
//             constant multipler M. Reverse transitions (back to the immediate last
//             state) are not enhanced.
//
//             The weighted path probability is computed
//             incrementally with each transition. A path is truncated if it exceeds
//             TMAX transitions without reaching either the target or a catalog state.
//
// Chris Winstead, Utah State University
//
// Based on PRISM API demo "SimulateModel". 

import java.io.File;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;

import parser.Values;
import parser.ast.Expression;
import parser.ast.ModulesFile;
import prism.Prism;
import simulator.Path;
import simulator.PathFull;
import prism.PrismDevNullLog;
import prism.PrismException;
import prism.PrismLog;
import prism.PrismPrintStreamLog;
import simulator.SimulatorEngine;

/**
 * An example class demonstrating how to control PRISM programmatically,
 * through the functions exposed by the class prism.Prism.
 * 
 * This shows how to use PRISM's discrete-even simulator to generate some
 * sample paths through a model loaded in from a file.
 * 
 * See the README for how to link this to PRISM.
*/
public class learnedRouteImportanceSampling
{
    public static double M = 2; // transition multiplier
    public static int TMAX = 1000000; // maximum transitions before truncating
    public static ArrayList<Integer> catalog = new ArrayList<Integer>();
    public static String catalogFileName;
    public static String modelFileName;
    public static PrismLog mainLog;
    public static Prism prism;
    public static SimulatorEngine sim;

    public static void main(String[] args)
    {
	//		new learnedRouteImportanceSampling().run();
	System.out.println("Running learnedRouteImportanceSampling");
	System.out.println(args.length + " arguments");

	for (int i=0; i<args.length; i++)
	    System.out.println(args[i]);

	if (args.length > 0) 
	    modelFileName = args[0];
	else
	    modelFileName = new String("models/abstract/3_states/3_state.pm");
	if (args.length > 1) 
	    modelFileName = args[1];
	else
	    catalogFileName = new String("models/abstract/3_states/3_state.is");

	if (args.length > 2) 
	    TMAX = Integer.parseInt(args[2]);
	else
	    TMAX=3;

	loadModel();
	loadCatalog();
	printCatalog();
	run();
    }

    public static void loadModel() 
    {
	try {
	    System.out.println("Loading PRISM model from " + modelFileName);

	    // Create a log for PRISM output (hidden or stdout)
	    mainLog = new PrismDevNullLog();
	    
	    // Initialise PRISM engine 
	    prism = new Prism(learnedRouteImportanceSampling.mainLog);
	    prism.initialise();
	    
	    // Parse and load a PRISM model from a file
	    ModulesFile modulesFile = prism.parseModelFile(new File(modelFileName));
	    prism.loadPRISMModel(modulesFile);
	    
	    // Load the model into the simulator
	    prism.loadModelIntoSimulator();
	    sim = prism.getSimulator();
	}
	catch (FileNotFoundException e) {
	    System.out.println("File Error: " + e.getMessage());
	    System.exit(1);
	} catch (PrismException e) {
	    System.out.println("PRISM Error: " + e.getMessage());
	    System.exit(1);
	}
    }

    public static void printCatalog()
    {
	for (int i=0; i<catalog.size(); i++) {
	    System.out.println(catalog.get(i));
	}
    }

    public static void loadCatalog()
    {
	try {
	    FileReader fr = new FileReader(catalogFileName);
	    BufferedReader br=new BufferedReader(fr);
	    String x;
			
	    x=br.readLine(); 
			

	    String[] states=x.split(",");
	    for (int i=0; i<states.length; i++) {
		catalog.add(Integer.parseInt(states[i]));
	    }
	    
	}
	catch (FileNotFoundException e) {
	    System.out.println("Error: " + e.getMessage());
	    System.exit(1);
	} 
	catch (IOException e) {
	    System.out.println("Error: " + e.getMessage());
	    System.exit(1);
	}
    }
    public static void run()
	{
	    try {

		sim.createNewPath();
		sim.initialisePath(null);

		System.out.println("Generating Path:");
		System.out.println("Initial state " + sim.getCurrentState());

		int index;
		double path_probability = 1.0;
		double total_rate = 0.0;

		
		for (int tdx=0; tdx < TMAX; tdx++) {
		    index=-1;
		    total_rate = 0.0;
		    for (int idx=0; idx<sim.getNumTransitions(); idx++) {		
			total_rate += sim.getTransitionProbability(idx);
		    }
		
		    if (sim.automaticTransition()) {
			PathFull pf = sim.getPathFull();
			System.out.println("Action " + pf.getPreviousAction() + " to state " + sim.getCurrentState() + " with prob " + pf.getPreviousProbability()/total_rate);
		
			path_probability *= pf.getPreviousProbability()/total_rate;
		    }
		    else
			break;
		
		    
		}
		
		System.out.println("Total path:");
		System.out.println(sim.getPath());
		
		
		System.out.printf("Path probability: %e\n", path_probability);
		

		// Close down PRISM
		prism.closeDown();

	    } 
	    catch (PrismException e) {
		System.out.println("Error: " + e.getMessage());
		System.exit(1);
	    }
	
	}
}

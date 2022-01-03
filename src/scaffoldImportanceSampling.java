//==============================================================================
//  scaffoldImportanceSampling: an IS experiment for CTMC models
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
import simulator.RandomNumberGenerator;
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
public class scaffoldImportanceSampling
{
    public static double             M = 2; // transition multiplier
    public static int                TMAX = 1000000; // maximum transitions before truncating
    public static ArrayList<Integer> catalog = new ArrayList<Integer>();

    public static String             catalogFileName;
    public static String             modelFileName;

    public static PrismLog           mainLog;
    public static Prism              prism;
    public static SimulatorEngine    sim;

    private static RandomNumberGenerator rng = new RandomNumberGenerator();


    public static void main(String[] args)
    {
	//		new scaffoldImportanceSampling().run();
	System.out.println("Running scaffoldImportanceSampling");
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
	    prism = new Prism(scaffoldImportanceSampling.mainLog);
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

		double path_probability = 1.0;
		double modified_probability = 1.0;
		double total_rate = 0.0;
		double modified_total_rate = 0.0;

		
		// Simulate a path step-by-step:
		for (int tdx=0; tdx < TMAX; tdx++) {
		    // Initialize variables:
		    total_rate = 0.0; 
		    modified_total_rate = 0.0;

		    // Loop through the possible transitions from the current state:
		    Integer numTransitions  = sim.getNumTransitions();
		    ArrayList transitionRates = new ArrayList<Double>(numTransitions);
		    ArrayList nativeRates = new ArrayList<Double>(numTransitions);

		    if (numTransitions > 0) {
			System.out.println(numTransitions + " transitions:");

			for (int idx=0; idx<numTransitions; idx++) { 

			    // Get target state of the indexed transition:
			    Integer transitionTarget = Integer.parseInt(sim.computeTransitionTarget(idx).toStringNoParentheses());

			    // Report if the target is in the catalog:
			    String inCatalog;

			    transitionRates.add(sim.getTransitionProbability(idx));
			    nativeRates.add(sim.getTransitionProbability(idx));

			    // Accumulate total rate:
			    total_rate += (double) sim.getTransitionProbability(idx);

			    if (catalog.indexOf(transitionTarget) > -1) {
				inCatalog = new String("(in catalog)");
				transitionRates.set(idx, M*(double)transitionRates.get(idx));
			    }
			    else {
				inCatalog = new String("(NOT in catalog)");
			    }

			    modified_total_rate += (double) transitionRates.get(idx);


			    // Print info string:
			    System.out.printf("  -> %d %s with probability %e\n", transitionTarget, inCatalog, sim.getTransitionProbability(idx));

			}
		
			// Save current state then run a transition:
			Integer currentState = Integer.parseInt(sim.getCurrentState().toStringNoParentheses());

			// |---------|----|------|-x--|-------|-----|  
			// 0                                        Sum(Rate)


			double tot = 0.0;
			int offset = 0;

			double x = rng.randomUnifDouble(modified_total_rate);

			for (offset = 0; x >= tot && offset < numTransitions; offset++) {
			    tot += (double) transitionRates.get(offset);
			}
			offset -= 1;

			// -----------------------
			// executeTimedTransition is private; changed to "manualTransition" below. Much simpler.
			//------------------------
			//		    int i = sim.getChoiceIndexOfTransition(offset);
			//		    sim.executeTimedTransition(i, offset, rng.randomExpDouble(total_rate), -1);   // <-- execute a transition

			if ((offset < numTransitions)&&(offset >= 0)) {
			
			    // Get the new state:
			    Integer transitionTarget = Integer.parseInt(sim.getCurrentState().toStringNoParentheses());
			    
			    // If the target is in the catalog but the preceding state is not, add the preceding state into the catalog:
			    if ((catalog.indexOf(transitionTarget) > -1) && (catalog.indexOf(currentState) == -1)) 
				catalog.add(currentState);
			    
			    
			    // Print transition summary:
			    //PathFull pf = sim.getPathFull();
			    //System.out.println("Action " + pf.getPreviousAction() + " to state " + sim.getCurrentState() + " with prob " + pf.getPreviousProbability()/total_rate);
			    
			    // Accumulate path probability:
			    double p_transition = (double)nativeRates.get(offset)/total_rate; 
			    path_probability *= p_transition;
			    double p_modified = (double)transitionRates.get(offset)/modified_total_rate;
			    modified_probability *= p_modified;
			    
			    System.out.println("  transition probability: " + p_transition + ", modified: " + p_modified);
			

			    sim.manualTransition(offset);
			    System.out.println("Next state " + sim.getCurrentState());
			}
			
		    }
		    else
			break;

		    /* ABSTRACT generalization:
		       1. initialize the path, then step time
		       2. loop over all transitions from current state, detect catalog inclusion 
		       -> check conditions to add state into the catalog
		       3. apply rate enhancements (THIS NEEDS A CUSTOM TRANSITION)
		       4. (?) check conditions to include in catalog (?)
		       5. check stopping conditions
		    */
		    
		}
		
		System.out.println("Total path:");
		System.out.println(sim.getPath());
		
		
		System.out.printf("Path probability: %e\n", path_probability);
		System.out.printf("Modified path probability: %e\n", modified_probability);
		

		// Close down PRISM
		prism.closeDown();

	    } 
	    catch (PrismException e) {
		System.out.println("Error: " + e.getMessage());
		System.exit(1);
	    }
	
	}
}

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
    public static double             M       = 15; // transition multiplier
    public static int                TMAX    = 10; // maximum transitions before truncating
    public static int                Nruns   = 10000; // Number of stochastic runs
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
	    modelFileName = new String("models/abstract/5_states/5_states.pm");
	if (args.length > 1) 
	    modelFileName = args[1];
	else
	    catalogFileName = new String("models/abstract/5_states/5_states.is");

	if (args.length > 2) 
	    TMAX = Integer.parseInt(args[2]);
	
	loadModel();
	loadCatalog();
	printCatalog();

	double sum       = 0.0;
	double squareSum = 0.0;

	ArrayList samples = new ArrayList<Double>(Nruns);
	for (int n=0; n<Nruns; n++) {
	    double sample = run();
	    sum += sample;

	    samples.add(sample);
	    //System.out.println(sim.getPath());
	    //System.out.println("=================");
	}	    
	
	prism.closeDown();

	double mean = sum/(double)Nruns;

	for (int n=0; n<Nruns; n++) {
	    double squareTerm = (double)samples.get(n) - mean;
	    squareSum += squareTerm*squareTerm; 
	}
	double variance = squareSum/((double)Nruns*((double)Nruns-1));
	System.out.println("Probability to reach final state: " + mean + ", Variance " + variance);
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

    public static void addToCatalog(Integer s)
    {
	if (catalog.indexOf(s) == -1)
	    catalog.add(s);
    }

    public static Boolean inCatalog(Integer s)
    {
	if (catalog.indexOf(s) == -1)
	    return false;
	else
	    return true;
    }


    public static Boolean satisfyCatalogCondition(Integer currentState, Integer targetState)
    {
	if (inCatalog(targetState) && !inCatalog(currentState))
	    return true;
	else
	    return false;
    }


    public static double run()
    {
	try {
	    sim.createNewPath();
	    sim.initialisePath(null);

	    double path_probability     = 1.0;
	    double modified_probability = 1.0;
	    double total_rate           = 0.0;
	    double modified_total_rate  = 0.0;

	    ArrayList visitedStates = new ArrayList<Double>(TMAX);

	    // Simulate a path step-by-step:
	    for (int tdx=0; tdx < TMAX; tdx++) {
		// Initialize variables:
		total_rate          = 0.0; 
		modified_total_rate = 0.0;

		// Loop through the possible transitions from the current state:
		Integer   numTransitions  = sim.getNumTransitions();
		ArrayList transitionRates = new ArrayList<Double>(numTransitions);
		ArrayList nativeRates     = new ArrayList<Double>(numTransitions);
		Integer   currentState    = Integer.parseInt(sim.getCurrentState().toStringNoParentheses());
		
		if (visitedStates.indexOf(currentState) == -1)
		    visitedStates.add(currentState);

		if (numTransitions > 0) {			
		    //++++++++++++++++++++++++++++++++++++++++++++++++++++
		    // Adjust Transition Rates 			
		    //++++++++++++++++++++++++++++++++++++++++++++++++++++
		    for (int idx=0; idx<numTransitions; idx++) { 
			// Get target state of the indexed transition:
			Integer transitionTarget = Integer.parseInt(sim.computeTransitionTarget(idx).toStringNoParentheses());
			if (satisfyCatalogCondition(currentState,transitionTarget))
			    addToCatalog(currentState);

			transitionRates.add(sim.getTransitionProbability(idx));
			nativeRates.add(sim.getTransitionProbability(idx));
			total_rate += (double) sim.getTransitionProbability(idx);

			if (inCatalog(transitionTarget) && (visitedStates.indexOf(transitionTarget) == -1)) { 
			    transitionRates.set(idx, M*(double)transitionRates.get(idx));
			}
			modified_total_rate += (double) transitionRates.get(idx);
		    }
		    //++++++++++++++++++++++++++++++++++++++++++++++++++++

		
		    ////////////////////////////////////////////////////
		    // Execute the transition:
		    ////////////////////////////////////////////////////
		    double x = rng.randomUnifDouble(modified_total_rate);

		    double tot    = 0.0;
		    int    offset = 0;
		    for (offset = 0; x >= tot && offset < numTransitions; offset++) {
			tot += (double) transitionRates.get(offset);
		    }
		    offset--;
			
		    // Get the new state:
		    //		    Integer transitionTarget = Integer.parseInt(sim.getCurrentState().toStringNoParentheses());
			 			    
		    // Accumulate path probability:
		    double p_transition   = (double)nativeRates.get(offset)/total_rate; 
		    path_probability     *= p_transition;
		    double p_modified     = (double)transitionRates.get(offset)/modified_total_rate;
		    modified_probability *= p_modified;
			    
		    sim.manualTransition(offset);
		    Integer transitionTarget = Integer.parseInt(sim.getCurrentState().toStringNoParentheses());
		    if (transitionTarget == 0)
			return 0;
		}
	    
		else
		    return path_probability/modified_probability;
		
		/* ABSTRACT generalization:
		   1. initialize the path, then step time
		   2. loop over all transitions from current state, detect catalog inclusion 
		   -> check conditions to add state into the catalog
		   3. apply rate enhancements (THIS NEEDS A CUSTOM TRANSITION)
		   4. (?) check conditions to include in catalog (?)
		   5. check stopping conditions
		*/
		
	    }
	    return 0;
		

	} 
	catch (PrismException e) {
	    System.out.println("Error: " + e.getMessage());
	    System.exit(1);
	}
	return 0;
    }
}

//==============================================================================
//  scaffoldImportanceSampling: an IS experiment for CTMC models
//    Concept: A CTMC is specified, and a target state property 
//             During stochastic simulation, if the target is reached, then all
//             states from the successful path are added to a catalog.
//             During path generation, at each transition, if the next state 
//             is in the catalog, then the transition rate is enhanced by a 
//             constant multiplier M. Reverse transitions (back to the immediate last
//             state) are not enhanced.
//
//             The weighted path probability is computed
//             incrementally with each transition. A path is truncated if it exceeds
//             TMAX transitions without reaching either the target or a catalog state.
//
// Chris Winstead, Utah State University
//
// Based on PRISM API demo "SimulateModel". 

package imsam;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.Logger;
import org.kohsuke.args4j.Option;

import parser.ast.ModulesFile;
import prism.Prism;
import prism.PrismDevNullLog;
import prism.PrismException;
import prism.PrismLog;
import simulator.RandomNumberGenerator;
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
public class ScaffoldImportanceSampling extends Command {

	final static Logger logger = Main.getLogger(ScaffoldImportanceSampling.class);
	

	//////////////////////////////////////////////////
    // CLI Arguments

	@Option(name="-M",usage="Transition multiplier")
	public double M = 2;

	@Option(name="--Tmax",usage="Maximum transitions before truncating")
	public int TMAX = 1000;

	@Option(name="--Nruns",usage="Number of stochastic runs")
	public int Nruns = 100000;

	@Option(name="--raw",usage="Print raw output values")
	public boolean raw = false;

	@Option(name="--model",usage="Filename of prism model")
	public String modelFileName = "models/abstract/10_states/10_states.pm";

	public void printArgs() {
		System.out.printf(" M=%f TMAX=%d Nruns=%d modelFile=%s ", M, TMAX, Nruns, modelFileName);
	}
	public void printRawArgs() {
		System.out.printf("%f\t%d\t%d", M, TMAX, Nruns);
	}

	// end CLI Arguments
    //////////////////////////////////////////////////


    public List<Integer> catalog = new ArrayList<>();

    public PrismLog           prismLog;
    public Prism              prism;
    public SimulatorEngine    sim;

    private RandomNumberGenerator rng = new RandomNumberGenerator();


	@Override
	public int exec() {

		//System.out.println("Running scaffoldImportanceSampling");
		//System.out.println(args.length + " arguments");

		loadModel();
		loadCatalog();
		//printCatalog();

		double sum       = 0.0;
		double squareSum = 0.0;

		List<Double> samples = new ArrayList<>(Nruns);
		for (int n=0; n < Nruns; n++) {
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
		if (raw) {
			System.out.print(mean + "\t" + variance + "\t");
			printRawArgs();
		}
		else {
			System.out.print(" Probability to reach final state: " + mean + ", Variance " + variance);
			printArgs();
		}
		System.out.println("");

		return 0;

	}

    public void loadModel() 
    {
		try {
			//System.out.println("Loading PRISM model from " + params.modelFileName);

			// Create a log for PRISM output (hidden or stdout)
			prismLog = new PrismDevNullLog();
			
			// Initialise PRISM engine 
			prism = new Prism(prismLog);
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

    public void printCatalog()
    {
		for (int i=0; i<catalog.size(); i++) {
			System.out.println(catalog.get(i));
		}
    }

    public void loadCatalog()
    {
		BufferedReader reader = null;				// Declare outside of try block to be accessible from final block
		try {
			reader = new BufferedReader(new FileReader(modelFileName));
			String[] seedPathStr = reader.lines()
					.filter(line -> line.matches("\\s*//\\s*SeedPath:\\s*\\d.*"))	// Filter lines to find Seed Path
					.findFirst()													// Select first match only
					.get()															// Throws NoSuchElementException if no match was found
					.replaceFirst(".*SeedPath:\\s*","")								// Remove label
					.split("\\s*,\\s*");											// Split on comma, allowing whitespace
			for (String s : seedPathStr) {
				catalog.add(Integer.parseInt(s));
			}
		}
		catch (FileNotFoundException e) {
			System.out.println("Error: " + e.getMessage());
			System.exit(1);
		}
		catch (NoSuchElementException e) {
			System.out.println("Error: No seed path found");
			System.exit(1);
		}
		finally {
			if (null != reader) {
				try {
					reader.close();					// Close file, if it was opened
				} catch (Exception e) { }			// Ignore errors when closing file
			}
		}
    }

    public void addToCatalog(Integer s)
    {
		if (catalog.indexOf(s) == -1)
			catalog.add(s);
    }

    public boolean inCatalog(Integer s)
    {
		return catalog.indexOf(s) != -1;
    }


    public boolean satisfyCatalogCondition(int currentState, int targetState, double rate)
    {
		return inCatalog(targetState) && !inCatalog(currentState) && (rate > 0);
    }


    public boolean stoppingCondition(int tdx, double path_probability)
    {
		// path_probability currently not used
		return tdx > TMAX;
    }


    public double run()
    {
		try {
			sim.createNewPath();
			sim.initialisePath(null);

			double path_probability     = 1.0;
			double modified_probability = 1.0;
			double total_rate           = 0.0;
			double modified_total_rate  = 0.0;

			List<Integer> visitedStates = new ArrayList<>(TMAX);

			// Simulate a path step-by-step:
			for (int tdx=0; !stoppingCondition(tdx,path_probability); tdx++) {
				// Initialize variables:
				total_rate          = 0.0; 
				modified_total_rate = 0.0;

				// Loop through the possible transitions from the current state:
				Integer   numTransitions  = sim.getNumTransitions();
				List<Double> transitionRates = new ArrayList<>(numTransitions);
				List<Double> nativeRates     = new ArrayList<>(numTransitions);
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
						if (satisfyCatalogCondition(currentState,transitionTarget, sim.getTransitionProbability(idx)))
							addToCatalog(currentState);

						transitionRates.add(sim.getTransitionProbability(idx));
						nativeRates.add(sim.getTransitionProbability(idx));
						total_rate += (double) sim.getTransitionProbability(idx);

						Boolean alreadyVisited = (visitedStates.indexOf(transitionTarget) > -1);
						if (inCatalog(transitionTarget) && !alreadyVisited) { 
							transitionRates.set(idx, M*(double)transitionRates.get(idx));
						}
						else if (alreadyVisited)
							transitionRates.set(idx, (1.0/M)*(double)transitionRates.get(idx));

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
				{
					for (int i=0; i<visitedStates.size(); i++) {
						addToCatalog((Integer)visitedStates.get(i));
					}
					return path_probability/modified_probability;
				}
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
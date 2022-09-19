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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.lang.Math;

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

	@Option(name="--target",usage="Target state")
	public double target = 8;

	@Option(name="-M",usage="Transition multiplier")
	public double M = 2;

	@Option(name="--Tmax",usage="Maximum transitions before truncating")
	public double TMAX = 1000;

	@Option(name="--Nruns",usage="Number of stochastic runs")
	public int Nruns = 100000;

	@Option(name="--raw",usage="Print raw output values")
	public boolean raw = false;

	@Option(name="--model",metaVar="FILENAME",usage="Prism model file name")
	public String modelFileName = "models/example.pm";

	public String argsToString() {
		return String.format("M=%f TMAX=%f Nruns=%d modelFile=%s ", M, TMAX, Nruns, modelFileName);
	}
	public String argsRawToString() {
		return String.format("%f\t%f\t%d", M, TMAX, Nruns);
	}

	// end CLI Arguments
    //////////////////////////////////////////////////


    public List<Integer> catalog = new ArrayList<>();

    public PrismLog           prismLog;
    public Prism              prism;
    public SimulatorEngine    sim;

    private RandomNumberGenerator rng = new RandomNumberGenerator();


	@Override
	public int exec() throws IOException, PrismException {

		logger.debug("Running scaffoldImportanceSampling");

		loadModel();
		loadCatalog();
		printCatalog();		// Prints at TRACE logger level only

		double sum       = 0.0;
		long   binarySum = 0;
		double squareSum = 0.0;

		List<Double> samples = new ArrayList<>(Nruns);
		for (int n=0; n < Nruns; n++) {
			double sample = simulate();
			sum += sample;
			if (sample > 0)
			    binarySum++;

			samples.add(sample);
			logger.debug(sim.getPath());
			logger.debug("=================");
		}	    
		
		prism.closeDown();

		double mean = sum/(double)Nruns;
		double importanceSampleRate = (double)binarySum/Nruns;

		for (int n=0; n<Nruns; n++) {
			double squareTerm = (double)samples.get(n) - mean;
			squareSum += squareTerm*squareTerm; 
		}
		double variance = squareSum/((double)Nruns*((double)Nruns-1));
		if (raw) {
			logger.info(mean + "\t" +
						variance + "\t" +
						binarySum + "\t" +
						importanceSampleRate + "\t" +
						argsRawToString());
		}
		else {
		    logger.info("Probability to reach final state: " + mean +
						", Variance " + variance +
						", non-zero samples "+ binarySum +
						", useful sample rate " + importanceSampleRate +
						" " + argsToString());
		}

		printCatalog();		// Prints at TRACE logger level only

		return 0;

	}

    public void loadModel() throws IOException, PrismException
    {
		try {
			//System.out.println("Loading PRISM model from " + params.modelFileName);

			// Create a log for PRISM output (hidden or stdout)
			prismLog = new PrismDevNullLog();
			
			// Initialize PRISM engine 
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
			throw new IOException("Prism model file not found", e);
		}
    }

    public void printCatalog()
    {
		for (int i=0; i<catalog.size(); i++) {
			logger.trace(catalog.get(i));
		}
    }

    public void loadCatalog() throws IOException
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
		}catch (NoSuchElementException e) {
			throw new IOException("SeedPath empty or invalid format", e);
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


    public boolean stoppingCondition(double t, double path_probability)
    {
		// path_probability currently not used
		//if ((t > TMAX) || indicatorFunction())
		if (indicatorFunction())
			return true;
		return false;
    }

    public double boundedTimeProbability(double mu, double sigma) 
    { 
		return CNDF((TMAX-mu)/sigma);
    }


    public boolean indicatorFunction()
    {
		Integer state = Integer.parseInt(sim.getCurrentState().toStringNoParentheses());
		if (state == target)
			return true;
		else
			return false;
    }

    int getTarget(int idx) throws PrismException
    {
		return Integer.parseInt(sim.computeTransitionTarget(idx).toStringNoParentheses());
    }

    int getState(int idx) 
    {
		return Integer.parseInt(sim.getCurrentState().toStringNoParentheses());
    }


    int makeTransition(double modified_total_rate,int numTransitions,List<Double> transitionRates)
		throws PrismException
    {
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
	    
		sim.manualTransition(offset);
		return offset;
    }


    /**
	 * Returns the cumulative normal distribution function (CNDF)
	 * for a standard normal: N(0,1)
	 */
    double CNDF(double x)
    {
		int neg = (x < 0d) ? 1 : 0;
		if ( neg == 1) 
			x *= -1d;

		double k = (1d / ( 1d + 0.2316419 * x));
		double y = (((( 1.330274429 * k - 1.821255978) * k + 1.781477937) *
				k - 0.356563782) * k + 0.319381530) * k;
		y = 1.0 - 0.398942280401 * Math.exp(-0.5 * x * x) * y;

		return (1d - neg) * y + neg * (1d - y);
    }


	/**
	 * ABSTRACT generalization:
	 *   1. initialize the path, then step time
	 *   2. loop over all transitions from current state, detect catalog inclusion 
	 *      -> check conditions to add state into the catalog
	 *   3. apply rate enhancements (THIS NEEDS A CUSTOM TRANSITION)
	 *   4. (?) check conditions to include in catalog (?)
	 *   5. check stopping conditions
	 * @return Path probability
	 * @throws PrismException
	 */
    public double simulate() throws PrismException
    {
		sim.createNewPath();
		sim.initialisePath(null);

		double path_probability     = 1.0;
		double modified_probability = 1.0;
		double total_rate           = 0.0;
		double modified_total_rate  = 0.0;

		List<Integer> visitedStates = new ArrayList<>();
		
		double mu     = 0;
		double sigma2 = 0;

		// Simulate a path step-by-step:
		//int tdx = 0;
		do {
			//for (int tdx= 0; !stoppingCondition(sim.getTotalTimeForPath(),path_probability); tdx++) {
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
				Integer transitionTarget = getTarget(idx);
				if (satisfyCatalogCondition(currentState,transitionTarget, sim.getTransitionProbability(idx)))
					addToCatalog(currentState);
				
				transitionRates.add(sim.getTransitionProbability(idx));
				nativeRates.add(sim.getTransitionProbability(idx));
				total_rate += (double) sim.getTransitionProbability(idx);
				
				Boolean alreadyVisited = (visitedStates.indexOf(transitionTarget) > -1);
				if (inCatalog(transitionTarget) && !alreadyVisited) { 
					transitionRates.set(idx, (double)transitionRates.get(idx));
				}
				else 
					transitionRates.set(idx, (M)*(double)transitionRates.get(idx));
				
				modified_total_rate += (double) transitionRates.get(idx);
				}
				//++++++++++++++++++++++++++++++++++++++++++++++++++++
				
				int offset = makeTransition(modified_total_rate,numTransitions,transitionRates);

				// Accumulate path probability:
				mu += 1.0/nativeRates.get(offset);
				sigma2 += mu*mu;

				double p_transition   = (double)nativeRates.get(offset)/total_rate; 
				path_probability     *= p_transition;
				double p_modified     = (double)transitionRates.get(offset)/modified_total_rate;
				modified_probability *= p_modified;
				
				Integer transitionTarget = Integer.parseInt(sim.getCurrentState().toStringNoParentheses());
				logger.trace(transitionTarget+"("+transitionRates.get(offset)+"),t="+sim.getTotalTimeForPath());
				if (transitionTarget == 0)
				return 0;
			}
			
			else
			{
				//if (sim.getTotalTimeForPath() > TMAX)
				//	    return 0;
				//					else 
				if (indicatorFunction()) {
					logger.trace("="+path_probability+"/"+modified_probability+"="+(path_probability/modified_probability)+"\n");
					for (int i=0; i<visitedStates.size(); i++) {
					addToCatalog((Integer)visitedStates.get(i));
					}

					return (path_probability/modified_probability)*boundedTimeProbability(mu, Math.sqrt(sigma2)); 
				}
				else {
					logger.trace("=0\n");
					return 0;
				}
			}
			
		} while (!stoppingCondition(sim.getTotalTimeForPath(),path_probability));

		if (indicatorFunction()) {
			logger.trace("="+path_probability+"/"+modified_probability+"="+(path_probability/modified_probability)+"\n");
			return path_probability/modified_probability*boundedTimeProbability(mu, Math.sqrt(sigma2));
		}
		else {
			logger.trace("=0\n");
			return 0;
		}
	}


}

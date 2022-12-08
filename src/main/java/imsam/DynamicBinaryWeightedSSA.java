//==============================================================================
//  DynamicBinaryWeightedSSA: an IS experiment for CTMC models
//    Concept: A CTMC is specified via a PRISM model file.
//             Simulation "constraint" and "objective" are given as labels
//             in the PRISM file. 
//             During stochastic simulation, transitions are only allowed if
//             the constraint is satisfied. When the constraint is violated,
//             the corresponding transition rate is set to zero. The importance 
//             sampling weight is the ratio of the sum of constrained transition 
//             rates to the sum of all transition rates.
//
// Chris Winstead, Utah State University
//
// Based on PRISM API demo "SimulateModel". 

package imsam;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
//ArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.kohsuke.args4j.Option;

import parser.Values;
import parser.ast.ModulesFile;
import prism.ModelGenerator;
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
public class DynamicBinaryWeightedSSA extends Command {

	final static Logger logger = Main.getLogger(DynamicBinaryWeightedSSA.class);
	

    //////////////////////////////////////////////////
    // CLI Arguments

	@Option(name="--Tmax",usage="Maximum transitions before truncating")
	public double TMAX = 10;

	@Option(name="--Nruns",usage="Number of stochastic runs")
	public int Nruns = 1;

	@Option(name="--raw",usage="Print raw output values")
	public boolean raw = false;

	@Option(name="--model",metaVar="FILENAME",usage="Prism model file name")
	public String modelFileName = "models/three_rxn.pm";

	@Option(name="--const",usage="Model constant name=value")
	public String modelConstant = "";

	@Option(name="--weight",usage="TODO: needs description")
	public Map<String,Double> predilections = Map.of("[r3]", 1e20);

	public String argsToString() {
		return String.format("TMAX=%f Nruns=%d modelFile=%s weights=%s", TMAX, Nruns, modelFileName, predilections);
	}
	public String argsRawToString() {
		return String.format("%f\t%d", TMAX, Nruns);
	}

	// end CLI Arguments
    //////////////////////////////////////////////////


    public PrismLog           prismLog;
    public Prism              prism;
    public SimulatorEngine    sim;
    public ModelGenerator     info;
    
    //private int               constraintIndex;
    //private int               objectiveIndex;
    private RandomNumberGenerator rng = new RandomNumberGenerator();

	@Override
	public int exec() throws IOException, PrismException {

		logger.debug("Running Dynamic Binary Weighted SSA");

		loadModel();

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
			logger.log(Main.LOG_ALWAYS,
						mean + "\t" +
						variance + "\t" +
						binarySum + "\t" +
						importanceSampleRate + "\t" +
						argsRawToString());
		}
		else {
		    logger.log(Main.LOG_ALWAYS,
						"Probability to reach final state: " + mean +
						", Variance " + variance +
						", non-zero samples "+ binarySum +
						", useful sample rate " + importanceSampleRate +
						" " + argsToString());
		}

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

			if (modelConstant != "")
			    {
				String[] cval = modelConstant.split("=");
				Values v = new Values();
				String constName=cval[0];
				Integer constVal=Integer.parseInt(cval[1]);
				v.addValue(constName,constVal);
				prism.setPRISMModelConstants(v);
			    }
			// Load the model into the simulator
			prism.loadModelIntoSimulator();
			sim = prism.getSimulator();
			info = prism.getModelGenerator();
			//constraintIndex = info.getLabelIndex("constraint");
			//objectiveIndex  = info.getLabelIndex("objective");
			//logger.trace("Constraint " + constraintIndex + "Objective " + objectiveIndex);
		}
		catch (FileNotFoundException e) {
			throw new IOException("Prism model file not found", e);
		}
    }





    public boolean indicatorFunction() throws PrismException
    {
	//Integer state = Integer.parseInt(sim.getCurrentState().toStringNoParentheses());
	if (info.isLabelTrue("objective"))
	    return true;
	else
	    return false;
    }

    public boolean stoppingCondition(double t, double path_probability) throws PrismException
    {
	// path_probability currently not used
	if ((t > TMAX) || indicatorFunction())
	    return true;
	return false;
    }

    String getTarget(int idx) throws PrismException
    {
		return sim.computeTransitionTarget(idx).toStringNoParentheses();
    }

    String getState(int idx) 
    {
		return sim.getCurrentState().toStringNoParentheses();
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
	 *   2. loop over all transitions from current state, check constraint
	 *   3. zero-out transitions that violate the constraint
	 *   4. check stopping conditions
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

		int step = 0;
		
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
			//Integer   currentState    = Integer.parseInt(sim.getCurrentState().toStringNoParentheses());
			

			if (numTransitions > 0) {			
				//++++++++++++++++++++++++++++++++++++++++++++++++++++
				// Adjust Transition Rates 			
				//++++++++++++++++++++++++++++++++++++++++++++++++++++
			    logger.trace("------ Step " + step + " -----");
			    for (int idx=0; idx<numTransitions; idx++) { 
				// Get target state of the indexed transition:
				String transitionTarget = getTarget(idx);
				logger.trace("  * transition " + idx + " to state " + transitionTarget);

				sim.manualTransition(idx);
				logger.trace("constraint query: " + info.isLabelTrue("constraint"));
				Boolean constraintSatisfied = info.isLabelTrue("constraint");
				sim.backtrackTo(step);			

				if (constraintSatisfied)
				    {
					double r=sim.getTransitionProbability(idx);
					String s = sim.getTransitionActionString(idx);
					if (s !=null) {
					    logger.trace("action label " + s);
					    Double delta = (Double) predilections.get(s);
					    if (delta != null) {
						r=r*delta;
						logger.trace("predilection " + delta);
					    }
					}
					transitionRates.add(r);
				    }
				else
				    {
					transitionRates.add(0.0);
				    }


				nativeRates.add(sim.getTransitionProbability(idx));
				total_rate += (double) sim.getTransitionProbability(idx);
				
				modified_total_rate += (double) transitionRates.get(idx);
				}
				//++++++++++++++++++++++++++++++++++++++++++++++++++++
				
				int offset = makeTransition(modified_total_rate,numTransitions,transitionRates);
				step++;

				// Accumulate path probability:
				mu += 1.0/nativeRates.get(offset);
				sigma2 += mu*mu;

				double p_transition   = (double)nativeRates.get(offset)/total_rate; 
				path_probability     *= p_transition;
				double p_modified     = (double)transitionRates.get(offset)/modified_total_rate;
				modified_probability *= p_modified;
				
				String transitionTarget = sim.getCurrentState().toStringNoParentheses();
				logger.trace(transitionTarget+"("+transitionRates.get(offset)+"),t="+sim.getTotalTimeForPath());
				//if (transitionTarget == 0)
				//return 0;
			}
			
			else
			{
				//if (sim.getTotalTimeForPath() > TMAX)
				//	    return 0;
				//					else 
				if (indicatorFunction()) {
					logger.trace("="+path_probability+"/"+modified_probability+"="+(path_probability/modified_probability)+"\n");
					return path_probability/modified_probability;
				}
				else {
					logger.trace("=0\n");
					return 0;
				}
			}
			
		} while (!stoppingCondition(sim.getTotalTimeForPath(),path_probability));

		if (indicatorFunction()) {
			logger.trace("="+path_probability+"/"+modified_probability+"="+(path_probability/modified_probability)+"\n");
			return path_probability/modified_probability;
		}
		else {
			logger.trace("=0\n");
			return 0;
		}
	}


}

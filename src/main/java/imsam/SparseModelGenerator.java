package imsam;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.kohsuke.args4j.Option;

import imsam.probability.ProbabilityDistribution;
import imsam.probability.UniformIntDistribution;

/**
 * TODO
 */
public class SparseModelGenerator implements Callable<Integer> {

    //////////////////////////////////////////////////
    // CLI Arguments

    @Option(name="-N",usage="number of states to generate. default: 20")
    public int numberOfStates = 20;

    @Option(name="-target-state",usage="index of the target state (zero indexed). default: 1")
    public int targetState = 1;

    @Option(name="-output",usage="name of the output Prism file. default: sparse-model.pm")
    public String outputFilename = "sparse-model.pm";

    @Option(name="-config",usage="model generator config json file. See README for examples.")
    public String configFilename = "";

    // end CLI Arguments
    //////////////////////////////////////////////////

    ProbabilityDistribution transitionCountDistribution = null;
    ProbabilityDistribution transitionRateDistribution = null;
    State[] stateSpace = null;
    String seedPath = "";

    /**
     * Default constructor should only be called by args4j,
     * otherwise use constructor with arguments.
     */
    public SparseModelGenerator() {
        // Nothing to do
    }

    /**
     * Constructor with arguments that setup all required parameters.
     * Note that outputPrismFilename has a default value, but can be
     * changed with setOutputPrismFile() method.
     * @param numberOfStates number of states in the model
     * @param transitionFilename
     * @param targetState
     */
    public SparseModelGenerator(int numberOfStates,
                                int targetState,
                                ProbabilityDistribution transitionCountDistribution,
                                ProbabilityDistribution transitionRateDistribution,
                                String outputFilename)
    {
        this.numberOfStates = numberOfStates;
        this.targetState = targetState;
        this.transitionCountDistribution = transitionCountDistribution;
        this.transitionRateDistribution = transitionRateDistribution;
        this.outputFilename = outputFilename;
    }

    /**
     * This function reads parameters from the config file
     * if one is provided. CLI and constructor arguments take
     * priority over config file values. This method must ensure
     * that all required variables are set. It can also be used to
     * reset the state space to be run again.
     */
    public void init() throws IllegalArgumentException {
        if (!configFilename.isBlank()) {
            //TODO: read config file
        }
        if (targetState >= numberOfStates || targetState < 0) {
            String errMsg = "Target state must be in the state space!";
            System.err.println(errMsg);
            throw new IllegalArgumentException(errMsg);
        }
        stateSpace = new State[numberOfStates];
        seedPath = "";
        if (null == transitionCountDistribution) {
            transitionCountDistribution = new UniformIntDistribution(1,4);
        }
        if (null == transitionRateDistribution) {
            transitionRateDistribution = new UniformIntDistribution(1, 10);
        }
    }

    @Override
    public Integer call() throws IllegalArgumentException{
        init();
        generateModel();
        generateSeedPath();
        savePrismFile();
        return 0;
    }

    protected class State {
        int stateId;
        List<TransitionPath> transitionsOut;
        List<TransitionPath> transitionsIn;
        State(int stateId) {
            this.stateId = stateId;
            transitionsOut = new ArrayList<>();
            transitionsIn = new ArrayList<>();
        }
    }
    protected class TransitionPath {
        int start;
        int end;
        double rate;
        TransitionPath(int start, int end, double rate) {
            this.start = start;
            this.end = end;
            this.rate = rate;
        }
    }
    
}

package imsam;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.kohsuke.args4j.Option;

import imsam.probability.ProbabilityDistribution;
import imsam.probability.UniformIntDistribution;

/**
 * TODO
 */
public class SparseModelGenerator extends Command {

    final static Logger logger = Main.getLogger(SparseModelGenerator.class);

    //////////////////////////////////////////////////
    // CLI Arguments

    @Option(name="-I",usage="number of models to generate; iterations. default: 1")
    public int iterations = -1;

    @Option(name="-N",usage="number of states to generate. default: 10")
    public int numberOfStates = -1;

    @Option(name="--target-state",usage="index of the target state (zero indexed). default: 1")
    public int targetState = -1;

    @Option(name="--output",usage="name of the output Prism file. default: sparse-model.pm")
    public String outputFilename = "";

    @Option(name="--config",usage="model generator config json file. See README for examples.")
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
    public void init() throws IllegalArgumentException, JSONException, IOException {
        // Read values from config file. CLI args take priority
        if (!configFilename.isBlank()) {
            Path filepath = Path.of(configFilename);
            String str = Files.readString(filepath);
            JSONObject json = new JSONObject(str);
            if (-1 == iterations && json.has("iterations")) {
                iterations = json.getInt("iterations");
            }
            if (-1 == numberOfStates && json.has("numberOfStates")) {
                numberOfStates = json.getInt("numberOfStates");
            }
            if (-1 == targetState && json.has("targetState")) {
                targetState = json.getInt("targetState");
            }
            if (outputFilename.isBlank() && json.has("outputFilename")) {
                outputFilename = json.getString("outputFilename");
            }
            if (json.has("transitionCountDistribution")) {
                transitionCountDistribution = ProbabilityDistribution.ParseJson(
                        json.getJSONObject("transitionCountDistribution")
                );
            }
            if (json.has("transitionRateDistribution")) {
                transitionRateDistribution = ProbabilityDistribution.ParseJson(
                        json.getJSONObject("transitionRateDistribution")
                );
            }
        }
        // Argument checking and set defaults
        if (-1 == iterations) {
            iterations = 1;
        } else if (iterations < 1) {
            String errMsg = "Number of iterations must be positive, non-zero";
            logger.error(errMsg);
            throw new IllegalArgumentException(errMsg);
        }
        if (-1 == numberOfStates) {
            numberOfStates = 10;
        } else if (numberOfStates < 2) {
            String errMsg = "Model must have at least 2 states";
            logger.error(errMsg);
            throw new IllegalArgumentException(errMsg);
        }
        if (-1 == targetState) {
            targetState = 1;
        } else if (targetState >= numberOfStates || targetState < 0) {
            String errMsg = "Target state must be in the state space!";
            logger.error(errMsg);
            throw new IllegalArgumentException(errMsg);
        }
        if (outputFilename.isBlank()) {
            if (1 == iterations) {
                outputFilename = "sparse-model.pm";
            } else {
                outputFilename = "sparse-model-%i%.pm";
            }
        }
        if (null == transitionCountDistribution) {
            transitionCountDistribution = new UniformIntDistribution(1,4);
        }
        if (null == transitionRateDistribution) {
            transitionRateDistribution = new UniformIntDistribution(1, 10);
        }
        logger.debug("Config File: '" + configFilename + "'");
        logger.debug("Iterations: " + iterations);
        logger.debug("Number of States: " + numberOfStates);
        logger.debug("Target State: " + targetState);
        logger.debug("Output File: " + outputFilename);
    }

    @Override
    public int exec() throws IllegalArgumentException, JSONException, IOException{
        init();
        for (int i=0; i<iterations; i++) {
            logger.info("Generating model " + (i+1) + "/" + iterations);
            generateModel();
            generateSeedPath();
            savePrismFile(i);
        }
        return 0;
    }

    protected void generateModel() {
        stateSpace = new State[numberOfStates];
        // Initialize State objects
        logger.debug("Initializing state space");
        for (int stateId=0; stateId<numberOfStates; stateId++) {
            stateSpace[stateId] = new State(stateId);
        }
        logger.debug("Generating transitions");
        for (int stateId=0; stateId<numberOfStates; stateId++) {
            stateSpace[stateId] = new State(stateId);
            int transitionCount = (int) transitionCountDistribution.random();
            logger.trace("Generating " + transitionCount + " transitions for state " + stateId);
            for (int transitionId = 0; transitionId < transitionCount; transitionId++) {
                int successor = (int) (Math.random() * numberOfStates);
                if (stateId != successor) {
                    TransitionPath transition = new TransitionPath(
                            stateId,
                            successor, 
                            transitionRateDistribution.random()
                    );
                    stateSpace[stateId].transitionsOut.add(transition);
                    stateSpace[successor].transitionsIn.add(transition);
                }
            }
        }
        int temp = 0;
        boolean[] seedTracker = new boolean[numberOfStates];
        seedTracker[0] = true;
        for (int stateId=0; stateId+1<numberOfStates; stateId++) {
            int successor = (int) (Math.random() * numberOfStates);
            while (seedTracker[successor]) {
                successor = (int) (Math.random() * numberOfStates);
            }
            if (temp != successor) {
                TransitionPath transition = new TransitionPath(
                        stateId,
                        successor,
                        transitionRateDistribution.random()
                );
                stateSpace[stateId].transitionsOut.add(transition);
                stateSpace[successor].transitionsIn.add(transition);
            }
            seedTracker[successor] = true;
            temp = successor;
        }
    }

    protected void generateSeedPath() {
        StringBuilder strBldr = new StringBuilder();
        int tracker = 0;
        while(tracker != targetState) {
            strBldr.append(tracker + ",");
            tracker = stateSpace[tracker].transitionsOut.get((int) (Math.random() * stateSpace[tracker].transitionsOut.size())).end;
        }
        strBldr.append(tracker);
        seedPath = strBldr.toString();
    }

    protected void savePrismFile(int iteration) throws IOException {
        String filename = resolvePlaceholders(outputFilename, iteration+1);
        logger.debug("Output filename after resolving placeholders: '" + filename + "'");
        FileWriter writer = new FileWriter(filename);
        writer.write("ctmc\n\n");
        writer.write("// SeedPath: " + seedPath + "\n\n");
        writer.write("module M1\n");
        writer.write("    x : [0.."+(numberOfStates-1)+"];\n");
        for (int i=0; i<numberOfStates; i++) {
            writer.write("    [] x=" + i + " -> ");
            for (int i2=0; i2<stateSpace[i].transitionsOut.size(); i2++) {
                TransitionPath transition = stateSpace[i].transitionsOut.get(i2);
                writer.write((int) transition.rate + ":(x'=" + transition.end + ")");
                if (i2+1 < stateSpace[i].transitionsOut.size()) {
                    writer.write(" + ");
                } else {
                    writer.write(";\n");
                }
            }
        }
        writer.write("endmodule\n");
        writer.close();
    }

    protected String resolvePlaceholders(String str, int iteration) {
        return str.replaceAll("%i%",Integer.toString(iterations))
                .replaceAll("%numberOfStates%",Integer.toString(numberOfStates))
                .replaceAll("%targetState%",Integer.toString(targetState));
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

package imsam.mgen;

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

import imsam.Command;
import imsam.probability.ProbabilityDistribution;
import imsam.probability.UniformIntDistribution;


public abstract class MGen extends Command {

    final static Logger logger = imsam.Main.getLogger(MGen.class);

    protected abstract void generateModel();

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

    protected ProbabilityDistribution transitionCountDistribution = null;
    protected ProbabilityDistribution transitionRateDistribution = null;
    protected State[] stateSpace = null;
    protected String seedPath = "";

    /**
     * This function reads parameters from the config file
     * if one is provided. CLI and constructor arguments take
     * priority over config file values. This method must ensure
     * that all required variables are set. It can also be used to
     * reset the state space to be run again.
     */
    public final void init() throws IllegalArgumentException, JSONException, IOException {
        // Read values from config file. CLI args take priority
        if (!configFilename.isBlank()) {
            Path filepath = Path.of(configFilename);
            String str = Files.readString(filepath);
            JSONObject json = new JSONObject(str);
            json.keys().forEachRemaining((key) -> {
                switch (key) {
                    case "iterations":
                        if (-1 == iterations) {
                            iterations = json.getInt(key);
                        }
                        break;
                    case "numberOfStates":
                        if (-1 == numberOfStates) {
                            numberOfStates = json.getInt(key);
                        }
                        break;
                    case "targetState":
                        if (-1 == targetState) {
                            targetState = json.getInt(key);
                        }
                        break;
                    case "outputFilename":
                        if (outputFilename.isBlank()) {
                            outputFilename = json.getString(key);
                        }
                        break;
                    case "transitionCountDistribution":
                        transitionCountDistribution = ProbabilityDistribution.ParseJson(
                            json.getJSONObject(key)
                        );
                        break;
                    case "transitionRateDistribution":
                        transitionRateDistribution = ProbabilityDistribution.ParseJson(
                            json.getJSONObject(key)
                        );
                        break;
                    default:
                        if (!parseSubclassConfigParam(json, key)) {
                            logger.warn("Unrecognized config option '" + key + "'");
                        }
                }
            });
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
            targetState = numberOfStates - 1;
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
        initSubclassParamDefaults();
    }

    /**
     * This method can optionally be overridden by subclasses to implement
     * parameters specific to that model generator. It is used to parse a
     * parameter from the config file. Do not override a value that is
     * already set; CLI args take priority. If the key is not a valid
     * parameter, then return false.
     * @param json config file as JSONObject
     * @param key the key for the parameter to be parsed
     * @return true if the key is valid, otherwise false
     */
    protected boolean parseSubclassConfigParam(JSONObject json, String key) {
        // No more parameters to parse. This key is invalid
        return false;
    }
    
    /**
     * This method can optionally be overridden by subclasses to implement
     * parameters specific to that model generator. It is used to set the
     * default value for these parameters if they have not already been set.
     */
    protected void initSubclassParamDefaults() {
        // Intentionally left empty
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
        return str.replaceAll("%i%",Integer.toString(iteration))
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

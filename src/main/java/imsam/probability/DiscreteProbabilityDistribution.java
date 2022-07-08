package imsam.probability;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.stream.Stream;

import org.json.JSONException;
import org.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Simple implementation for representing histograms. Variables are stored
 * internally as doubles. Includes functions to read from basic (space/tab/
 * comma delimited) format and json data sources.
 * 
 * Uses private constructors and static from* methods.
 * 
 * @author Andrew Gerber
 */
public class DiscreteProbabilityDistribution implements ProbabilityDistribution {
    
    final static Logger logger = LogManager.getLogger(DiscreteProbabilityDistribution.class);


    ///////////////////////////////////////////////////////////
    // Static Methods

    /**
     * Reads a discrete distribution (histogram) from a tab, space, or comma
     * delimited data sources. Elements are separated by new lines.
     * 
     * ex: 6-sided dice
     *  1 0.1667
     *  2 0.1667
     *  3 0.1667
     *  4 0.1667
     *  5 0.1667
     *  6 0.1667
     * 
     * @param str input discrete distribution (histogram) data
     * @return constructed DiscreteDistribution object
     */
    public static DiscreteProbabilityDistribution fromStringBasic(String str) throws IllegalArgumentException {
        String[] lines = str.lines().toArray(String[]::new);
        DiscreteProbabilityDistribution distribution
                = new DiscreteProbabilityDistribution(lines.length);
        Scanner scanner = null;
        int lineNum = 1;
        try {
            for (String line : lines) {
                if (!line.strip().isEmpty()) {
                    scanner = new Scanner(line);
                    scanner.useDelimiter("[\\s,]+");            // whitespace or commas
                    double value = scanner.nextDouble();
                    double probability = scanner.nextDouble();
                    distribution.probabilities.add(new ProbabilityMapping(value, probability));
                    lineNum++;
                }
            }
        } catch (InputMismatchException | NumberFormatException ex) {
            String msg = "Format error on line "+lineNum+" of histogram input string - "+ex.getMessage();
            logger.error(msg, ex);
            throw new IllegalArgumentException(msg, ex);
        } catch (NoSuchElementException | IllegalStateException ex) {
            String msg = "Unexpected EOF while scanning histogram input string - "+ex.getMessage();
            logger.error(msg, ex);
            throw new IllegalArgumentException(msg, ex);
        } finally {
            if (null != scanner) {
                scanner.close();
            }
        }
        distribution.normalize();
        return distribution;
    }

    /**
     * Reads a discrete distribution (histogram) from a JSON data string.
     * 
     * ex: 6-sided dice
     *  {"1":"0.1667", "2":"0.1667", "3":"0.1667", "4":"0.1667", "5":"0.1667", "6":"0.1667"}
     * 
     * @param json input discrete distribution (histogram) data as JSON string
     * @return constructed DiscreteDistribution object
     */
    public static DiscreteProbabilityDistribution fromStringJSON(String json) throws IllegalArgumentException, JSONException {
        return fromJSON(new JSONObject(json));
    }

    /**
     * Reads a discrete distribution (histogram) from a JSONObject.
     * 
     * @param json input discrete distribution (histogram) data as JSONObject
     */
    public static DiscreteProbabilityDistribution fromJSON(JSONObject json) throws IllegalArgumentException {
        DiscreteProbabilityDistribution distribution;
        distribution = new DiscreteProbabilityDistribution(json.length());
        try {
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                distribution.probabilities.add(new ProbabilityMapping(
                            Double.parseDouble(key),
                            Double.parseDouble(json.get(key).toString())
                ));
            }
        }
        catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
        distribution.normalize();
        return distribution;
    }

    /**
     * Reads a discrete distribution (histogram) from tab, space, or
     * comma delimited data from a file source. Elements are separated
     * by new lines.
     * @param filename file path to data source
     * @return constructed DiscreteDistribution object
     */
    public static DiscreteProbabilityDistribution fromFileBasic(String filename) throws IllegalArgumentException, IOException {
        Path filepath = Path.of(filename);
        return fromStringBasic(Files.readString(filepath));
    }

    /**
     * Reads a discrete distribution (histogram) from a json file source.
     * @param filename file path to json data source
     * @return constructed DiscreteDistribution object
     */
    public static DiscreteProbabilityDistribution fromFileJSON(String filename) throws IllegalArgumentException, JSONException, IOException {
        Path filepath = Path.of(filename);
        return fromStringJSON(Files.readString(filepath));
    }

    // 
    ///////////////////////////////////////////////////////////




    ///////////////////////////////////////////////////////////
    // Class Methods

    /**
     * Main data
     */
    private final List<ProbabilityMapping> probabilities;

    /**
     * Constructors are private. Use static methods to create
     * new class objects.
     * @param size initial size of ArrayList
     */
    private DiscreteProbabilityDistribution(int size) {
        this.probabilities = new ArrayList<ProbabilityMapping>(size);
    }

    /**
     * Constructors are private. Use static methods to create
     * new class objects
     * @param probabilities list of type ProbabilityMapping to back the object
     */
    private DiscreteProbabilityDistribution(List<ProbabilityMapping> probabilities) {
        this.probabilities = probabilities;
    }

    /**
     * Returns the probability of the given value
     * @param value value to get probability for
     * @return probability associated with the value
     */
    public double getProbability(double value) throws NoSuchElementException {
        for (ProbabilityMapping probMapping : probabilities) {
            if (probMapping.value == value) {
                return probMapping.probability;
            }
        }
        return 0;
    }

    /**
     * Returns a random value based on the histograms probability
     * distribution
     * @return random value from distribution
     */
    @Override
    public double random() {
        return 0;
    }

    /**
     * Returns the histogram as a space delimited string
     * 
     * ex: 6-sided dice
     *  1 0.1667
     *  2 0.1667
     *  3 0.1667
     *  4 0.1667
     *  5 0.1667
     *  6 0.1667
     * 
     * @return histogram as string
     */
    public String toStringBasic() {
        return toStringBasic(" ");
    }

    /**
     * Returns the histograms as a string using the specified
     * delimiter
     * @param delimiter character to use as delimiter
     * @return histogram as string
     */
    public String toStringBasic(String delimiter) {
        StringBuilder builder = new StringBuilder();
        for (ProbabilityMapping probMapping : probabilities) {
            builder.append(probMapping.value)
                    .append(delimiter)
                    .append(probMapping.probability)
                    .append("\n");
        }
        if (!probabilities.isEmpty()) {
            builder.deleteCharAt(builder.length()-1);
        }
        return builder.toString();
    }

    /**
     * Returns the histogram as a JSONObject
     * 
     * ex: 6-sided dice
     *  {"1":"0.1667", "2":"0.1667", "3":"0.1667", "4":"0.1667", "5":"0.1667", "6":"0.1667"}
     * 
     * @return histogram as JSON
     */
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        for (ProbabilityMapping probMapping : probabilities) {
            json.put(
                String.valueOf(probMapping.value),
                probMapping.probability
            );
        }
        return json;
    }


    /**
     * Override toString method to return object in json format.
     * 
     * @return histogram as string in json format
     */
    @Override
    public String toString() {
        return toJSON().toString();
    }

    /**
     * Creates the specified file with the histogram represented
     * as space delimited data. If the file already exists, it will
     * be overwritten automatically.
     * @param filename name of file to write to
     */
    public void toFileBasic(String filename) throws IOException {
        toFileBasic(filename, " ");
    }

    /**
     * Creates the specified file with the histogram represented
     * using the given delimiter.  If the file already exists, it
     * will be overwritten automatically.
     * @param filename name of file to write to
     * @param delimiter character to use as delimiter
     */
    public void toFileBasic(String filename, String delimiter) throws IOException {
    }

    /**
     * Creates the specified file with the histogram represented
     * in JSON format. If the file already exists, it will be
     * overwritten automatically.
     * @param filename name of file to write to
     */
    public void toFileJSON(String filename) throws IOException {
    }

    /**
     * Return the number of elements contained in this histogram
     * @return number of elements
     */
    public int size() {
        return probabilities.size();
    }

    /**
     * Used in the creation of a Histogram object. Non-normalized
     * data can be provided as input to then be normalized before
     * construction of the new object is complete.
     */
    private void normalize() {
        double totalProbability = 0;
        for (ProbabilityMapping probMapping : probabilities) {
            totalProbability += probMapping.probability;
        }
        if (totalProbability != 1) {
            for (ProbabilityMapping probMapping : probabilities) {
                probMapping.probability = probMapping.probability / totalProbability;
            }
        }
    }

    //
    ///////////////////////////////////////////////////////////



    ///////////////////////////////////////////////////////////
    // Static Subclass

    /**
     * Simple structure to store the value/probability pairs
     */
    public static class ProbabilityMapping {
        public double value;
        public double probability;
        ProbabilityMapping(double value, double probability) {
            this.value = value;
            this.probability = probability;
        }
    }

    //
    ///////////////////////////////////////////////////////////
    
}

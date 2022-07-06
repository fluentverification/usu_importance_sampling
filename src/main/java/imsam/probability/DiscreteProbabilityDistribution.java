package imsam.probability;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.json.JSONException;
import org.json.JSONObject;

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
        return null;
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
        DiscreteProbabilityDistribution hist = new DiscreteProbabilityDistribution(json.length());
        try {
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                hist.probabilityMappings.add(new ProbabilityMapping(
                            Double.parseDouble(key),
                            Double.parseDouble((String) json.get(key))
                ));
            }
            return hist;
        }
        catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Reads a discrete distribution (histogram) from tab, space, or
     * comma delimited data from a file source. Elements are separated
     * by new lines.
     * @param filename file path to data source
     * @return constructed DiscreteDistribution object
     */
    public static DiscreteProbabilityDistribution fromFileBasic(String filename) throws IllegalArgumentException, IOException {
        return null;
    }

    /**
     * Reads a discrete distribution (histogram) from a json file source.
     * @param filename file path to json data source
     * @return constructed DiscreteDistribution object
     */
    public static DiscreteProbabilityDistribution fromFileJSON(String filename) throws IllegalArgumentException, JSONException, IOException {
        return null;
    }

    // 
    ///////////////////////////////////////////////////////////




    ///////////////////////////////////////////////////////////
    // Class Methods

    /**
     * Main data
     */
    private final List<ProbabilityMapping> probabilityMappings;

    /**
     * Constructors are private. Use static from* methods to create
     * new class objects.
     * @param size initial size of ArrayList
     */
    private DiscreteProbabilityDistribution

    /**
     * Returns the probability of the given value
     * @param value value to get probability for
     * @return probability associated with the value
     */
    @Override
    public double getProbability(double value) throws NoSuchElementException {
        return 0;
    }

    /**
     * Returns the probability  of the given value. The tolerance
     * is used to compensate for rounding errors. In the case of
     * multiple values within the tolerance, the lowest value is
     * used.
     * @param value value to get probability for
     * @param tolerance tolerance +/- of value
     * @return probability associated with the value
     */
    @Override
    public double getProbability(double value, double tolerance) throws NoSuchElementException {
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
        return "";
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
        return null;
    }


    /**
     * Override toString method to return object in json format.
     * 
     * @return histogram as string in json format
     */
    @Override
    public String toString() {
        return "";
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
     * @param filenamen name of file to write to
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
     * Used in the creation of a Histogram object. Non-normalized
     * data can be provided as input to then be normalized before
     * construction of the new object is complete.
     */
    private void normalize() {
    }

    //
    ///////////////////////////////////////////////////////////



    ///////////////////////////////////////////////////////////
    // Static Subclass

    /**
     * Simple structure to store the value/probability pairs
     */
    public static class Element {
        public double value;
        public double probability;
        Element(double value, double probability) {
            this.value = value;
            this.probability = probability;
        }
    }

    //
    ///////////////////////////////////////////////////////////
    
}

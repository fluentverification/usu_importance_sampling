import java.util.ArrayList;
import java.util.List;

/**
 * Simple implementation for representing histograms. Variables are stored
 * internally as doubles. Includes functions to read from basic (space/tab/
 * comma delimited) format and json data sources.
 * 
 * Uses private constructors and static from* methods.
 * 
 * @author Andrew Gerber
 */
public class Histogram implements ProbabilityDistribution {
    

    ///////////////////////////////////////////////////////////
    // Static Methods

    /**
     * Reads a histogram from a tab, space, or comma delimited data
     * sources. Elements are separated by new lines.
     * 
     * ex: 6-sided dice
     *  1 0.1667
     *  2 0.1667
     *  3 0.1667
     *  4 0.1667
     *  5 0.1667
     *  6 0.1667
     * 
     * @param str input histogram data
     * @return constructed Histogram object
     */
    public static Histogram fromStringBasic(String str) {
    }

    /**
     * Reads a histogram from json data sources.
     * 
     * ex: 6-sided dice
     *  {"1":"0.1667", "2":"0.1667", "3":"0.1667", "4":"0.1667", "5":"0.1667", "6":"0.1667"}
     * 
     * @param json input histogram data in json format
     * @return constructed Histogram object
     */
    public static Histogram fromStringJson(String json) {
    }

    /**
     * Reads a histogram from tab, space, or comma delimited data
     * from a file source. Elements are separated by new lines.
     * @param filename file path to data source
     * @return constructed Histogram object
     */
    public static Histogram fromFileBasic(String filename) {
    }

    /**
     * Reads a histogram from a json file source.
     * @param filename file path to json data source
     * @return constructed Histogram object
     */
    public static Histogram fromFileJson(String filename) {
    }

    // 
    ///////////////////////////////////////////////////////////




    ///////////////////////////////////////////////////////////
    // Class Methods

    /**
     * Main data
     */
    private final List<Element> elements;

    /**
     * Constructors are private. Use static from* methods to create
     * new class objects.
     * @param size initial size of ArrayList
     */
    private Histogram(int size) {
        elements = new ArrayList<Element>(size);
    }

    /**
     * Returns the probability of the given value
     * @param value value to get probability for
     * @return probability associated with the value
     */
    @Override
    public double getProbability(double value) {
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
    public double getProbability(double value, double tolerance) {
    }

    /**
     * Returns a random value based on the histograms probability
     * distribution
     * @return random value from distribution
     */
    @Override
    public double random() {
    }

    private void normalize() {
    }

    //
    ///////////////////////////////////////////////////////////



    ///////////////////////////////////////////////////////////
    // Subclass

    /**
     * Simple structure to store the value/probability pairs
     */
    public class Element {
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

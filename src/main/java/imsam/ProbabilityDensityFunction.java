package imsam;

/**
 * A standard interface for probability density functions (PDF)
 */
public interface ProbabilityDensityFunction {

    /**
     * Returns the probability of the given value
     * @param value value to get probability for
     * @return probability associated with the value
     */
    public double getProbability(double value);

    /**
     * Returns the probability  of the given value. The tolerance
     * is used to compensate for rounding errors. In the case of
     * multiple values within the tolerance, the lowest value is
     * used.
     * 
     * This only makes sense to use with discrete distributions
     * 
     * @param value value to get probability for
     * @param tolerance tolerance +/- of value
     * @return probability associated with the value
     */
    public double getProbability(double value, double tolerance);

    /**
     * Returns a random value based on this probability
     * distribution
     * @return random value from this distribution
     */
    public double random();
    
}

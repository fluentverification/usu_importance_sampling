//package imsam.probability;

/**
 * A standard interface for probability density functions (PDF)
 */
public interface ProbabilityDistribution {

    /**
     * Returns the probability of the given value
     * @param value value to get probability for
     * @return probability associated with the value
     */
    public double getProbability(double value);

    /**
     * Returns a random value based on this probability
     * distribution
     * @return random value from this distribution
     */
    public double random();
    
}

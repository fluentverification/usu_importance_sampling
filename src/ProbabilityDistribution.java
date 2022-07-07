//package imsam.probability;

/**
 * A standard interface for probability density functions (PDF)
 */
public interface ProbabilityDistribution {

    /**
     * Returns a random value based on this probability
     * distribution
     * @return random value from this distribution
     */
    public double random();
    
}

package imsam.probability;

import java.util.Random;


/**
 * Simple implementation of ProbabilityDistribution modeling
 * a fair coin toss.
 */
public class CoinTossDistribution implements ProbabilityDistribution {

    private final Random rand;


    public CoinTossDistribution() {
        rand = new Random();
    }

    public CoinTossDistribution(long seed) {
        rand = new Random(seed);
    }

    
    public double getProbability(double value) {
        if (0 == value || 1 == value) {
            return 0.5;
        }
        else {
            return 0;
        }
    }

    @Override
    public double random() {
        return rand.nextBoolean() ? 1 : 0;
    }
    
}

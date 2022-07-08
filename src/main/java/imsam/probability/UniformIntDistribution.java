package imsam.probability;

import java.util.Random;

import imsam.probability.ProbabilityDistribution;


public class UniformIntDistribution implements ProbabilityDistribution {

    private final int min;
    private final int max;
    private final Random rand;

    
    public UniformIntDistribution(int max) {
        this(0, max);
    }

    public UniformIntDistribution(int min, int max) {
        this.rand = new Random();
        this.min  = min;
        this.max  = max;
    }

    public UniformIntDistribution(int max, long seed) {
        this(0, max, seed);
    }

    public UniformIntDistribution(int min, int max, long seed) {
        this.rand = new Random(seed);
        this.min  = min;
        this.max  = max;
    }


    @Override
    public double random() {
        return rand.nextInt((max - min) + 1) + min;
    }
    
}

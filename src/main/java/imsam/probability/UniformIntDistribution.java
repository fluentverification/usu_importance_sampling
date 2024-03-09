package imsam.probability;

import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;


public class UniformIntDistribution implements ProbabilityDistribution {

    public static final String TYPE_KEY = "uniform-int";

    public static UniformIntDistribution fromJSON(JSONObject json) throws JSONException {
        return new UniformIntDistribution(json);
    }



    
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

    public UniformIntDistribution(JSONObject json) throws JSONException {
        this.max = json.getInt("max");
        if (json.has("min")) {
            this.min = json.getInt("min");
        } else {
            this.min = 0;
        }
        if (json.has("seed")) {
            this.rand = new Random(json.getLong("seed"));
        } else {
            this.rand = new Random();
        }
    }


    @Override
    public double random() {
        return rand.nextInt((max - min) + 1) + min;
    }
    
}

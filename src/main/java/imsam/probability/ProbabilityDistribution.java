package imsam.probability;

import org.json.JSONObject;

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

    public static ProbabilityDistribution ParseJson(JSONObject json) {
        String type = json.getString("type");
        switch (type) {
            case DiscreteProbabilityDistribution.TYPE_KEY:
                return DiscreteProbabilityDistribution.fromJSON(json);
            case UniformIntDistribution.TYPE_KEY:
                return UniformIntDistribution.fromJSON(json);
        }
        return null;
    }
    
}

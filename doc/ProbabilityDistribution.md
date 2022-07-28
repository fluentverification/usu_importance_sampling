# `ProbabilityDistribution` interface

This is interface is used to model probability mass/density functions.
A basic template implementation is provided below.

```java
package imsam.probability;

import java.util.Random;

import org.json.JSONOBject;
import org.json.JSONException;

public class ExampleProbabilityDistribution implements ProbabilityDistribution {

    // Each implementation requires a unique `TYPE_KEY`
    public static final String TYPE_KEY = "example";

    public static ExampleProbabilityDistribution fromJSON(JSONObject json) {
        // Construct the object
    }



    private final Random rand;

    public ExampleProbabilityDistribution() {
        this.rand = new Random();
    }

    // Each implementation should be capable of accepting a random seed
    public ExampleProbabilityDistribution(long seed) {
        this.rand = new Random(seed);
    }

    public double random() {
        return rand.nextDouble();
    }

}
```

<br>

## JSON Configuration Examples

### `DiscreteProbabilityDistribution`

```json
{
    "type": "discrete",     // (required)
    "seed": 26152745,       // (optional) specify random generator seed
    "values": {             // (required) key-value pairs for discrete distributions
        "1": 5,
        "2": 2,
        "3": 1,
        "4": 7,
        "5": 3,
    },
}
```

### `UniformIntDistribution`

```json
{
    "type": "uniform-int",      // (required)
    "seed": 26152745,           // (optional) specify random generator seed
    "min": 1,                   // (optional; default=0) minimum value, inclusive
    "max": 10,                  // (required) maximum value, inclusive
}
```

### `GaussianDistribution`

```json
{
    "type": "gaussian",         // (required)
    "mean": 0.5,                // (optional; default=0)
    "max": 1,                   // (optional; default=1)
}
```




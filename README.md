# Java importance sampling experiments

These simulations use the PRISM model checker engine to 
simulate Markov models. Path generation is managed to 
implement various experiments with importance sampling, 
importance splitting, and related techniques.

<br>

# Dependencies and Build Instructions

Tested on Ubuntu 20 *WSL* and Centos 7

## Dependencies:

- Install the following packages (package names for Debian/Ubuntu Based):
    - openjdk-17-jdk *(or 13; 17 preferred)*
    - gcc
    - g++
    - git
    - make
- Clone this repository and run `./bin/initPrism.sh` to install Prism from source

<br>

## Build instructions:

1. Run `./gradlew build`


#### Troubleshooting

- Run `./gradlew -v`. Gradle should be 7, JVM should be 13-17
- Java version not available?
    - Debian/Ubuntu Based: run `apt update`
    - Arch-based: `pacman -Syuu`
- Error `Cannot find symbol ... Prism* ...` or `package parser/prism does not exist`
    - Prism is not installed/compiled correctly
    - Run `./bin/initPrism.sh`

<br>

 ## Run:

- Simulations can be run using `./bin/run.sh` from the repository root directory
- The Sparse Model Generator can be run using `PRISM_MAINCLASS=imsam.SparseModelGenerator ./bin/run.sh`.

<br><br>

# Development

Example model generator config file

```json
{
    "numberOfModels": 10,   // number of models to generate
    "numberOfStates": 100,  // number of states in each model
    "targetState": 99,      // the target state used when generating the seed path
    "outputFilename": "sparse-model%i.pm",      // name of Prism output file, can include placeholders
    "transitionCountDistribution": {
        "type": "discrete",     // probability distributions must always include a type
        "values": {             // key-value pairs for discrete distributions
            "1": 5,
            "2": 2,
            "3": 1,
            "4": 7,
            "5": 3,
        },
    },
    "transitionRateDistribution": {
        "type": "uniform-int",
        "min": 1,               // minimum value (inclusive)
        "max": 10,              // maximum value (inclusive)
    }
}
```
## Using Libraries

- `org.kohsuke.args4j`
    - Here is the [javadoc](https://args4j.kohsuke.org/args4j/apidocs/). The `Option` class is the most used.

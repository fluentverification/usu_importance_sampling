# Java importance sampling experiments

These simulations use the PRISM model checker engine to 
simulate Markov models. Path generation is managed to 
implement various experiments with importance sampling, 
importance splitting, and related techniques.

# Dependencies and Build Instructions

Dependencies:

- Install the following packages:
    - openjdk-13-jdk
    - gcc
    - g++
    - git
    - make
- Clone this repository and run `./bin/initPrism.sh` to install Prism from source

Build instructions:

1. Run `./gradlew build`
2. Simulations can now by run using `./bin/run.sh` from the repository root directory





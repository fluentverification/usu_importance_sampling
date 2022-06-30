# Java importance sampling experiments

These simulations use the PRISM model checker engine to 
simulate Markov models. Path generation is managed to 
implement various experiments with importance sampling, 
importance splitting, and related techniques.

<br>

# Dependencies and Build Instructions

Tested on Ubuntu 20 *WSL* and Centos 7

### Dependencies:

- Install the following packages (package names for Ubuntu):
    - openjdk-17-jdk *(or 13; 17 preferred)*
    - gcc
    - g++
    - git
    - make
- Clone this repository and run `./bin/initPrism.sh` to install Prism from source

### Build instructions:

1. Run `./gradlew build`
2. Simulations can now by run using `./bin/run.sh` from the repository root directory

<br>

### Troubleshooting

- Run `./gradlew -v`. Gradle should be 7, JVM should be 13-17
- Java version not available?
    - Ubuntu: run `apt update`
- Error `Cannot find symbol ... Prism* ...` or `package parser/prism does not exist`
    - Prism is not installed/compiled correctly
    - Run `./bin/initPrism.sh`
    

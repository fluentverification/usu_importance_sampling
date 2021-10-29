# Java importance sampling experiments

These simulations use the PRISM model checker engine to 
simulate Markov models. Path generation is managed to 
implement various experiments with importance sampling, 
importance splitting, and related techniques.

# Dependencies and Build Instructions

## Cygwin

* Java Open-JDK 13 -- Download from [http://jdk.java.net/java-se-ri/13]
* Development package group, specifically: 
     - gcc (tested with gcc-11.2.0)
     - git (tested with 2.33.0)
     - gnu make (tested with 2.33.0)
* PRISM v4.6 source distribution -- downloaded automatically by the Makefile
* PRISM-API v4.6 -- dowloaded automatically by the Makefile

Build instructions:

1. Install jdk-13, gcc, git, and make.
2. Set JAVA_DIR environment variable to location of jdk-13 
3. Clone this repository and run `make`





# Java importance sampling experiments

These simulations use the PRISM model checker engine to 
simulate Markov models. Path generation is managed to 
implement various experiments with importance sampling, 
importance splitting, and related techniques.

<br>

# Build Instructions

Tested on Ubuntu 20 *WSL* and Centos 7

## Dependencies:

- Install the following packages (package names for Debian/Ubuntu Based):
    - openjdk-17-jdk *(or 13; 17 preferred)*
    - build-essential
        - gcc
        - g++
        - git
        - make
- Clone this repository and run `./bin/initPrism.sh` to install Prism from source

<br>

## Build:

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

# Usage:

 ### Common Arguments

 | Usage: | `./bin/run.sh <command> [OPTION]...` |
 |---|---|
 | `-v` | Verbose logging (Info level) |
 | `-vv` | Verbose logging (Debug level) |
 | `-vvv` | Verbose logging (Trace level) |
 | `--quiet` | Suppress logging to console, except for errors |

### Scaffold Importance Sampling

| Usage: | `./bin/run.sh simulate [OPTION]...` |
|-|-|
| `-M` | Transition multiplier (default: 2) |
| `-Tmax` | Maximum transitions before truncating (default: 1,000) |
| `--Nruns` | Number of stochastic runs (default: 100,000) |
| `--raw` | Print raw output values, no labels |
| `--model` | Prism model file name |

### Model Generator (MGen)
| Usage: | `./bin/run.sh mgen <mgenID> [OPTION]...` |
|-|-|
| `mgenID` | The model generator to use (`sparse` or `multi-target`) |
| `-I` | Number of models to generate, iterations. (default: 1) |
| `-N` | Number of states to generate (default: 10) |
| `--target-state` | Index of the target state, zero indexed (default: `numberOfState`-1)
| `--output` | Name of the output Prism file (default: `sparse-model-%i%.pm`)
| `--config` | Model generator json config file. See example below

<br>

### Multi-target Model Generator Options
 Usage: | `./bin/run.sh mgen multi-target [OPTION]...` |
|-|-|
| `--target-list`  | List of states that paths will terminate in, must placed in a comma-separated string (ex. `"1 4 2"`). Automatically adds state specified by `--target-state`. Cannot be used with `--absorb`.  |
| `--absorb` | Creates an absorbing state that all states not on the target path will go to in one step. Cannot be used with `--target-list`. |
| `--simple` | Generate only target paths |

<br>
The absolute path to `/.bin/run.sh` could be added to the user or system path
as a symlink `imsam` if desired. Later version may have an install option to
do this.

<br><br>

# Development

## VS Code Environment

### If using WSL , the following configuration is a recommended basic setup
  1. Clone repo to windows filesystem. (ex: `C:/Users/*\<username\>*/git-clones/`)
      - Note: Accessing the Windows file system from WSL is slower, but is more convenient
      and still provides sufficient performance for this use case.
  2. Create a symlink in WSL by running
  ```ln -s /mnt/c/Users/*\<username\>*/git-clones/usu_importance_sampling ~/```
  3. Open VS code and install the `Remote - WSL` extension
  4. Click the bottom-left green button and open a WSL instance

### Recommended VS Code Extensions (Installed in WSL instance)
- Extension Pack for Java
- GitLens (Optional, but recommended)
- Code Spell Checker (Optional, but recommended)

<br>

## Example model generator config file
Note: JSON does not actually support comments
```json
// Placeholders:
//      %i% - the index of this model, from 1 to "iterations"
//      %numberOfStates% - number of states in the model
//      %targetState% - the target state used when generating the seed path
{
    "iterations": 10,   // (optional; default=1) number of models to generate
    "numberOfStates": 100,  // (optional; default=10) number of states in each model
    "targetState": 99,      // (optional; default=1) the target state used when generating the seed path
    "outputFilename": "sparse-model-%i%-%numberOfStates%states.pm",
        // (optional; default="sparse-model-%i%.pm") name of Prism output file, can include placeholders
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
        "seed": 26152745,       // optionally specify random generator seed
        "min": 1,               // minimum value (inclusive; default=0)
        "max": 10,              // maximum value (inclusive; required)
    }
}
```

<br>

## Argument Parsing

**[Documentation link.](https://args4j.kohsuke.org/args4j/apidocs/)** `Option` and `Argument` are the most useful.

This utility uses the `org.kohsuke.args4j` library. The `Main` class handles parsing the
arguments, then passes control to one of the subcommands. All subcommands extend the abstract
class `Command` and are registered in `Main` with the `@SubCommand` annotation.

A template subcommand `MyCommand` is provided below
```java
package imsam;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

public class MyCommand extends Command {

    @Argument(index=0,required=true,metaVar="a",usage="first operand")
    public int a;

    @Argument(index=1,required=true,metaVar="b",usage="second operand")
    public int b;

    @Option(name="--diff",usage="perform a difference operation, instead of addition")
    public boolean diff=false;

    @Override
    public int exec() {
        if (diff) {
            System.out.println(a-b);
        } else {
            System.out.println(a+b);
        }
    }

}
```

Verbose logging options are already implemented by the abstract class `Command`.

<br>

## Logging

**[Documentation link](https://logging.apache.org/log4j/2.x/manual/api.html)**

This utility uses the `org.apache.logging.log4j` library (log4j v2). The main configuration
is in the `src/main/resources/log4j.properties` and setup is handled by `Main`. Each class
should use it's own logger to help distinguish were a log message was generated. An example
logger of `MyClass` is shown below.
```java
package imsam;

import org.apache.logging.log4j.Logger;

public class MyClass extends Command {

    final static Logger logger = Main.getLogger(MyClass.class);

    @Override
    public int exec() {
        logger.info("MyClass is running");
        logger.debug("This is an example class used to show how to use logging");
        for (int i=0; i<10; i++) {
            logger.trace("Iteration " + i);
        }
        logger.warn("This class does nothing. why are using it?");
    }

}
```

| Log Level | Audience | Description | Integer Value |
| --- | --- | --- | --- |
| `OFF` |  | Logging is completely off | 0 |
| `FATAL` |  | Really bad stuff, or application crash | 100 |
| `ERROR` | | Something is broken and needs fixing immediately | 200 |
| `WARN` | | Something isn't right, but the application can still work | 300 |
| `INFO` | Everyone | Basic information about the program (requires `-v` option) | 400 |
| `DEBUG` | Developer/Adv. User | Details about what the application is doing (requires `-vv` option) | 500 |
| `TRACE` | Developer | Super detailed stuff. Probably use this inside loops (required `-vvv` option) | 600 |

<br>

## Adding Dependencies/Libraries

Dependencies can easily be included from the maven central repository. You can find libraries by using
[search.maven.org](https://search.maven.org/). There are a lot of libraries available, so make sure
you are actually using the one you want. When looking for a library, ask if what you need is already
available in a library that is already included. If not, try to find the most mainstream library and
look at when it was last updated.

The Apache POI library (MS Excel files) would be added by inserting the following in the `dependencies`
section of `build.gradle`.
```gradle
dependencies {

    libBundle "org.apache.poi:poi:5.2.2"
    
}
```
Tutorials will say to use `implementation` or `compile` instead of `libBundle`. The `libBundle` is a custom
configuration used to bundling the libraries into a single jar file.

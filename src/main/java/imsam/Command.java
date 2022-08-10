package imsam;

import org.apache.logging.log4j.Level;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionHandlerFilter;

/**
 * Implementations of this abstract class are used to create the command
 * hierarchy of the program. Work is performed by the exec() method. The
 * entryPoint() method is called when passing control to a next level
 * subcommand. Generic argument flags, such as logging directives, are
 * implemented here to allow them to be inserted anywhere in the argument
 * hierarchy.
 */
public abstract class Command {

    @Option(name="-v",usage="enable verbose logging (INFO level)")
    protected boolean verboseLogging1 = false;

    @Option(name="-vv",usage="enable verbose logging (DEBUG level)")
    protected boolean verboseLogging2 = false;

    @Option(name="-vvv",usage="enable verbose logging (TRACE level)")
    protected boolean verboseLogging3 = false;

    @Option(name="--quiet",usage="disable logging to console, except for errors")
    protected boolean quietLogging = false;

    @Option(name="--help",aliases={"-h"},help=true,usage="Show usage information")
    protected boolean showHelp = false;


    /**
     * This abstract method is where control is passed to subcommands.
     * It should never actually be called outside entryPoint. Be sure
     * to call the entryPoint method instead.
     * @return system exit code
     * @throws Exception can throw uncaught exceptions if it makes sense to do so
     */
    protected abstract int exec() throws Exception;

    /**
     * This method parses any logging args, then executes the exec method
     * @return system exit code
     * @throws Exception can throw uncaught exceptions if it makes sense to do so
     */
    public final int entryPoint() throws Exception {
        readCommonArgs();      // read any common args provided to this command
        return exec();
    }

    /**
     * Parses the logging arguments that are available from any level of the command structure
     */
    public final void readCommonArgs() {
        if (showHelp) {
            Main.printUsage(this);
            System.exit(0);
        }
        if (quietLogging) {
            Main.disableConsoleLogging();
        }
        if (verboseLogging3) {
            Main.setLogLevel(Level.TRACE);
            Main.logger.info("Log level set to TRACE");
        } else if (verboseLogging2) {
            Main.setLogLevel(Level.DEBUG);
            Main.logger.info("Log level set to DEBUG");
        } else if (verboseLogging1) {
            Main.setLogLevel(Level.INFO);
            Main.logger.info("Log level set to INFO");
        }
    }
    
}

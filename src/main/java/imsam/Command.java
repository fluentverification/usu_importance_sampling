package imsam;

import org.apache.logging.log4j.Level;
import org.kohsuke.args4j.Option;

public abstract class Command {

    @Option(name="-v",usage="enable verbose logging (INFO level)")
    protected boolean verboseLogging1 = false;

    @Option(name="-vv",usage="enable verbose logging (DEBUG level)")
    protected boolean verboseLogging2 = false;

    @Option(name="-vvv",usage="enable verbose logging (TRACE level)")
    protected boolean verboseLogging3 = false;

    @Option(name="--quiet",usage="disable all logging to console")
    protected boolean quietLogging = false;


    protected abstract int exec() throws Exception;

    public final int entryPoint() throws Exception {
        readLoggingArgs();      // read any logging args provided after the command arg
        return exec();
    }

    public final void readLoggingArgs() {
        if (verboseLogging3) {
            Main.setLogLevel(Level.TRACE);
        } else if (verboseLogging2) {
            Main.setLogLevel(Level.DEBUG);
        } else if (verboseLogging1) {
            Main.setLogLevel(Level.INFO);
        }
        if (quietLogging) {
            Main.disableConsoleLogging();
        }
    }
    
}

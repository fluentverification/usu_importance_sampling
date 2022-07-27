package imsam;

import org.apache.logging.log4j.Level;
import org.kohsuke.args4j.Option;

public abstract class Command {

    @Option(name="-v",usage="enable verbose logging (INFO level)")
    protected boolean verboseInfo = false;

    @Option(name="-vv",usage="enable verbose logging (DEBUG level)")
    protected boolean verboseDebug = false;

    @Option(name="-vvv",usage="enable verbose logging (TRACE level)")
    protected boolean verboseTrace = false;


    protected abstract int exec() throws Exception;

    public int entryPoint() throws Exception {
        if (verboseTrace) {
            Main.setLogLevel(Level.TRACE);
        } else if (verboseDebug) {
            Main.setLogLevel(Level.DEBUG);
        } else if (verboseInfo) {
            Main.setLogLevel(Level.INFO);
        }
        return exec();
    }
    
}

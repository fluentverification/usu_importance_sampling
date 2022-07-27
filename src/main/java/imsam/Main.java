package imsam;

import java.util.concurrent.Callable;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.SubCommand;
import org.kohsuke.args4j.spi.SubCommandHandler;
import org.kohsuke.args4j.spi.SubCommands;


public class Main {

    private static final LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
    public static final Logger logger = loggerContext.getRootLogger();

    @Option(name="-v",usage="enable verbose logging (INFO level)")
    protected boolean verboseInfo = false;

    @Option(name="-vv",usage="enable verbose logging (DEBUG level)")
    protected boolean verboseDebug = false;

    @Option(name="-vvv",usage="enable verbose logging (TRACE level)")
    protected boolean verboseTrace = false;
    

    @Argument(required=true,index=0,metaVar="command",usage="subcommands, e.g., {generate|simulate}",handler=SubCommandHandler.class)
    @SubCommands({
        @SubCommand(name="generate",impl=SparseModelGenerator.class),
        @SubCommand(name="simulate",impl=SimulateCommand.class),
        @SubCommand(name="simulation",impl=SimulateCommand.class),
        @SubCommand(name="sim",impl=SimulateCommand.class),
    })
    protected Callable<Integer> command; // Use Callable instead of Runnable to allow exceptions


    public static void main(String[] args) {
        Main main = new Main();     // This is ugly, but an instance of the class is required by args4j
        CmdLineParser parser = new CmdLineParser(main);
        try {
            parser.parseArgument(args);
            if (main.verboseTrace) {
                setLogLevel(Level.TRACE);
            } else if (main.verboseDebug) {
                setLogLevel(Level.DEBUG);
            } else if (main.verboseInfo) {
                setLogLevel(Level.INFO);
            }
            main.command.call();
        } catch (CmdLineException ex) {
            System.err.println(ex.getMessage());
            return;
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }


    public static class SimulateCommand implements Callable<Integer> {
        public Integer call() {
            scaffoldImportanceSampling.main(new String[0]);
            return 0;
        }
    }


    public static void setLogLevel(Level logLevel) {
        loggerContext.getConfiguration()
                .getLoggerConfig(LogManager.ROOT_LOGGER_NAME)
                .setLevel(logLevel);
        loggerContext.updateLoggers();
    }


    public static Logger getLogger(Class<?> clazz) {
        return loggerContext.getLogger(clazz);
    }
    
}

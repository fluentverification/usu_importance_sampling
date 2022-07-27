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


public class Main extends Command {

    private static final LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
    public static final Logger logger = loggerContext.getRootLogger();
    

    @Argument(required=true,index=0,metaVar="command",usage="subcommands, e.g., {generate|simulate}",handler=SubCommandHandler.class)
    @SubCommands({
        @SubCommand(name="generate",impl=SparseModelGenerator.class),
        @SubCommand(name="simulate",impl=ScaffoldImportanceSampling.class),
        @SubCommand(name="sim",impl=ScaffoldImportanceSampling.class),
    })
    protected Command command; // Use Callable instead of Runnable to allow exceptions


    public static void main(String[] args) {
        Main main = new Main();     // This looks weird, but an instance of the class is required by args4j
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
            main.command.entryPoint();
        } catch (CmdLineException ex) {
            System.err.println(ex.getMessage());
            return;
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    @Override
    protected int exec() {
        // Only sub-commands need to use this method
        // This class extends Command only to inherit generic args
        return 0;
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

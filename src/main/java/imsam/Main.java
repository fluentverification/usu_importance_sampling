package imsam;

import java.util.stream.Stream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.spi.SubCommand;
import org.kohsuke.args4j.spi.SubCommandHandler;
import org.kohsuke.args4j.spi.SubCommands;

import imsam.mgen.MGenCommand;


public class Main extends Command {

    /**
     * The logger context is used to set settings
     */
    private static final LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
    
    /**
     * This is the root logger for this program. Other classes should
     * usually use `Main.getLogger()` and have their own.
     */
    public static final Logger logger = loggerContext.getRootLogger();
    public static final Level LOG_ALWAYS = Level.forName("ALWAYS", 0);

    public static final Main main = new Main();       // This looks weird, but an instance of the class is required by args4j

    public static final CmdLineParser parser = new CmdLineParser(main);
    

    @Argument(required=true,index=0,metaVar="command",usage="subcommand (use --help option with subcommand for more information)",handler=SubCommandHandler.class)
    @SubCommands({
        @SubCommand(name="mgen",impl=MGenCommand.class),
        @SubCommand(name="simulate",impl=ScaffoldImportanceSampling.class),
        @SubCommand(name="dbwSSA",impl=DynamicBinaryWeightedSSA.class),
    })
    protected Command command;


    /**
     * Initialize an instance of main and a CmdLineParse. Parse
     * args and move to main entryPoint. Exceptions should be caught
     * and meaningful error messages given.
     * @param args see README.md for details
     */
    public static void main(String[] args) {
        try {
            parser.parseArgument(args);
            System.exit( main.entryPoint() );
        } catch (CmdLineException ex) {
            System.err.println();
            System.err.println(ex.getMessage());
            System.err.println();
            printUsage(ex);
            System.exit(1);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            System.exit(1);
        }
    }

    /**
     * This method just needs to pass control to subcommand
     */
    @Override
    protected int exec() throws Exception {
        command.entryPoint();
        return 0;
    }

    public static void printUsage(Command cmd) {
        _printUsage(
                new CmdLineParser(cmd),
                !cmd.getClass().isInstance(main)
        );
    }

    public static void printUsage(CmdLineException ex) {
        _printUsage(
                ex.getParser(),
                ex.getParser() != Main.parser
        );
    }

    private static void _printUsage(CmdLineParser parser, boolean includeSubcommand) {
        System.err.println("Usage:");
        System.err.print(" ./bin/run.sh");
        Stream.concat(
            Main.parser.getArguments().stream(),
            parser.getArguments().stream()
        ).forEachOrdered(opt -> System.err.print(" "+opt.getDefaultMetaVariable()));
        System.err.println(" [OPTIONS]...\n");
        parser.printUsage(System.err);
    }


    /**
     * Set the logging level for all loggers
     * @param logLevel new log level
     */
    public static void setLogLevel(Level logLevel) {
        loggerContext.getConfiguration()
                .getLoggerConfig(LogManager.ROOT_LOGGER_NAME)
                .setLevel(logLevel);
        loggerContext.updateLoggers();
    }

    /**
     * Disable logging to console, except errors
     */
    public static void disableConsoleLogging() {
        Configuration config = loggerContext.getConfiguration();
        Appender      stdout = config.getAppender("STDOUT");
        Filter        filter = config.getFilter();
        LoggerConfig rootLoggerConfig
                = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        rootLoggerConfig.removeAppender("STDOUT");
        rootLoggerConfig.addAppender(stdout, Level.ERROR, filter);
        loggerContext.updateLoggers();
        logger.info("Set STDOUT log level to ERROR");
    }


    /**
     * Get a new logger for the provided class
     * @param clazz the class this logger belongs to
     * @return a new logger for this class
     */
    public static Logger getLogger(Class<?> clazz) {
        return loggerContext.getLogger(clazz);
    }
    
}

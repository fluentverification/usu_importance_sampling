package imsam;

import java.util.concurrent.Callable;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.spi.SubCommand;
import org.kohsuke.args4j.spi.SubCommandHandler;
import org.kohsuke.args4j.spi.SubCommands;


public class Main {

    @Argument(required=true,index=0,metaVar="command",usage="subcommands, e.g., {generate|simulate}",handler=SubCommandHandler.class)
    @SubCommands({
        @SubCommand(name="generate",impl=SparseModelGenerator.class),
        @SubCommand(name="simulate",impl=SimulateCommand.class),
        @SubCommand(name="simulation",impl=SimulateCommand.class),
        @SubCommand(name="sim",impl=SimulateCommand.class),
    })
    protected Callable<Integer> command; // Use Callable instead of Runnable to allow exceptions


    public static void main(String[] args) {
        Main main = new Main();
        CmdLineParser parser = new CmdLineParser(main);
        try {
            parser.parseArgument(args);
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
    
}

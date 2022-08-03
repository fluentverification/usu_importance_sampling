package imsam.mgen;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.spi.SubCommand;
import org.kohsuke.args4j.spi.SubCommands;
import org.kohsuke.args4j.spi.SubCommandHandler;

import imsam.Command;

public class MGenCommand extends Command {

    @Argument(required=true,index=0,metaVar="generator",usage="model generator algorithm, e.g., {sparse}",handler=SubCommandHandler.class)
    @SubCommands({
        @SubCommand(name="sparse",impl=SparseModelGenerator.class),
    })
    protected MGen generator;

    @Override
    protected int exec() throws Exception {
        return generator.entryPoint();
    }
    
}

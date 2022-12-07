package imsam.args4j;

import java.util.Map;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.MapOptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

public class MultiMapOptionHandler extends MapOptionHandler  {

    protected static final String delimiterRegex = ",";

    protected final CmdLineParser parser;
    protected final OptionDef option;

    public MultiMapOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super Map<?,?>> setter) {
        super(parser, option, setter);
        this.parser = parser;
        this.option = option;
    }

    @Override
    public int parseArguments(Parameters params) throws CmdLineException {
        for (String arg : params.getParameter(0).split(delimiterRegex)) {
            Parameters splitParam = new ParametersImpl(new String[]{arg}, parser, option);
            super.parseArguments(splitParam);
        }
        return 1;
    }
    
}

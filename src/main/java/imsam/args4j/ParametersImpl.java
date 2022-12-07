package imsam.args4j;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.Parameters;

public class ParametersImpl implements Parameters {

    private final CmdLineParser parser;
    private final OptionDef option;
    
    private final String[] args;
    private int pos;

    ParametersImpl(String[] args, CmdLineParser parser, OptionDef option) {
        this.parser = parser;
        this.option = option;
        this.args = args;
        pos = 0;
    }

    @Override
    @SuppressWarnings("deprecation")    // Use CmdLineException without localization
    public String getParameter(int idx) throws CmdLineException {
        if (pos+idx >= args.length || pos+idx < 0) {
            String msg = String.format("Option \"{0}\" takes an operand", option.toString());
            throw new CmdLineException(parser, msg);
        }
        return args[pos+idx];
    }

    @Override
    public int size() {
        return args.length - pos;
    }

    public boolean hasMore() {
        return pos < args.length;
    }

    public String getCurrentToken() {
        return args[pos];
    }

    public void proceed(int n) {
        pos += n;
    }
    
}

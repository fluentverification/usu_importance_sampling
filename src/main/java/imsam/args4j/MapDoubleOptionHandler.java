package imsam.args4j;

import java.util.Map;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.Setter;

public class MapDoubleOptionHandler extends MultiMapOptionHandler {

    public MapDoubleOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super Map<?, ?>> setter) {
        super(parser, option, setter);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void addToMap(Map m, String key, String value) {
        m.put(key, Double.parseDouble(value));
    }
    
}

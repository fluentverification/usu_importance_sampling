package imsam.args4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.opentest4j.AssertionFailedError;

public class Test_MapDoubleOptionHandler {

    protected class TestClass {
        @Option(name="--dict", metaVar="dict", handler=MapDoubleOptionHandler.class)
        public Map<String,Double> dictionary = new HashMap<>();
    }

    protected Map<String,Double> parse(String[] args) throws CmdLineException {
        TestClass testClass = new TestClass();
        CmdLineParser parser = new CmdLineParser(testClass);
        parser.parseArgument(args);
        return testClass.dictionary;
    }

    @Test
    public void test_MapStringDouble() throws CmdLineException {
        String[] args = "--dict foo=1.11 --dict bar=2.22 --dict baz=3.33".split("\\s+");
        Map<String,Double> map = parse(args);
        try {
            assertTrue(map.size() == 3);
            assertEquals(1.11, map.get("foo"));
            assertEquals(2.22, map.get("bar"));
            assertEquals(3.33, map.get("baz"));
        }
        catch (AssertionFailedError ex) {
            System.err.println(map);
            throw ex;
        }
    }
    
}

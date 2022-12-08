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

public class Test_MapIntOptionHandler {

    protected class TestClass {
        @Option(name="--dict", metaVar="dict", handler=MapIntOptionHandler.class)
        public Map<String,Integer> dictionary = new HashMap<>();
    }

    protected Map<String,Integer> parse(String[] args) throws CmdLineException {
        TestClass testClass = new TestClass();
        CmdLineParser parser = new CmdLineParser(testClass);
        parser.parseArgument(args);
        return testClass.dictionary;
    }

    @Test
    public void test_MapStringInt() throws CmdLineException {
        String[] args = "--dict foo=1 --dict bar=2 --dict baz=3".split("\\s+");
        Map<String,Integer> map = parse(args);
        try {
            assertTrue(map.size() == 3);
            assertEquals(1, map.get("foo"));
            assertEquals(2, map.get("bar"));
            assertEquals(3, map.get("baz"));
        }
        catch (AssertionFailedError ex) {
            System.err.println(map);
            throw ex;
        }
    }
    
}

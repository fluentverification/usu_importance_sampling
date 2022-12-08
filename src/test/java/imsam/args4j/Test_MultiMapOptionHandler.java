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

public class Test_MultiMapOptionHandler {

    protected class TestClass {
        @Option(name="--dict", metaVar="dict", handler=MultiMapOptionHandler.class)
        public Map<String,String> dictionary = new HashMap<>();
    }

    protected Map<String,String> parse(String[] args) throws CmdLineException {
        TestClass testClass = new TestClass();
        CmdLineParser parser = new CmdLineParser(testClass);
        parser.parseArgument(args);
        return testClass.dictionary;
    }

    @Test
    public void test_Empty() throws CmdLineException {
        String[] args = {};
        Map<String,String> map = parse(args);
        assertTrue(map.isEmpty());
    }

    @Test
    public void test_MapStringString() throws CmdLineException {
        String[] args = "--dict foo=Hello --dict bar=World".split("\\s+");
        Map<String,String> map = parse(args);
        try {
            assertEquals(2, map.size());
            assertEquals("Hello", map.get("foo"));
            assertEquals("World", map.get("bar"));
        }
        catch (AssertionFailedError ex) {
            System.err.println(map);
            throw ex;
        }
    }

    @Test
    public void test_MultiMap() throws CmdLineException {
        String[] args = "--dict foo=99,bar=88,baz=77".split("\\s+");
        Map<String,String> map = parse(args);
        try {
            assertEquals(3, map.size());
            assertEquals("99", map.get("foo"));
            assertEquals("88", map.get("bar"));
            assertEquals("77", map.get("baz"));
        }
        catch (AssertionFailedError ex) {
            System.err.println("size="+map.size());
            map.forEach((key, value) -> {
                System.err.println("'"+key+"' : '"+value+"'");
            });
            throw ex;
        }
    }
    
}

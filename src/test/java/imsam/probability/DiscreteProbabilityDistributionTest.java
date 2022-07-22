package imsam.probability;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class DiscreteProbabilityDistributionTest {

    final static Logger logger = LogManager.getLogger(DiscreteProbabilityDistribution.class);

    @Test
    public void test_fromStringBasic() {
        String testInput = "1 4\n"
                            + "2   1\t\n"
                            + "  3\t2\r\n"
                            + "4,1\n\r"
                            + "5, \t1\n"
                            + "\n"
                            + "6 1\n"
                            + " ";
        DiscreteProbabilityDistribution distribution;
        distribution = DiscreteProbabilityDistribution.fromStringBasic(testInput);
        System.out.println("Testing method fromStringBasic():");
        System.out.println(distribution);
        assertTrue(distribution.size() == 6);
        assertTrue(distribution.getProbability(1) == 0.4);
        assertTrue(distribution.getProbability(2) == 0.1);
        assertTrue(distribution.getProbability(3) == 0.2);
        assertTrue(distribution.getProbability(4) == 0.1);
        assertTrue(distribution.getProbability(5) == 0.1);
        assertTrue(distribution.getProbability(6) == 0.1);
        assertTrue(distribution.getProbability(7) == 0);
        System.out.println("\n\n");
    }

    @Test
    public void test_fromStringJSON() {
        String testInput = "{\n"
                            + "  \"type\": \"discrete\","
                            + "  \"values\": {"
                            + "    \"1\":4,"
                            + "    \"2\":1,"
                            + "    \"3\":2,"
                            + "    \"4\":1,"
                            + "    \"5\":1,"
                            + "    \"6\":1"
                            + "  },"
                            + "}";
        DiscreteProbabilityDistribution distribution;
        distribution = DiscreteProbabilityDistribution.fromStringJSON(testInput);
        System.out.println("Testing method fromStringJSON():");
        System.out.println(distribution);
        assertTrue(distribution.size() == 6);
        assertTrue(distribution.getProbability(1) == 0.4);
        assertTrue(distribution.getProbability(2) == 0.1);
        assertTrue(distribution.getProbability(3) == 0.2);
        assertTrue(distribution.getProbability(4) == 0.1);
        assertTrue(distribution.getProbability(5) == 0.1);
        assertTrue(distribution.getProbability(6) == 0.1);
        assertTrue(distribution.getProbability(7) == 0);
        System.out.println("\n\n");
    }

}
import java.util.ArrayList;
import java.util.List;

public class Histogram {
    

    ///////////////////////////////////////////////////////////
    // Static Methods

    public static Histogram fromStringBasic(String str) {
    }

    //TODO: Is JSON the best format
    public static Histogram fromStringJson(String json) {
    }

    public static Histogram fromFileBasic(String filename) {
    }

    public static Histogram fromFileJson(String filename) {
    }

    // 
    ///////////////////////////////////////////////////////////




    ///////////////////////////////////////////////////////////
    // Class Methods

    //TODO: Is 'element' the right name?
    private final List<Element> elements;

    private Histogram(int size) {
        elements = new ArrayList<Element>(size);
    }

    public double getProbability(int value) {
    }

    public int random() {
    }

    //
    ///////////////////////////////////////////////////////////



    ///////////////////////////////////////////////////////////
    // Subclass

    //TODO: Is 'Element' the right name?
    public class Element {
        public int value;
        public double probability;
        Element(int value, double probability) {
            this.value = value;
            this.probability = probability;
        }
    }

    //
    ///////////////////////////////////////////////////////////
    
}

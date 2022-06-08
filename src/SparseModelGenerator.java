import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.lang.Exception;
import java.lang.Math;
import java.lang.Double;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class SparseModelGenerator {
    class Node{ // Node clas is used to track each connection//
        int start;
        int end;
        double rate;
        Node(int begin, int result, double transition){
            start = begin;
            end = result;
            rate = transition;
        }
    }
    int graphSize;
    ArrayList<Node>[] graph; //Array list to track all transitions to and from states in the graph//

    SparseModelGenerator(int size){ //Makes a object for a new graph//
        graphSize = size;
        graph = new ArrayList[graphSize];
    }

    public void makeProb(String filename, ArrayList<Pair<Integer, Double>> itemProbabilityPairs) throws Exception {
        if (itemProbabilityPairs == null) {
            throw new NullPointerException("Item probability pairs is null!");
        }
        File input = new File(filename);
        Scanner fileReader = new Scanner(input);
        int state;
        double prob;
        while (fileReader.hasNextInt() && fileReader.hasNextDouble()){
            state = fileReader.nextInt();
            prob = fileReader.nextDouble();
            itemProbabilityPairs.add(new Pair<Integer, Double>(state, prob));
        }

    }

    public void makeGraph(int rate, String file) throws Exception {
        ArrayList<Pair<Integer, Double>> itemProbabilityPairs = new ArrayList<Pair<Integer,Double>>();
        makeProb(file, itemProbabilityPairs);
        SpecifiedDegreeDistribution<Integer> dist = new SpecifiedDegreeDistribution<Integer>(itemProbabilityPairs);
        for(int count = 0; count < graphSize; count++){
            graph[count] = new ArrayList<Node>();
            int firstChoice = dist.get();
            for(int count2 = 0; count2 < firstChoice; count2++) {
                int successor = (int) (Math.random() * graphSize);
                if(count != successor){
                    graph[count].add(new Node(count, successor, (rate * Math.random())));
                }
            }
        }
        int temp = 0;
        boolean[] seedTracker = new boolean[graphSize];
        seedTracker[0] = true;
        for(int count = 0; count < graphSize - 1; count++){
            int successor = (int) (Math.random() * graphSize);
            while(seedTracker[successor]){
                successor = (int) (Math.random() * graphSize);
            }
            if(temp != successor) {
                graph[temp].add(new Node(temp, successor, (rate * Math.random())));
            }
            seedTracker[successor] = true;
            temp = successor;
        }
    }
    public void printGraph(){
        System.out.println("ctmc\n");
        for(int count = 0; count < graphSize; count++){
            System.out.print("[] x="+ count + " -> ");
            for(int count2 = 0; count2 < graph[count].size(); count2++) {
                System.out.print((int) graph[count].get(count2).rate +":(x'=" + graph[count].get(count2).end + ")");
                if(count2 + 1 < graph[count].size()){
                    System.out.print(" + ");
                }
                else{
                    System.out.println(";");
                }
            }
        }
    }

    public void seedPath(int target){
        int tracker = 0;
        System.out.println("Path from origin to target:");
        while(tracker != target){
            System.out.print(tracker + " -> ");
            tracker = graph[tracker].get((int) (Math.random() * graph[tracker].size())).end;
        }

        System.out.println(target);
    }

    /*
SpecifiedDegreeDistribution Class
Author: Josh Jeppson
To use: create an ArrayList of items and their respective probabilities (must add up to 1)
and will randomly choose based on the uniformly distributed Math.random() according to desired
probabilities.
*/

    /**
     * Simple pair class which contains items of two different types
     */
    class Pair<T, U> {
        public T first;
        public U second;
        public Pair(T f, U s) {
            first = f;
            second = s;
        }
    }

    public class SpecifiedDegreeDistribution<T> {
        /**
         * Constructor
         *
         * @param itemProbabilityPairs Items and probabilities they are chosen
         * */
        public SpecifiedDegreeDistribution(ArrayList<Pair<T, Double>> itemProbabilityPairs) throws Exception {
            this.itemProbabilityPairs = itemProbabilityPairs;
            checkItemProbabilityPairs();
        }

        /**
         * Gets an item
         *
         * @return An item randomly chosen such that it matches the histogram
         * */
        public T get() {
            double r = Math.random();
            ArrayList<Pair<T, Double>> pairs = this.itemProbabilityPairs;
            Double total = 0.00;
            for (Pair<T, Double> pair : pairs) {
                total += pair.second;
                if (total >= r) {
                    return pair.first;
                }
            }
            return pairs.get(pairs.size() - 1).first;
        }

        /**
         * Checks the item probability pairs to make sure that they have valid probabilities
         *
         * Throws an exception if total probability is greater than 1.0
         */
        private void checkItemProbabilityPairs() throws Exception {
            ArrayList<Pair<T, Double>> pairs = this.itemProbabilityPairs;
            Double total = 0.00;
            for (Pair<T, Double> pair : pairs) {
                total += pair.second;
                if (total > 1) {
                    throw new Exception("Total probability should not be greater than 1!");
                }
            }
        }

        ArrayList<Pair<T, Double>> itemProbabilityPairs;
    }

    public static void main(String[] args) throws Exception {
        Scanner input1 = new Scanner(System.in);
        System.out.print("Enter the number of States: ");
        int numberOfStates = input1.nextInt();
        SparseModelGenerator newModel = new SparseModelGenerator(numberOfStates);

        Scanner input2 = new Scanner(System.in);
        System.out.print("Enter the max transition rate: ");
        int transitionRate = input2.nextInt();
        Scanner input3 = new Scanner(System.in);
        System.out.print("Enter the Diogram of Transition probabilities: ");
        String filename = input3.nextLine();
        newModel.makeGraph(transitionRate,filename);

        newModel.printGraph();

        while(true) {
            Scanner input4 = new Scanner(System.in);
            System.out.print("\nEnter the Target State: ");
            int target = input4.nextInt();
	    if(target >= numberOfStates || target < 0){
	    System.out.print("\nOut of Bounds");
	    }
	    else{
            newModel.seedPath(target);
	    }
        }
    }
}

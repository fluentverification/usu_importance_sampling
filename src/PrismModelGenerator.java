import java.util.Scanner;
import java.lang.Math;

public class PrismModelGenerator {
    public static void main(String[] args) {


        Scanner input1 = new Scanner(System.in);
        System.out.print("Enter the number of States: ");
        int numberOfStates = input1.nextInt();
        Scanner input2 = new Scanner(System.in);
        System.out.print("Enter the length of seed path (Length must be less than number of states): ");
        int lengthOfSeedPath = input2.nextInt();
        double maxRate = 10;

        boolean[][] truthArray = new  boolean[numberOfStates][numberOfStates];
        double[][] probArray = new double[numberOfStates][numberOfStates]; //Array of all state probabilities//

        System.out.println("ctmc");
        System.out.println("\n    //This section contains the seed path probabilities//");

        int startState = 0;
        for (int count = 0; count < lengthOfSeedPath; count++) {
            int randomNumber = (int) (numberOfStates * Math.random());
            if(startState != randomNumber) {
                double probRandom = (maxRate * Math.random());
                System.out.printf("    const double R" + startState + "_" + randomNumber + " = " + "%.3f" + ";\n", probRandom);
                truthArray[startState][randomNumber] = true;
                probArray[startState][randomNumber] = probRandom;
            }
            startState = randomNumber;

        }
        System.out.println("    //End of seed path section//\n");

        for (int count = 0; count < numberOfStates; count++) {
            for (int i = 0; i < numberOfStates; i++) {
                if(count != i) {
                    if(!truthArray[count][i]) {
                        if(Math.random() < 0.75) {
                            double probRandom = (maxRate * Math.random());
                            System.out.printf("    const double R" + count + "_" + i + " = " + "%.3f" + ";\n", probRandom);
                            probArray[count][i] = probRandom;
                        }
                        else{
                            System.out.printf("    const double R" + count + "_" + i + " = " + "%.3f" + ";\n", 0.000);
                            probArray[count][i] = 0.000;
                        }
                    }
                }
            }
        }
        System.out.println();

        System.out.println("module M1");
        System.out.println("    x : [0.." + (numberOfStates - 1) + "] init 0;");
        System.out.println();

        for (int count = 0; count < numberOfStates; count++) {
            System.out.print("    [] x= "+ count + " -> ");
            for (int i = 0; i < numberOfStates; i++) {
                if(count != i) {
                    System.out.print("R" + count + "_" + i + ":(x'=" + i + ")");
                    if (i != (numberOfStates - 1)) {
                        System.out.print(" + ");
                    } else {
                        System.out.println(";");
                    }
                }
            }
        }

        System.out.println();
        System.out.printf("endmodule");
        System.out.println();
    }
}


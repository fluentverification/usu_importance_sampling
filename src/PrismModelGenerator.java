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
        Scanner input3 = new Scanner(System.in);
        System.out.print("Enter the connectivity of state space (Percentage): ");
        float connectivity = input3.nextFloat();

        double maxRate = 0.1;

        boolean[][] truthArray = new  boolean[numberOfStates][numberOfStates];
        boolean[] seedPathArray = new boolean[numberOfStates]; //Array of all state probabilities//

        System.out.println("\nctmc");
        System.out.println("\n    //This section contains the seed path probabilities//");

        int startState = 0;

        for (int count = 0; count < lengthOfSeedPath; count++) { //Creates seed path of probabilities//
            int randomNumber = (int) (numberOfStates * Math.random()); //Random state to connect to//
            if(startState != randomNumber && !seedPathArray[randomNumber]) {
                double probRandom = (maxRate * Math.random()); //Probability of the new connection//
                System.out.printf("    const double R" + startState + "_" + randomNumber + " = " + "%.3f" + ";\n", probRandom); //Prints out connection to model//
                truthArray[startState][randomNumber] = true; //Prevents connection from showing up later in the model//
                seedPathArray[randomNumber] = true; //Prevent the seed path from returning to a past state//

                startState = randomNumber; //Sets the end state to the new start state//

                if(!seedPathArray[0]){ //Makes sure that the seed path cannot return to 0 after it has left the state of 0//
                    seedPathArray[0] = true;
                }
            }
            else{ //Causes the seed path to generate until it reaches the desired length//
                count--;
            }
        }
        System.out.println("    //End of seed path section//\n");

        for (int count = 0; count < numberOfStates; count++) {
            for (int i = 0; i < numberOfStates; i++) {
                if(count != i) {
                    if(!truthArray[count][i]) {
                        if(Math.random() < connectivity * 0.01) {
                            double probRandom = (maxRate * Math.random());
                            System.out.printf("    const double R" + count + "_" + i + " = " + "%.3f" + ";\n", probRandom);
                        }
                        else{
                            //System.out.printf("    const double R" + count + "_" + i + " = " + "%.3f" + ";\n", 0.000);
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

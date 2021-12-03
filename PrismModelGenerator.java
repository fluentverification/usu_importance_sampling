import java.util.Scanner;
import java.lang.Math;

public class PrismModelGenerator {
    public static void main(String[] args) {


        Scanner input = new Scanner(System.in);
        System.out.print("Enter the number of States: ");
        int numberOfStates = input.nextInt();
        double maxRate = 10;

        System.out.println("ctmc");
        System.out.println();
        for (int count = 0; count < numberOfStates; count++) {
            for (int i = 0; i < numberOfStates; i++) {
                if(count != i) {
                    System.out.printf("    const double R" + count + "_" + i + " = " + "%.3f" + ";\n", (maxRate * Math.random()));
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

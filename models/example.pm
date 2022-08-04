ctmc
 
// SeedPath: 0,3,4,6,7,8

const double R0_1 = 2;
const double R0_3 = 4;
const double R10_13 =3; 
const double R10_14 = 8;
const double R11_13 = 3;
const double R11_15 = 4;
const double R12_14 = 7;
const double R12_15 = 2;
const double R13_14 = 6;
const double R13_15 = 3;
const double R14_15 = 9;
const double R1_2 = 1;
const double R2_10 = 2;
const double R2_9 = 1;
const double R3_4 = 7;
const double R4_5 = 3;
const double R4_6 = 0.1;
const double R5_16 = 9;
const double R5_17 = 2;
const double R6_7 = 6;
const double R6_8 = 3;
const double R7_8 = 5;
const double R9_11 = 8;
const double R9_12 = 2;




module M1
    x : [0..17] init 0;

    [] x= 0 -> R0_1:(x'=1) +               R0_3:(x'=3)              ;
    [] x= 1 ->               R1_2:(x'=2)                            ;
    [] x= 2 -> R2_9:(x'=9) + R2_10:(x'=10);
    [] x= 3 ->                                           R3_4:(x'=4);
    [] x= 4 -> R4_5:(x'=5) + R4_6:(x'=6);
    [] x= 5 -> R5_16:(x'=16) + R5_17:(x'=17);
    [] x= 6 -> R6_7:(x'=7) + R6_8:(x'=8);
    [] x= 7 -> R7_8:(x'=8); 
    // 8 is a terminal state
    [] x= 9 -> R9_11:(x'=11) + R9_12:(x'=12);
    [] x= 10 -> R10_13:(x'=13) + R10_14:(x'=14);
    [] x= 11 -> R11_13:(x'=13) + R11_15:(x'=15);
    [] x= 12 -> R12_14:(x'=14) + R12_15:(x'=15);
    [] x= 13 -> R13_14:(x'=14) + R13_15:(x'=15);
    [] x= 14 -> R14_15:(x'=15);
    // 15 is a terminal state
    // 16 is a terminal state
    // 17 is a terminal state
    
endmodule

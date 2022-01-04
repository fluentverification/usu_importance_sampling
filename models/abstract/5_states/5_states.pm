ctmc

    const double R0_1 = 5.495;
    const double R0_2 = 8.192;
    const double R0_3 = 0.040;
    const double R0_4 = 7.780;
    const double R1_0 = 0.983;
    const double R1_2 = 0.860;
    const double R1_3 = 9.855;
    const double R1_4 = 5.948;
    const double R2_0 = 8.033;
    const double R2_1 = 6.951;
    const double R2_3 = 0.220;
    const double R2_4 = 8.893;
    const double R3_0 = 1.169;
    const double R3_1 = 0.817;
    const double R3_2 = 1.869;
    const double R3_4 = 0.246;
    const double R4_0 = 6.807;
    const double R4_1 = 3.479;
    const double R4_2 = 0.801;
    const double R4_3 = 8.497;

module M1
    x : [0..4] init 0;

    [] x= 0 -> R0_1:(x'=1) +               R0_3:(x'=3)              ;
    [] x= 1 -> R1_0:(x'=0) + R1_2:(x'=2)                            ;
    [] x= 2 -> R2_0:(x'=0)               + R2_3:(x'=3)              ;
    [] x= 3 ->               R3_1:(x'=1)               + R3_4:(x'=4);
endmodule

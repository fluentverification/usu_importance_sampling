ctmc

const double R1_2 = 1;
const double R1_3 = 1;
const double R2_1 = 1;

module three_state
   x : int init 1;

   [] x=1 -> R1_2:x'=2 + R1_3:x'=3;
   [] x=2 -> R2_3:x'=3 + R2_1:x'=1;

endmodule

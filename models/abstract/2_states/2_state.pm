ctmc

const double R = 1;

module two_state
   x : int init 1;

   [] x=1 -> R:x'=2;

endmodule
ctmc

const double k1=1.0;
const double k2=1.0;
const double k3=1.0;
const double T=0.2;

module birth
  x : [0..3] init 0;

  [] x=0 -> k1:(x'=1); 
  [] x=1 -> k2:(x'=2); 
  [] x=2 -> k3:(x'=3);

endmodule

label "objective"  = x=3;
label "constraint" = true;


   

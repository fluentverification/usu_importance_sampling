ctmc

const double k1=1.0;
const double k2=0.2;
const double k3=0.05;
const double T=1;

const double delta1=1.0;
const double delta2=5.0;
const double delta3=50.0;

const int maxSpecies=100;
const int maxConstraint=100;

module RXN
    a : [0..maxSpecies] init 0;
    b : [0..maxSpecies] init 0;
    c : [0..maxSpecies] init 0;
   
    [r1] (a<maxSpecies) -> k1:(a'=a+1);
    [r2] (b<maxSpecies) -> k2:(b'=b+1);
    [r3] (a>0) & (b>0) & (c<maxSpecies) -> a*b*k3:(c'=c+1) & (b'=b-1) & (a'=a-1);
endmodule

label "objective" = a=0 & b=0 & c=2; 
label "constraint" = a+c<maxConstraint & b+c<maxConstraint;


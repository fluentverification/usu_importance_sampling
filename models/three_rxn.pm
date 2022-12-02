ctmc

const double k1=1.0;
const double k2=1.0;
const double k3=0.001;

const int maxSpecies=10;
const int maxConstraint;

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



//Simple Handshaking model:
//Used to simulate an syncronous handshaking algorithm
//between a sender and a reciever. Model contains 4 signals
//that are used to control and faciliate the communictation 
//bewteen the two devices.
//Model also contains error transistions to simulate possible 
//signals errors (such as a bit change caused by cosmic interferance).
ctmc 
module producer 

valid: bool init false; 

[] (rdy = false) & (valid = true) -> .1:(valid' = false); 

[] (rdy = false) & (valid = false) -> .1:(valid' = true); 

[] (valid = true) -> .000000000001:(valid' = false); 

[] (valid = false) -> .000000000001:(valid' = true);

endmodule 


module consumer 

done: bool init false; 

[] (ack = true) & (done = false) -> .1:(done' = true); 

[] (ack = false) & (done = true) -> .1:(done' = false);

[] (done = true) -> .000000000001:(done' = false);

[] (done = false) -> .000000000001:(done' = true);

endmodule 


module sender 

rdy: bool init false; 

[] (valid = true) & (ack = false) & (rdy = false) -> 1:(rdy' = true); 

[] (rdy = true) & (ack = true) -> 1:(rdy' = false);

[] (rdy = true) -> .000000000001:(rdy' = false);

[] (rdy = false) -> .000000000001:(rdy' = true);

endmodule 


module reciever 

ack: bool init false; 

[] (rdy = true) & (ack = false) & (done = false) -> 1:(ack' = true); 

[] (rdy = false) & (done = true) & (ack = true) -> 1:(ack' = false); 

[] (ack = true) -> .000000000001:(ack' = false);

[] (ack = false) -> .000000000001:(ack' = true);


endmodule 

const int maxConstraint;

label "objective" = done=true;
label "constraint" = (valid) & (ack) & (done) & (!rdy);

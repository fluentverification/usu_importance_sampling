#!/bin/bash

clear

# Pure birth process:
printf "=========================================\n"
printf "  Analyzing Three-Reaction Network in PRISM\n"
printf "=========================================\n"
printf "[Press Return]\n\n"
read t

prism models/three_rxn/three_rxn.pm models/three_rxn/three_rxn.csl

printf "[Press Return]\n\n"
read t

clear

printf "=========================================\n"
printf "  Simulating Three-Reaction Network in PRISM\n"
printf "=========================================\n"
printf "[Press Return]\n\n"
read t

prism -sim -simsamples 100000 models/three_rxn/three_rxn.pm models/three_rxn/three_rxn.csl

printf "[Press Return]\n\n"
read t

clear

printf "===============================================\n"
printf "  Simulating Three-Reaction Network with SSA\n  100,000 runs\n"
printf "================================================\n"
printf "[Press Return]\n\n"
read t

clear

./bin/run.sh simulate --model models/three_rxn/three_rxn.pm --Tmax 1 --Nruns 100000 

printf "[Press Return]\n\n"
read t

clear

printf "===============================================\n"
printf "  Simulating Three-Reaction Network with Modulo-SSA\n  10,000 paths\n  100 samples per successful path\n"
printf "================================================\n"
printf "[Press Return]\n\n"
read t

./bin/run.sh simulate --model models/three_rxn/three_rxn.pm --Tmax 1 --Nruns 10000 --modulo --numModuloSamples 100


printf "[Press Return]\n\n"
read t

clear

printf "===============================================\n"
printf "  Simulating Three-Reaction Network with constrained IS\n  10,000 runs\n"
printf "================================================\n"
printf "[Press Return]\n\n"
read t

./bin/run.sh simulate --model models/three_rxn/three_rxn_constrained.pm --Tmax 1 --Nruns 10000 

printf "[Press Return]\n\n"
read t

clear

printf "===============================================\n"
printf "  Simulating Three-Reaction Network with Constrained-Modulo-SSA\n  1,000 paths\n  100 samples per successful path\n"
printf "================================================\n"
printf "[Press Return]\n\n"
read t

./bin/run.sh simulate --model models/three_rxn/three_rxn_constrained.pm --Tmax 1 --Nruns 1000 --modulo --numModuloSamples 100



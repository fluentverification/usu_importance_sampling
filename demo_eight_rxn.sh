#!/bin/bash

clear

# Pure birth process:
printf "=========================================\n"
printf "  Analyzing Eight-Reaction Network in PRISM\n"
printf "=========================================\n"
printf "[Press Return]\n\n"
read t

# time prism models/eight_rxn/eight_rxn.pm models/eight_rxn/eight_rxn.csl

printf "This simulation does not complete within 24 hours and is skipped.\n\n"
printf "[Press Return]\n\n"
read t

clear

printf "=========================================\n"
printf "  Simulating Eight-Reaction Network in PRISM\n"
printf "=========================================\n"
printf "[Press Return]\n\n"
read t

time prism -sim -simsamples 100000 models/eight_rxn/eight_rxn.pm models/eight_rxn/eight_rxn.csl

printf "[Press Return]\n\n"
read t

clear

printf "===============================================\n"
printf "  Simulating Eight-Reaction Network with SSA\n  100,000 runs\n"
printf "================================================\n"
printf "[Press Return]\n\n"
read t

clear

time ./bin/run.sh simulate --model models/eight_rxn/eight_rxn.pm --Tmax 20 --Nruns 100000 

printf "[Press Return]\n\n"
read t

clear

printf "===============================================\n"
printf "  Simulating Eight-Reaction Network with Modulo-SSA\n  10,000 paths\n  100 samples per successful path\n"
printf "================================================\n"
printf "[Press Return]\n\n"
read t

time ./bin/run.sh simulate --model models/eight_rxn/eight_rxn.pm --Tmax 20 --Nruns 10000 --modulo --numModuloSamples 100


printf "[Press Return]\n\n"
read t

clear

printf "===============================================\n"
printf "  Simulating Eight-Reaction Network with constrained IS\n  10,000 runs\n"
printf "================================================\n"
printf "[Press Return]\n\n"
read t

time ./bin/run.sh simulate --model models/eight_rxn/eight_rxn_constrained.pm --Tmax 20 --Nruns 10000 

printf "[Press Return]\n\n"
read t

clear

printf "===============================================\n"
printf "  Simulating Eight-Reaction Network with Constrained-Modulo-SSA\n  1,000 paths\n  100 samples per successful path\n"
printf "================================================\n"
printf "[Press Return]\n\n"
read t

time ./bin/run.sh simulate --model models/eight_rxn/eight_rxn_constrained.pm --Tmax 20 --Nruns 10000 --modulo --numModuloSamples 100



#!/bin/bash

clear

# Pure birth process:
printf "=========================================\n"
printf "  Analyzing Pure Birth Process in PRISM\n"
printf "=========================================\n"
printf "[Press Return]\n\n"
read t

prism models/birth_process/birth_process.pm models/birth_process/birth_process.csl

printf "[Press Return]\n\n"
read t

clear

printf "=========================================\n"
printf "  Simulating Pure Birth Process in PRISM\n"
printf "=========================================\n"
printf "[Press Return]\n\n"
read t

prism -sim models/birth_process/birth_process.pm models/birth_process/birth_process.csl

printf "[Press Return]\n\n"
read t

clear

printf "===============================================\n"
printf "  Simulating Pure Birth Process with SSA\n  10,000 runs\n"
printf "================================================\n"
printf "[Press Return]\n\n"
read t

clear

./bin/run.sh simulate --model models/birth_process/birth_process.pm --Tmax 0.2 --Nruns 100000 

printf "[Press Return]\n\n"
read t

clear

printf "===============================================\n"
printf "  Simulating Pure Birth Process with Modulo-SSA\n  1,000 paths\n  100 samples per successful path\n"
printf "================================================\n"
printf "[Press Return]\n\n"
read t

./bin/run.sh simulate --model models/birth_process/birth_process.pm --Tmax 0.2 --Nruns 1000 --modulo --numModuloSamples 100 



#!/bin/bash

printf "Available classes:\n"

#i=0
#while read line
#do
#    CLASSES[ $i ] = "$line"
#    (( i++ ))
#done < <(ls -1 classes | sed 's/\(.*\)\..*/\1/')

CLASSES=($(ls -1 classes | sed 's/\(.*\)\..*/\1/'))
i=0
for item in ${CLASSES[*]}
do
    printf "%d  %s\n" $i $item
    (( i++ ))
done

read -p "Which class to run? (Enter number) " sel

export LD_LIBRARY_PATH=/usr/local/prism-src/prism/prism/lib:/usr/local/prism-src/prism/prism/prism/lib
export jlibs="-Djava.library.path=/usr/local/prism-src/prism/prism/lib:/usr/local/prism-src/prism/prism/prism/lib"
export clibs='classes:/usr/local/prism-src/prism/prism:/usr/local/prism-src/prism/prism/classes:/usr/local/prism-src/prism/prism/lib/*:/usr/local/prism-src/prism/prism/prism:/usr/local/prism-src/prism/prism/prism/classes:/usr/local/prism-src/prism/prism/prism/lib/*'

printf "Running %s\n" ${CLASSES[ $sel ] }

java $jlibs -cp $clibs ${CLASSES[ $sel ] }


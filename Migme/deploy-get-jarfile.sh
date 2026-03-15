#!/bin/bash
JARFILEANT=target/artifacts/lib/fusion.jar
JARFILE=( `find . -name \*usion\*.jar` );
if [ ${#JARFILE[*]} -eq 1 ] ; then
    JARFILE=${JARFILE[0]};
else
    JAR_COUNT=${#JARFILE[*]};
    for (( idx = 1; idx <= $JAR_COUNT; idx++ ))
    do
        jar=${JARFILE[`expr $idx - 1`]};
        echo $idx.`basename $jar`;

    done;
    echo Enter the number of the jar to deplay;
    read selection;
    if [ `expr $selection - 1` -gt $JAR_COUNT ]; then
        echo Invalid selection.;
        exit 1;
    else
        JARFILE=${JARFILE[`expr $selection - 1`]};
        echo using $JARFILE;
    fi;
fi;


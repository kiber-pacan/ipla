#!/bin/bash

echo "-------------------------------"
echo "------------BUILDING-----------"
echo "-------------------------------"
mkdir -p buildAllJars | true
y=2

for i in $(seq 20 $END); do
    sh gradlew :fabric:build :fabric:modrinth -Pindex="$y"

    if [ "$y" -eq 6 ]; then
        echo "Skipping neoforge for version 6"
    elif [ "$y" -gt 5 ]; then
        sh gradlew :neoforge:build :neoforge:modrinth  -Pindex="$y"
    else
        sh gradlew :forge:build :forge:modrinth  -Pindex="$y"
    fi

    ((y=y+1))
done



echo "-------------------------------"
echo "--------------DONE-------------"
echo "-------------------------------"

#!/bin/bash

echo "-------------------------------"
echo "------------BUILDING-----------"
echo "-------------------------------"
mkdir -p buildAllJars | true
y=2

for i in $(seq 21 $END); do
    sh gradlew :fabric:build -Pversion_index="$y"

    if [ "$y" -eq 6 ]; then
        echo "Skipping neoforge for version 6"
    elif [ "$y" -gt 5 ]; then
        sh gradlew :neoforge:build -Pversion_index="$y"
    else
        sh gradlew :forge:build -Pversion_index="$y"
    fi

    mv ./*/build/libs/ipla-*-[!c]*-*[[:digit:]].jar "buildAllJars"
    ((y=y+1))
done



echo "-------------------------------"
echo "--------------DONE-------------"
echo "-------------------------------"

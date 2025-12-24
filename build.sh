#!/bin/bash

echo "-------------------------------"
echo "------------BUILDING-----------"
echo "-------------------------------"
mkdir -p buildAllJars | true
y=5

for i in $(seq 13 $END); do
    sh gradlew :fabric:build -Pindex="$y"

    if [ "$y" -eq 6 ]; then
        echo "Skipping neoforge for version 6"
    elif [ "$y" -gt 5 ]; then
        sh gradlew :neoforge:build -Pindex="$y"
    else
        sh gradlew :forge:build -Pindex="$y"
    fi

    mv ./*/build/libs/ipla-*-[!c]*-*[[:digit:]].jar "buildAllJars"
    ((y=y+1))
done



echo "-------------------------------"
echo "--------------DONE-------------"
echo "-------------------------------"

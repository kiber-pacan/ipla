#!/bin/bash

echo "-------------------------------"
echo "------------BUILDING-----------"
echo "-------------------------------"
mkdir -p buildAllJars | true
y=5

for i in $(seq 13 $END); do
    sh gradlew :fabric:build :fabric:modrinth :fabric:curseforge -Pindex="$y"

    if [ "$y" -eq 6 ]; then
        echo "Skipping neoforge for version 6"
    elif [ "$y" -gt 5 ]; then
        sh gradlew :neoforge:build :neoforge:modrinth :neoforge:curseforge  -Pindex="$y"
    else
        sh gradlew :forge:build :forge:modrinth :forge:curseforge  -Pindex="$y"
    fi

    ((y=y+1))
done



echo "-------------------------------"
echo "--------------DONE-------------"
echo "-------------------------------"

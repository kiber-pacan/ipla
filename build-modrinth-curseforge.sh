#!/bin/bash

echo "-------------------------------"
echo "------------BUILDING-----------"
echo "-------------------------------"
mkdir -p buildAllJars | true
y=1

for i in $(seq 17 $END); do
    sh gradlew :fabric:build :fabric:modrinth :fabric:curseforge -Pindex="$y"

    if [ "$y" -gt 5 ]; then
        sh gradlew :neoforge:build :neoforge:modrinth :neoforge:curseforge -Pindex="$y"
    else
        sh gradlew :forge:build :forge:modrinth :forge:curseforge -Pindex="$y"
    fi

    mv ./*/build/libs/cui-*-[!c]*-*[[:digit:]].jar "buildAllJars"
    ((y=y+1))
done



echo "-------------------------------"
echo "--------------DONE-------------"
echo "-------------------------------"

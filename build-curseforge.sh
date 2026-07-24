#!/bin/bash

echo "-------------------------------"
echo "------------BUILDING-----------"
echo "-------------------------------"
mkdir -p buildAllJars | true
y=2

for i in $(seq 20 $END); do
    sh gradlew :fabric:build :fabric:publishCurseForge -Pindex="$y"

    if [ "$y" -gt 5 ]; then
        sh gradlew :neoforge:build :neoforge:publishCurseForge -Pindex="$y"
    else
        sh gradlew :forge:build :forge:publishCurseForge -Pindex="$y"
    fi

    mv ./*/build/libs/cui-*-[!c]*-*[[:digit:]].jar "buildAllJars"
    ((y=y+1))
done



echo "-------------------------------"
echo "--------------DONE-------------"
echo "-------------------------------"

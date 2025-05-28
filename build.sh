#!/bin/bash

echo "-------------------------------"
echo "------------BUILDING-----------"
echo "-------------------------------"
mkdir -p buildAllJars | true
y=2

for i in $(seq 10 $END); do
    sh gradlew build -Pindex="$y"

    mv ./*/build/libs/ipla-*-*-*.jar "buildAllJars"
    ((y=y+1))
done

echo "-------------------------------"
echo "--------------DONE-------------"
echo "-------------------------------"

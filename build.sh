echo "-------------------------------"
echo "------------BUILDING-----------"
echo "-------------------------------"
mkdir -p buildAllJars | true
y=3

for i in $(seq 5 $END); do
    sh gradlew build -Pindex="$y"
    mv ./*/build/libs/ipla-*-*-1.3.jar "buildAllJars"
    ((y=y+1))
done

echo "-------------------------------"
echo "--------------DONE-------------"
echo "-------------------------------"

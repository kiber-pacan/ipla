echo "-------------------------------"
echo "------------BUILDING-----------"
echo "-------------------------------"
mkdir -p buildAllJars | true
y=3

for i in $(seq 5 $END); do
    sh gradlew clean -Pindex="$y"
    sh gradlew build curseforge -Pindex="$y"
    ((y=y+1))
done

echo "-------------------------------"
echo "--------------DONE-------------"
echo "-------------------------------"

VERSION=$1

while [[ -z $VERSION ]]; do
	echo "Specify version string:"
	read VERSION
done

echo Building JAR...

cd ..
./gradlew jar
cd release/
echo

JAR=../build/libs/CircuitSim.jar
NAME=CircuitSim$VERSION
OUT=out

rm -rf $OUT
mkdir $OUT
rm build.log

# Linux
echo Creating Linux release...
cat linux_stub.sh $JAR > $OUT/$NAME

# Mac
echo Creating Mac release
python jar2app.py $JAR $OUT/$NAME -n "CircuitSim $VERSION" -i icon.icns -b com.ra4king.circuitsim -j "-Xmx250M" -v $VERSION -s $VERSION >> build.log

# Windows
echo Creating Windows release
"C:\Program Files (x86)\Launch4j\launch4jc.exe" create_exe.xml >> build.log
mv $OUT/CircuitSim.exe $OUT/$NAME.exe

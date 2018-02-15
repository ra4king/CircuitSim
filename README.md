CircuitSim v1.7.2
=================

Site: https://ra4king.github.io/CircuitSim

Basic circuit simulator with many built-in components. Check out `src/com/ra4king/circuitsim/simulator/components`
for examples of how Components are written. Their GUI counterparts are in `src/com/ra4king/circuitsim/gui/peers`.

Screenshot
![Screenshot](http://i.imgur.com/Oon39Np.png)

Building
--------

The gradle script produces a [multi-release jar][1] to resolve
incompatibilities between JavaFX 8 and 9, so you need to install both
Java 8 and Java 9 to build. For example:

    $ export JAVA_8_HOME=/usr/lib/jvm/java-8-openjdk-amd64/
    $ export JAVA_9_HOME=/usr/lib/jvm/java-9-openjdk-amd64/
	$ ./gradlew jar

[1]: http://openjdk.java.net/jeps/238

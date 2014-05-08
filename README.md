# HisTEI

A Framework add-on for Oxygen XML Editor allowing researchers to transcribe historical documents in TEI. More information on http://www.histei.info/p/home.html.

## Compilation

Compile the project using IntelliJ. Make sure to update the _build.properties_ file with the correct locations of the various modules. 

### JDK 

Preferred JDK is Oracle. Installation: 
```
sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
sudo apt-get install oracle-java7-installer
```

Then make sure `java -version` outputs something along the lines of: 
```
java version "1.7.0_55"
Java(TM) SE Runtime Environment (build 1.7.0_55-b13)
Java HotSpot(TM) 64-Bit Server VM (build 24.55-b03, mixed mode)
```

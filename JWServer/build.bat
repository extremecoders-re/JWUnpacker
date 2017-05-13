@echo off
if exist build rmdir /S /Q build
mkdir build
echo Compiling...
javac -d .\build -cp .\lib\bcel-6.0.jar Server.java Disarm.java
chdir build
echo Creating MANIFEST
echo Class-Path: bcel-6.0.jar >> manifest
echo Main-Class: Server >> manifest
echo Creating jar file...
jar cvfm JWServer.jar manifest Server.class Disarm.class
copy ..\lib\bcel-6.0.jar .
del *.class
del manifest
cd ..
echo Done..
pause
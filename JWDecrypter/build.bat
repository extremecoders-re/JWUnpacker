@echo off
if exist build rmdir /S /Q build
mkdir build
echo Compiling...
javac -d .\build Decrypter.java
mkdir build\jwrapper\crypt
echo Done...
echo *************************************************
echo Now copy the 6 class files dumped by JWServer
echo within build\jwrapper\crypt directory.
echo You can run the decrypter like the following
echo java Decrypter encrypter.jar decrypter.jar
echo *************************************************
pause
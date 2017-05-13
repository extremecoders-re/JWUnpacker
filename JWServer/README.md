# JWServer
JWServer is the counterpart of `JWAgent`.  This recieves class files over the network from the JVMTI agent `JWAgent`. It patches them so as to remove any anti-debugging feature implemented. It also dumps the required class files which would be later used by `JWDecrypter`.

### Compiling
Simply run **build.bat**. Make sure **javac**  is in your path.

### Running
You can run `JWServer` as follows.
```bash
C:\> java -jar JWServer.jar
```
When it is dump, it will also dump 6 class files in the current directory which would be later required by `JWDecrypter`.

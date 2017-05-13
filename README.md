# JWUnpacker

`JWUnpacker` is a set of tools to unpack java-to-exe files converted by [JWrapper][1]. 

This comprises of 3 tools:
 1. **JWAgent** : A [JVM TI][2] agent to hook into the class loading process
 2. **JWServer**: Recieves class files from JWAgent, removes anti-debug and dumps them.
 3. **JWDecrypter**: Decrypts encrypted files.

`JWAgent` and `JWServer` works in tandem. 

### Workflow

First, you would need to get the right command line passed to java.exe. You should be able to find this by using a debugger like OllyDbg, setting a breakpoint on `CreateProcessW`.

The command line would look similar to the following:
```
"C:\Documents and Settings\Administrator\Application Data\JWrapper-FindSpace\JWrapper-Windows32JRE-00029224476-complete\bin\FindSpace.exe" -cp "C:\Documents and Settings\Administrator\Application Data\JWrapper-FindSpace\JWrapper-FindSpace-00029226110-complete\fspace.jar;C:\Documents and Settings\Administrator\Application Data\JWrapper-FindSpace\JWrapper-FindSpace-00029226110-complete\liquidlnf.jar;C:\Documents and Settings\Administrator\Application Data\JWrapper-FindSpace\JWrapper-FindSpace-00029226110-complete\jwcrypt.jar;C:\Documents and Settings\Administrator\Application Data\JWrapper-FindSpace\JWrapper-FindSpace-00029226110-complete\jwrapper_utils.jar;" -Xmx512m -Dcom.ibm.tools.attach.enable=false -XX:+DisableAttachMechanism jwrapper.JWrapper "C:\Documents and Settings\Administrator\Application Data\JWrapper-FindSpace\JWrapper-FindSpace-00029226110-complete\JWLaunchProperties-1494665842828-44"
```

You would first need to run `JWAgent` with slightly modified parameters as shown below:

```
"C:\Documents and Settings\Administrator\Application Data\JWrapper-FindSpace\JWrapper-Windows32JRE-00029224476-complete\bin\FindSpace.exe" -cp "C:\Documents and Settings\Administrator\Application Data\JWrapper-FindSpace\JWrapper-FindSpace-00029226110-complete\fspace.jar;C:\Documents and Settings\Administrator\Application Data\JWrapper-FindSpace\JWrapper-FindSpace-00029226110-complete\liquidlnf.jar;C:\Documents and Settings\Administrator\Application Data\JWrapper-FindSpace\JWrapper-FindSpace-00029226110-complete\jwcrypt.jar;C:\Documents and Settings\Administrator\Application Data\JWrapper-FindSpace\JWrapper-FindSpace-00029226110-complete\jwrapper_utils.jar;" -Xmixed -agentlib:JWAgent jwrapper.JWrapper "C:\Documents and Settings\Administrator\Application Data\JWrapper-FindSpace\JWrapper-FindSpace-00029226110-complete\JWLaunchProperties-1494665842828-44"
```
`JWServer` must be run before invoking `JWAgent`.

[1]: http://www.jwrapper.com/
[2]: https://docs.oracle.com/javase/7/docs/platform/jvmti/jvmti.html
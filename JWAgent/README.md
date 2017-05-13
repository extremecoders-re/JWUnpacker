# JWAgent
`JWAgent` is a JVMTI agent.  It hooks into the class loading process, and sends each class to `JWServer` for removal of anti-debug. The modified class is then loaded. `JWAgent` and `JWServer` works in tandem.

### Compiling
Open *JWAgent.sln* Visual Studio and compile.

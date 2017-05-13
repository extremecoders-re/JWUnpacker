# JWDecrypter
JWrapper offers a feature [JWCrypt][1] that can be used to AES encrypt JAR files and  resources. This tool JWDecrypter can be used to decrypt such files, but before that you would need to obtain the decryption keys. 

### Compiling
Simply run **build.bat**. Make sure **javac**  is in your path. It will create a build directory to hold the compiled files.

### Obtaining the decryption keys
The decryption keys are hardcoded in the class file `JWCrypt_Rijndael`. This file will be dumped by `JWServer`. Decompile the file using a good java decompiler. 

Navigate within the decompiled code and you would find a place with the decryption keys as shown in the below example:

```java
static Rijndael aes(final String s) 
{
	final Rijndael rijndael = new Rijndael();
	if (s.equals("fspace.jar")) 
	{
		rijndael.init(false, new byte[] { -76, -51, 42, 4, -42, 58, -107, -78, 60, 92, 68, 88, -45, 54, -85, -80, -27, 35, 49, -53, 26, -115, -112, -71, 63, -124, -117, 126, -111, -53, 27, -108 });
	 }
	
	if (s.equals("file.png")) 
	{
		rijndael.init(false, new byte[] { -100, 31, 33, -80, 25, 61, -59, 55, -63, 37, 24, -116, -91, 22, -10, -26, -4, 121, 36, -20, 36, -76, -99, 119, -28, -60, -11, 60, 95, -13, 67, -3 });
	}
	return rijndael;
}
```
In this example, there are two decryption keys:

**Key for fspace.jar**
```
-76, -51, 42, 4, -42, 58, -107, -78, 60, 92, 68, 88, -45, 54, -85, -80, -27, 35, 49, -53, 26, -115, -112, -71, 63, -124, -117, 126, -111, -53, 27, -108
```

**Key for file.png**
```
-100, 31, 33, -80, 25, 61, -59, 55, -63, 37, 24, -116, -91, 22, -10, -26, -4, 121, 36, -20, 36, -76, -99, 119, -28, -60, -11, 60, 95, -13, 67, -3
```

Apart from this, you would also need the following files which will also be dumped by `JWServer`.

- JWCrypt_Main.class
- JWCrypt_Rijndael$ByteArrayInputStream.class
- JWCrypt_Rijndael$ByteArrayOutputStream.class
- JWCrypt_Rijndael$Rijndael.class
- JWCrypt_Rijndael.class
- JWCrypt_RijndaelConsts.class

All of this 6 files, must be copied within the `build/jwrapper/crypt` directory.

### Running
You can run `JWDecrypter` as follows.
```bash
C:\> java Decrypter fspace.jar fspace-d.jar
Enter the keybytes: (use comma as delimiter)
-76, -51, 42, 4, -42, 58, -107, -78, 60, 92, 68, 88, -45, 54, -85, -80, -27, 35, 49, -53, 26, -115, -112, -71, 63, -124, -117, 126, -111, -53, 27, -108
```

```bash
C:\> java Decrypter file.png decrypted.png
Enter the keybytes: (use comma as delimiter)
-100, 31, 33, -80, 25, 61, -59, 55, -63, 37, 24, -116, -91, 22, -10, -26, -4, 121, 36, -20, 36, -76, -99, 119, -28, -60, -11, 60, 95, -13, 67, -3
```

[1]: http://www.jwrapper.com/guide-jwcrypt-code-protection.html

import org.apache.bcel.classfile.*;

import java.io.*;

public class Disarm
{
    public static String[] dumpNames = new String[]{"JWCrypt_Main", "JWCrypt_Rijndael$ByteArrayInputStream", "JWCrypt_Rijndael$ByteArrayOutputStream", "JWCrypt_Rijndael$Rijndael", "JWCrypt_Rijndael", "JWCrypt_RijndaelConsts"};
    public static int numDumped = 0;

    public static byte[] disarmClass(byte[] klass_buf) throws IOException
    {
        JavaClass klass;
        boolean isModified = false;
        try
        {
            klass = new ClassParser(new ByteArrayInputStream(klass_buf), "").parse();
        }
        catch (IOException ex)
        {
            System.out.println("[!] Warning: Failed to parse class file.");
            return null;
        }

        String klassName = klass.getClassName();

        for (String name: dumpNames)
        {
            if (klassName.endsWith(name))
            {
                FileOutputStream fos = new FileOutputStream(name+".class");
                fos.write(klass_buf);
                fos.close();
                numDumped++;
            }
        }

        // Check if it is a JWrapper class
        if (!klassName.startsWith("jwrapper.crypt.JWC"))
            System.out.println("[-] Skipping " + klassName + ", Size="+klass_buf.length);
        else
            System.out.println("[+] Inspecting "+klassName+ ", Size="+klass_buf.length);

        // Get the constant pool of the class
        ConstantPool constantPool = klass.getConstantPool();

        System.out.println("[+] Scanning constant pool");
        for (int i = 0; i < constantPool.getLength(); i++)
        {
            Constant constant = constantPool.getConstant(i);

            if (constant instanceof ConstantUtf8)
            {
                String constStr = ((ConstantUtf8) constant).getBytes();

                if (constStr.equalsIgnoreCase("-Dcom.ibm.tools.attach.enable=false"))
                {
                    // JWrapper will not run if this string is absent in the cmd args
                    System.out.println("[+] Disarming -Dcom.ibm.tools.attach.enable=false");

                    //We patch this string to -showversion
                    constantPool.setConstant(i, new ConstantUtf8("-Xmixed"));
                    isModified = true;
                }

                else if (constStr.equalsIgnoreCase("-XX:+DisableAttachMechanism"))
                {
                    // JWrapper will not run if this string is absent in the cmd args
                    System.out.println("[+] Disarming -XX:+DisableAttachMechanism");

                    //We patch this string to -showversion
                    constantPool.setConstant(i, new ConstantUtf8("-Xmixed"));
                    isModified = true;
                }

                else if (constStr.equalsIgnoreCase("-agentlib"))
                {
                    // This string must never be present in the cmd args
                    System.out.println("[+] Disarming -agentlib");

                    //Since we are using -agentlib parameter ourselves, this must be patched too
                    constantPool.setConstant(i, new ConstantUtf8("-nonexistentparam"));
                    isModified = true;
                }
            }
        }

        if (isModified)
        {
            System.out.println("[+] Class has been modified.");
            klass.setConstantPool(constantPool);
            return klass.getBytes();
        }
        System.out.println("[+] Class has not been modified.");
        return null;
    }
}

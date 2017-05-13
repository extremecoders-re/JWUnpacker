import java.io.*;
import java.lang.reflect.Method;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

// JAR
// -76, -51, 42, 4, -42, 58, -107, -78, 60, 92, 68, 88, -45, 54, -85, -80, -27, 35, 49, -53, 26, -115, -112, -71, 63, -124, -117, 126, -111, -53, 27, -108

// PNG
// -100, 31, 33, -80, 25, 61, -59, 55, -63, 37, 24, -116, -91, 22, -10, -26, -4, 121, 36, -20, 36, -76, -99, 119, -28, -60, -11, 60, 95, -13, 67, -3

public class Decrypter
{
    String infilepath;
    String outfilepath;
    byte[] key;

    Decrypter(String ipath, String opath)
    {
        infilepath = ipath;
        outfilepath = opath;
    }

    private void acceptKey()
    {
        System.out.println("Enter the keybytes: (use comma as delimiter)");
        Scanner sc = new Scanner(System.in);
        String line = sc.nextLine();
        String kbytes[] = line.split(",");
        if (kbytes.length != 32)
        {
            System.out.println("Invalid length (must be 32)");
            key = null;
            return;
        }
        key = new byte[32];
        for (int i=0; i < 32; i++)
        {
            key[i] = Byte.parseByte(kbytes[i].trim());
        }
    }

    private void decrypt() throws IOException
    {
        acceptKey();
        if (key == null) return;

        if (infilepath.endsWith(".jar"))
            decryptJar();
        else
            decryptResource();
    }

    public static void main(String[] args) throws IOException
    {
        String infile = args[0];
        String outfile = args[1];
        new Decrypter(infile, outfile).decrypt();
    }

    private byte[] decryptBuf(byte[] buf, byte[] key)
    {
        try
        {
            Class cls = Class.forName("jwrapper.crypt.JWCrypt_Rijndael$Rijndael");
            Object ob = cls.newInstance();
            Method init = cls.getMethod("init", new Class[]{byte[].class});
            init.invoke(ob, new Object[]{key});
            Method decryptCtr = cls.getMethod("decryptCTR", new Class[]{byte[].class, int.class});
            return (byte[]) decryptCtr.invoke(ob, buf, 0);
        }
        catch (Exception ex)
        {
            System.out.println("[!] Exception : " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    private void decryptJar() throws IOException
    {
        System.out.println("Input file: "+infilepath);
        System.out.println("Output file: "+outfilepath);
        System.out.println("File type: JAR");

        RandomAccessFile raf = new RandomAccessFile(infilepath, "r");
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outfilepath));

        while (raf.getFilePointer() < raf.length())
        {
            int entryNameLength = raf.readInt();
            byte[] entryNameBytes = new byte[entryNameLength];
            raf.read(entryNameBytes);
            int payloadSize = raf.readInt();
            String entryName = new String(entryNameBytes);
            if (entryName.startsWith("/")) entryName = entryName.substring(1);
            ZipEntry zipEntry = new ZipEntry(entryName);
            zos.putNextEntry(zipEntry);
            System.out.println("[+] Decrypting " +entryName);
            byte[] payload = new byte[payloadSize];
            raf.read(payload);
            byte[] decrypted = decryptBuf(payload, key);
            if (decrypted == null || decrypted.length == 0)
            {
                zos.closeEntry();
                continue;
            }
            zos.write(decrypted);
            zos.closeEntry();
        }
        zos.close();
    }

    private void decryptResource() throws IOException
    {
        System.out.println("Input file: "+infilepath);
        System.out.println("Output file: "+outfilepath);
        System.out.println("File type: RESOURCE");

        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(infilepath));
        byte[] payload = new byte[bis.available()];
        bis.read(payload);
        bis.close();

        FileOutputStream fos = new FileOutputStream(outfilepath);
        fos.write(decryptBuf(payload, key));
        fos.close();
    }

    private int readInt(InputStream inputStream) throws IOException
    {
        final int read = inputStream.read();
        final int read2 = inputStream.read();
        final int read3 = inputStream.read();
        final int read4 = inputStream.read();
        if ((read | read2 | read3 | read4) < 0)
        {
            throw new EOFException();
        }
        return (read << 24) + (read2 << 16) + (read3 << 8) + (read4);
    }

    private byte readByte(final InputStream inputStream) throws IOException
    {
        final int read = inputStream.read();
        if (read < 0)
        {
            throw new EOFException();
        }
        return (byte) read;
    }
}

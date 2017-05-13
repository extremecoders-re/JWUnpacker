import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server
{
    final int JW_DISARMCLASS = 100;
    final int JW_CLASSNOCHANGE = 101;
    final int JW_CLASSMODIFIED = 102;
    final int JW_QUIT = 103;

    final int PORT = 5555;

    private Socket socket;

    private DataInputStream dis;
    private DataOutputStream dos;

    private boolean initiateConnection()
    {
        try
        {
            ServerSocket servSock = new ServerSocket(PORT);

            System.out.println("[+] JWServer Listening on port " + PORT);
            socket = servSock.accept();
            socket.setKeepAlive(true);
            System.out.println("[+] Connection accepted.");
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        }
        catch (IOException e)
        {
            return false;
        }

        return true;
    }

    private boolean handshake()
    {
        byte[] clientHS = new byte[8];
        try
        {
            dis.readFully(clientHS);
            if (!new String((clientHS)).equals("JW-AGENT"))
            {
                return false;
            }
            dos.writeBytes("JW-SERVER");
        }
        catch (IOException e)
        {
            return false;
        }
        return true;
    }

    public void process() throws IOException
    {
        int cmd;
        while (true)
        {
            cmd = dis.readInt();

            if (cmd == JW_DISARMCLASS)
            {
                int klassSize = dis.readInt();
                byte[] klassData = new byte[klassSize];

                dis.readFully(klassData);
                byte[] disarmedClass = Disarm.disarmClass(klassData);

                // We have already dumped the required classes, can quit
                if (Disarm.numDumped == Disarm.dumpNames.length)
                {
                    System.out.println("[!] All necessary classes have been dumped. Sending QUIT signal.");
                    dos.writeInt(JW_QUIT);
                    break;
                }

                else if (disarmedClass == null)
                {
                    //No change
                    dos.writeInt(JW_CLASSNOCHANGE);
                }
                else
                {
                    // Modified
                    dos.writeInt(JW_CLASSMODIFIED);
                    dos.writeInt(disarmedClass.length);
                    dos.flush();
                    dos.write(disarmedClass);
                }

            }
            else if (cmd == JW_QUIT)
            {
                dos.close();
                dis.close();
                socket.close();
                break;
            }
        }
    }

    public void startServer() throws IOException
    {
        if (!initiateConnection())
        {
            System.out.println("[!] Could not initiate connection.");
            return;
        }

        if (!handshake())
        {
            System.out.println("[+] Handshake failure.");
            return;
        }
        process();
    }

    public static void main(String[] args) throws IOException
    {
        Server server = new Server();
        server.startServer();
    }
}

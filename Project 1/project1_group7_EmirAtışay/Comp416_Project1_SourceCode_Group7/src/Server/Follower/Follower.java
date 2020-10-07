package Server.Follower;

import Server.Master.GameThread;

import java.io.*;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

import static Config.Config.ADDRESSOFMASTER;

public class Follower {

    private ServerSocket serverSocket;

    public static final int DEFAULT_SERVER_PORT = 7755;
    public static final int DEFAULT_MASTER_PORT = 4545;
    public static final int DEFAULT_PORT = 8888;
    public final static String hashValid="CONSISTENCY_CHECK_PASSED";
    public final static String hashInvalid="RETRANSMIT";



    public final static String
//            FILE_TO_RECEIVED = "C:\\Master_GameStates.json";

            FILE_TO_RECEIVED = System.getProperty("user.dir")+"\\master.json";

    public final static int FILE_SIZE = 6022386;

    public static final String DEFAULT_SERVER_ADDRESS = "localhost";
    protected BufferedReader isT;
    protected PrintWriter osT;
    protected int bytesRead;
    protected int current = 0;
    protected FileOutputStream fos;
    protected BufferedOutputStream bos;
    protected InputStream is;

    /**
     *
     * @param port takes the server port to connect
     * @throws IOException for a potential program about input output
     * @throws NoSuchAlgorithmException handles hashing algorithm
     */

    public Follower(int port) throws IOException, NoSuchAlgorithmException {
        try
        {
            System.out.println("Opened up a server socket on " + Inet4Address.getLocalHost());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.err.println("Server.Master.Master.Server class.Constructor exception on oppening a server socket");
        }
        Connect("172.20.41.181",DEFAULT_MASTER_PORT,DEFAULT_SERVER_PORT);
        System.out.println("Enter a command:");
        Scanner scanner= new Scanner(System.in);
        String msg= scanner.nextLine();
        while (msg.compareTo("QUIT")!=0)
        {
            System.out.println(SendForAnswer(msg));
            String verification=hashInvalid;
            while (verification.equals(hashInvalid)){
                verification=acceptFile();
            }
            System.out.println("File accepted");
            System.out.println("Enter a command:");
            msg= scanner.nextLine();
        }
        Disconnect();
    }

    /**
     * connects follower to the master server
     * @param serverAddress ip address of the server
     * @param serverPortFF FTP port number
     * @param serverPortFT TCP port number
     */
    public void Connect(String serverAddress, int serverPortFF, int serverPortFT)
    {
        try
        {
            Socket sFF=new Socket(serverAddress, serverPortFF);
            Socket sFT=new Socket(serverAddress, serverPortFT);
            isT = new BufferedReader(new InputStreamReader(sFT.getInputStream()));
            osT = new PrintWriter(sFT.getOutputStream());
            is = sFF.getInputStream(); // file'ı pipetan çekiyo
            System.out.println("Successfully connected to " + serverAddress + " on port " + serverPortFF);
            System.out.println("Successfully connected to " + serverAddress + " on port " + serverPortFT);
        }
        catch (IOException e)
        {
            //e.printStackTrace();
            System.err.println("Error: no server has been found on " + serverAddress + "/" + serverPortFF);
        }
    }

    /**
     * This part is for the distraction handling in case of Master Server crush. Follower is designed as back up server.
     * However, since it is excluded from the obligations, this function is not used at all.
     */
    private void ListenAndAccept()
    {
        Socket s;
        try
        {
            s = serverSocket.accept();
            System.out.println("A connection was established with a client on the address of " + s.getRemoteSocketAddress());
            System.out.println(s);

            /*
            ServerThread st = new ServerThread(s);
            st.start();
             */
        }

        catch (Exception e)
        {
            e.printStackTrace();
            System.err.println("Server.Master.Master.Server Class.Connection establishment error inside listen and accept function");
        }
    }

    /**
     * This function only send message to the master.
     * @param message
     * @return
     */
    public String SendForAnswer(String message)
    {
        String response = new String();
        osT.println(message);
        osT.flush();
        return response;
    }

    /**
     * takes the file, hashes with MD5 hashing algorithm returns the value
     * @param filename takes the filename/path as the input to hash the target
     * @return the hash value
     * @throws IOException any kind of problem because of Input Output Operations
     * @throws NoSuchAlgorithmException avoiding hashing algorithm problems
     */

    public static String calculateDigestValue(String filename) throws IOException, NoSuchAlgorithmException {

        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        try (InputStream inputStreams = Files.newInputStream(Paths.get(filename));
             DigestInputStream dis = new DigestInputStream(inputStreams, messageDigest))
        {

        }
        byte[] temp = messageDigest.digest();
        String result=temp.toString();

        return result;
    }

    public String acceptFile() throws IOException, NoSuchAlgorithmException {
        byte [] mybytearray  = new byte [FILE_SIZE];
        String line= new String();
        bytesRead = is.read(mybytearray,0,mybytearray.length);
        current = bytesRead;
        fos = new FileOutputStream(FILE_TO_RECEIVED,false);
        bos = new BufferedOutputStream(fos);
        bos.write(mybytearray, 0 , current);
        bos.flush();
        fos.close();
        System.out.println("File " + FILE_TO_RECEIVED + " downloaded (" + current + " bytes read)");
        int followerHash=Files.readString(Paths.get(FILE_TO_RECEIVED)).hashCode();
        int masterHasH= Integer.parseInt(isT.readLine());
        boolean b=(followerHash==masterHasH);
        String verif= new String();
        if (b) verif=hashValid; else verif=hashInvalid;
        osT.println(verif);
        osT.flush();
        return verif;
    }

    /**
     * disconnects follower from the master server by closing input stream, output stream and socket
     */

    public void Disconnect()
    {
        try
        {
            isT.close();
            osT.close();
            is.close();
            System.out.println("ConnectionToServer. Connection Closed");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }



}

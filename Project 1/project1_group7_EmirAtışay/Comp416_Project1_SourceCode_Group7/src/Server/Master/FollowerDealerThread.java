package Server.Master;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;

public class FollowerDealerThread extends Thread {
    //public final static String FILE_TO_SEND = "den-eme.json";  // you may change this
    public final static String hashValid="CONSISTENCY_CHECK_PASSED";
    public final static String hashInvalid="RETRANSMIT";
    protected BufferedReader isT;
    protected PrintWriter osT;
    protected Socket sFF;
    protected Socket sFT;
    protected FileInputStream fis = null;
    protected BufferedInputStream bis = null;
    protected OutputStream os = null;
    protected String directory;
    private String line = new String();

    /**
     * constructor of the follower dealer thread
     * @param sFF socket for FTP
     * @param sFT socket for TCO
     * @param dirToSend directory to send the follower files
     */

    public FollowerDealerThread(Socket sFF, Socket sFT, String dirToSend)
    {
        this.sFF = sFF; this.sFT=sFT; directory=dirToSend;
    }
    public void run()
    {
        try
        {
            isT = new BufferedReader(new InputStreamReader(sFT.getInputStream()));
            osT = new PrintWriter(sFT.getOutputStream());
            os = sFF.getOutputStream();

        }
        catch (IOException e)
        {
            System.err.println("Server.Master.Master.Server Thread. Run. IO error in server thread");
        }

        try
        {
            String verification= new String();
            while (line.compareTo("QUIT") != 0)
            {
                line = isT.readLine();
                System.out.println("Follower: " + sFT.getRemoteSocketAddress() + " sent : " + line);
                verification=sendFile();
                System.out.println(verification);
                while (verification.equals(hashInvalid)){
                    verification=sendFile();
                    System.out.println(verification);
                }
            }
        }
        catch (IOException e)
        {
            line = this.getName(); //reused String line for getting thread name
            System.err.println("Server.Master.Master.Server Thread. Run. IO Error/ Client " + line + " terminated abruptly");
        }

        catch (NullPointerException e)
        {
            line = this.getName(); //reused String line for getting thread name
            System.err.println("Server.Master.Master.Server Thread. Run.Client " + line + " Closed");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally
        {
            try
            {
                Files.walk(Paths.get(directory))
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
                System.out.println("Closing the connection");
                if (isT != null)
                {
                    isT.close();
                    System.err.println(" Socket Input Stream Closed");
                }

                if (osT != null)
                {
                    osT.close();
                    System.err.println("Socket Out Closed");
                }
                if (sFT != null)
                {
                    sFT.close();
                    System.err.println("Socket Closed");
                }

            }
            catch (IOException ie)
            {
                System.err.println("Socket Close Error");
            }
        }//end finally
    }

    /**
     * writes json object to a file with p1name-p2name.json format
     * @param jsonObject json object to be written to the file
     * @param p1name name of the player 1 as a string
     * @param p2name name of the player 2 as a string
     * @throws Exception
     */

    public static void writeJsonTtoFile(JSONObject jsonObject, String p1name, String p2name) throws Exception {

        String fileName = p1name + "-" + p2name + ".json";
        FileWriter file = new FileWriter(fileName);
        file.write(jsonObject.toString());
        file.flush();
        file.close();

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

    /**
     * This functions first creates a temporary file, then writes the game state files of the corresponding follower files to that file.
     * Then, sends that file via the output stream to the follower. Also, from the contents of that temp file, calculates the hash value
     * and sends it througj the TCP channel. This function handles the sync of multiple streams.
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */

    public String sendFile() throws IOException, NoSuchAlgorithmException {


        File tmp = new File("tmp.json");
        String content=new String();
        File dir = new File(directory);
        JSONObject tmpJ= new JSONObject();
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            if (directoryListing.length==0){
                content=tmpJ.toString();
            }else{
                for (File myFile : directoryListing) {
                    content=content+ Files.readString(Paths.get(myFile.getAbsolutePath()), StandardCharsets.US_ASCII);
                }
            }
        }

        Files.write(Paths.get(String.valueOf(tmp)), content.getBytes());
        os.write(content.getBytes(),0,content.length());
        os.flush();
        int hashedValue=content.hashCode();
        osT.println(Integer.toString(hashedValue));
        osT.flush();
        String reply=isT.readLine();
        System.out.println(Integer.toString(hashedValue));
        System.out.println(content);
        System.out.println(content.hashCode());
        tmp.delete();
        return reply;
    }
}

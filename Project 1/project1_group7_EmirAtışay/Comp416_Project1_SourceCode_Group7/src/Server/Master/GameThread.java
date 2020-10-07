package Server.Master;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


import java.util.Timer;
import java.util.TimerTask;


import Client.ConnectionToServer;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import org.json.*;


public class GameThread extends Thread{
    private String[] line1 = new String[2];
    private String[] line2 = new String[2];
    protected BufferedReader is1, is2;
    protected PrintWriter os1, os2;

    protected boolean locked;
    protected ArrayList deck1 = new ArrayList();
    protected ArrayList deck2 = new ArrayList();

    protected int turn;
    protected Socket[] players=new Socket[2];
    protected String p1name,p2name;
    protected int p1score, p2score;
    protected Player p1, p2;
    protected JSONObject state;
    protected MongoDatabase database;
    protected String collectionName= new String();
    protected boolean stateChanged;


    /**
     * constructor of game thread
     * @param playersL list of all players connect to the server
     * @param db instance of mongo database
     */
    public GameThread(LinkedList playersL, MongoDatabase db)
    {
        try {

            this.players[0]= (Socket) playersL.pop();
            this.players[1]= (Socket) playersL.pop();
            this.database=db;
            try
            {
                is1 = new BufferedReader(new InputStreamReader(this.players[0].getInputStream()));
                os1 = new PrintWriter(this.players[0].getOutputStream());
                is2 = new BufferedReader(new InputStreamReader(this.players[1].getInputStream()));
                os2 = new PrintWriter(this.players[1].getOutputStream());
                p1=new Player();
                p2=new Player();
            }
            catch (IOException e)
            {
                System.err.println("Server.Master.Master.Server Thread. Run. IO error in server thread");
            }
        } catch (NullPointerException e)
        {
            System.err.println("Players could not connected to the game");
        }
    }

    /**
     * converts arraylist to string
     * @param deck takes deck as arraylist
     * @return returns deck in string format
     */
    private String arrayToStringConv(ArrayList deck){
        String joinedString = String.join(",", deck);
        joinedString="1-"+joinedString;
        return joinedString;
    }

    /**
     * compare each players cards by reading their lines after seeing command "1". Comparison is made according to the
     * value of the card diamonds, spades, clubs hearts are indifferent
     * @param c1 first player's card, string input
     * @param c2 second player's card, string input
     * @return
     */
    private String[] compareCards(String c1, String c2){
        String res[]=new String[2];
        int a=Integer.parseInt(c1);
        int b=Integer.parseInt(c2);
        int aValue=a%13;
        int bValue=b%13;
        if (aValue > bValue){
            p1score=p1score+1;
            res[0]="3-0";
            res[1]="3-2";
        } else if (aValue < bValue){
            p2score=p2score+1;
            res[0]="3-2";
            res[1]="3-0";
        } else {
            res[0]="3-1";
            res[1]="3-1";
        }
        return res;
    }

    /**
     * initializes the deck deals 26 card to each player
     */
    private void initializeTheDeck(){
        String[] deck =new String[52];
        Arrays.setAll(deck, Integer::toString);
        List<String> deckL = Arrays.asList(deck);
        Collections.shuffle(deckL);
        deckL.toArray(deck);
        for(int i=0; i<26;i++){
            deck1.add(deck[i]);
            deck2.add(deck[i+26]);
        }
    }

    /**
     * updates database according to the current state of the game
     */
    private void updateDB(){
        System.out.println("Updating");

        MongoCollection<Document> collection = database.getCollection(collectionName);
        Document document = new Document("title", "MongoDB")
                .append("Player1 name:", p1name)
                .append("Player2 name:", p2name)
                .append("Number of rounds played:", turn)
                .append("Remaining cards of Player1:", deck1)
                .append("Remaining cards of Player2:", deck2)
                .append("Score of Player1", p1score)
                .append("Score of Player2", p2score);
        collection.insertOne(document);


        FindIterable<Document> iterDoc = collection.find();
        int i = 1;
        // Getting the iterator
        Iterator it = iterDoc.iterator();
        while (it.hasNext()) {
            System.out.println(it.next());
            i++;
        }
                System.out.println("Updated");

    }
    /**
     * The server thread, echos the client until it receives the QUIT string from the client
     */
    public void run()
    {
        try {
            initializeTheDeck();
            line1=is1.readLine().split("-");
            line2=is2.readLine().split("-");
            if (line1[0].equals("0")) this.p1name=line1[1];
            if (line2[0].equals("0")) this.p2name=line2[1];
        } catch (IOException e) {
            e.printStackTrace();
        }
        String dcStr1=arrayToStringConv(deck1);
        String dcStr2=arrayToStringConv(deck2);
        os1.println(dcStr1);
        os1.flush();
        os2.println(dcStr2);
        os2.flush();
        p1score=0;
        p2score=0;
        turn=0;
        p1.setName(p1name);
        p2.setName(p2name);
        stateChanged=true;
        state=new JSONObject();
        String tempPath=p2name+"-"+p1name+".json";
        try {
            this.state=newFileCreator(p1,p2);
            writeJsonTtoFile(state,tempPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.collectionName=this.getName();
        if (database.getCollection(collectionName).equals(null)){
            database.createCollection(this.getName());
        }
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Sync");
                if (stateChanged){
                    System.out.println("Current time: the following files are going to be synchronized");
                    updateDB();
                    stateChanged=false;
                }else{
                    System.out.println("Synchronization done with MongoDB");
                }

            }
        }, 0, 30000);
        try {
            line1=(is1.readLine()).split("-");
            line2=(is2.readLine()).split("-");
        } catch (IOException e) {
            e.printStackTrace();
        }

        while ((line1[0].compareTo("100") != 0) || (line2[0].compareTo("100") != 0))
        {
            if ((line1[0].compareTo("100") == 0) && (line2[0].compareTo("100") != 0)){
                os1.println("4-2");
                os1.flush();
                os2.println("4-0");
                os2.flush();
                break;
            }
            else if ((line1[0].compareTo("100") != 0) && (line2[0].compareTo("100") == 0)){
                os1.println("4-0");
                os1.flush();
                os2.println("4-2");
                os2.flush();
                break;
            }
            else{
                if (turn<=25) {
                    String res[] = this.compareCards(line1[1], line2[1]);
                    deck1.remove(line1[1]);
                    deck2.remove(line2[1]);
                    os1.println(res[0]);
                    os1.flush();
                    os2.println(res[1]);
                    os2.flush();
                    this.updateDB();
                    p1.setNumberofRoundsPlayed(turn);
                    p1.setRemainingCards(deck1);
                    p1.setTotalScore(p1score);
                    p2.setNumberofRoundsPlayed(turn);
                    p2.setRemainingCards(deck2);
                    p2.setTotalScore(p2score);
                    stateChanged = true;
                    try {
                        this.state=newFileCreator(p1,p2);
                        File tempDir= new File(System.getProperty("user.dir"));
                        File[] directoryListing = tempDir.listFiles();
                        if (directoryListing != null) {
                            for (File myFile : directoryListing) {
                                if(myFile.getAbsolutePath().contains("Follower")){
                                    writeJsonTtoFile(state,myFile.getAbsolutePath()+"\\"+tempPath);
                                    System.out.println("Folder for follower is found");
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (turn == 25) {
                        line1[0]="100";
                        line2[0]="100";
                        String finale[] = this.compareCards(line1[1], line2[1]);
                        os1.println(res[0]);
                        os1.flush();
                        os2.println(res[1]);
                        os2.flush();
                        if (p1score > p2score) {
                            os1.println("4-0");
                            os1.flush();
                            os2.println("4-2");
                            os2.flush();
                        } else if (p1score < p2score) {
                            os1.println("4-2");
                            os1.flush();
                            os2.println("4-0");
                            os2.flush();
                        } else {
                            os1.println("4-1");
                            os1.flush();
                            os2.println("4-1");
                            os2.flush();
                        }


                    } else {
                        turn++;
                        try {

                            line1=is1.readLine().split("-");
                            line2=is2.readLine().split("-");

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
        os1.println("4-1");
        os1.flush();
        os2.println("4-1");
        os2.flush();
            try
            {
                Files.deleteIfExists(Paths.get(tempPath));
                System.out.println("Closing the game");
                if ((is1 != null)||(is1 != null))
                {
                    is1.close();
                    is2.close();
                    System.err.println(" Socket Input Stream Closed");
                }
                if ((os1 != null) || (os2 != null))
                {
                    os1.close();
                    os2.close();
                    System.err.println("Socket Out Closed");
                }
                if ((players[0] != null) || players[1] != null)
                {
                    players[0].close();
                    players[0].close();
                    System.err.println("Socket Closed");
                }
            }
            catch (IOException ie)
            {
                System.err.println("Socket Close Error");
            }

    }
    /**
     * writes json object to a file with p1name-p2name.json format
     * @param jsonObject json object to be written to the file
     * @param path where and which name the file should have
     * @throws Exception
     */

    private void writeJsonTtoFile(JSONObject jsonObject, String path) throws Exception {

        FileWriter file = new FileWriter(path);
        file.write(jsonObject.toString());
        file.flush();
        file.close();
    }

    /**
     *
     * @param p1 instant of the player 1
     * @param p2 instant of the player 2
     * @return a json object which holds both player's state in it
     * @throws JSONException
     */
    private JSONObject newFileCreator(Player p1, Player p2) throws JSONException {
        JSONObject tempJsonObject=new JSONObject();
        tempJsonObject.put("Player1",p1);
        tempJsonObject.put("Player2",p2);
        return tempJsonObject;
    }

}



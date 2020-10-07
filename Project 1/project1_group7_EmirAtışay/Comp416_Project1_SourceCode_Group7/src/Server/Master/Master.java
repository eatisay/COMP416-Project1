package Server.Master;// echo server

//import shaded.com.mongodb.util.*;
//import net.sf.mongodb_jdbc_driver.*;



import Server.Follower.Follower;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoDatabase;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

import static Config.Config.*;


public class Master
{
    private ServerSocket serverSocketC;
    private ServerSocket serverSocketFF;
    private ServerSocket serverSocketFT;
    public static final int DEFAULT_SERVER_PORT = 4444;
    public static final int DEFAULT_FOLLOWERSERVERTCP_PORT = 4545;
    public static final int DEFAULT_FOLLOWERSERVERFTP_PORT = 7755;
    protected ArrayList players;
    protected LinkedList waitingPlayers;

    protected MongoClient mongo;
    protected MongoCredential credential;
    protected MongoDatabase database;


    protected ArrayList<GameThread> currentGames;
    protected ArrayList<Socket> currentFollowers;

    /**
     * initializes all required stuff for the entire project
     * @param portC port number of the client
     * @param portFF FTP connection's port number of the follower
     * @param portFT TCP connection's port number of the follower
     */

    public Master(int portC,int portFF,int portFT)
    {
        try
        {
            serverSocketC = new ServerSocket(portC);
            serverSocketFF = new ServerSocket(portFF);
            serverSocketFT = new ServerSocket(portFT);
            System.out.println("Oppened up a server socket on " + Inet4Address.getLocalHost());
            players=new ArrayList();
            waitingPlayers=new LinkedList();
            currentGames=new ArrayList();
            mongo = new MongoClient( ADDRESSOFMASTER , 27017 );
            credential = MongoCredential.createCredential(MONGOUSERNAME, MONGODBNAME, MONGOPW.toCharArray());
            System.out.println("Connected to the database successfully");
            database = mongo.getDatabase(MONGODBNAME);
            System.out.println("Credentials ::"+ credential);
            FollowerListenerThread lt= new FollowerListenerThread(serverSocketFF,serverSocketFT,currentGames,currentFollowers);
            lt.start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.err.println("Server.Master.Master.Server class.Constructor exception on oppening a server socket");
        }
        while (true)
        {
            ListenAndAccept();
            if (!players.isEmpty())
            {System.out.println(players);}
        }
    }

    /**
     * waits until there are two clients. when there are two, opens a game thread and serves them to the game thread.
     */
    private void ListenAndAccept()
    {
        Socket sc;
        try
        {
            sc = serverSocketC.accept();
            System.out.println(sc.getPort());
            System.out.println("A connection was established with a client on the address of " + sc.getRemoteSocketAddress());
            players.add(sc);
            waitingPlayers.push(sc);
            if(waitingPlayers.size()%2==0)
            {
                LinkedList gamersList=new LinkedList();
                gamersList.add(waitingPlayers.pop());
                gamersList.add(waitingPlayers.pop());
                GameThread gt = new GameThread(gamersList, database);
                currentGames.add(gt);
                gt.start();
                System.out.println("Game has been created ");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.err.println("Server.Master.Master.Server Class.Connection establishment error inside listen and accept function");
        }
    }

}


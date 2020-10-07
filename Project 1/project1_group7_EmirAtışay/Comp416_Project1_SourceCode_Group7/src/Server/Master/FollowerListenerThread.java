package Server.Master;

import Server.Follower.Follower;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

public class FollowerListenerThread extends Thread {
    protected ServerSocket serverSocketFF;
    protected ServerSocket serverSocketFT;
    protected ArrayList<GameThread> currentGames;
    protected ArrayList<Socket> followers;

    /**
     * constructor of follower listener thread
     * @param sFF server socket for FTP connection
     * @param sFT server socket for TCP connection
     * @param currentGames all online game threads in an arraylist
     * @param followers all online followers in an arraylist
     */
    public FollowerListenerThread(ServerSocket sFF, ServerSocket sFT,
                                  ArrayList<GameThread> currentGames, ArrayList<Socket> followers){
        this.serverSocketFF=sFF;
        this.serverSocketFT=sFT;
        this.currentGames=currentGames;
        this.followers=followers;
    }
    protected int numberOfListeners;

    public void run()
    {
        numberOfListeners=0;
        while (true)
        {
            ListenAndAccept();
        }
    }

    /**
     * opens socket for upcoming follower, opens a directory for follower and increments the number of followers
     * creates a follower dealer thread for each new Follower
     */
    private void ListenAndAccept()
    {
        Socket sf1;
        Socket sf2;
        try
        {

            sf1 = serverSocketFF.accept();
            sf2 = serverSocketFT.accept();
            System.out.println(sf1.getPort());
            System.out.println(sf2.getPort());
            System.out.println("A connection was established with a follower on the address of " + sf1.getRemoteSocketAddress());
            System.out.println("A connection was established with a follower on the address of " + sf2.getRemoteSocketAddress());
            numberOfListeners++;
            String newdir=System.getProperty("user.dir")+"\\"+"Follower"+numberOfListeners;
            File file = new File(newdir);
            file.mkdirs();
            file.setWritable(true);
            file.setReadable(true);
            FollowerDealerThread fdt= new FollowerDealerThread(sf1,sf2,newdir);
            fdt.start();
        }

        catch (Exception e)
        {
            e.printStackTrace();
            System.err.println("Server.Master.Master.Server Class.Connection establishment error inside listen and accept function");
        }
    }


}

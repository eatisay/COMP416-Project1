package Server;

import Server.Follower.Follower;
import Server.Master.Master;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Main
{
    /**
     * where master server or follower is initialized
     * @param args
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Select the server mode: Master(0) / Follower(1) ");
        String mode = scanner.nextLine();
        switch (mode){
            case("0"):
                Master server = new Master(Master.DEFAULT_SERVER_PORT,Follower.DEFAULT_MASTER_PORT, Follower.DEFAULT_SERVER_PORT);
                break;
            case("1"):
                Follower follower= new Follower(Follower.DEFAULT_PORT);
        }


    }
}
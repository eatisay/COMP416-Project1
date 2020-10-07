package Client;

import java.util.Scanner;

import static Config.Config.ADDRESSOFMASTER;

public class Main
{
    /**
     * provides master and client's synch, clients enter to game from this channel
     * @param args
     */
    public static  void main(String args[])
    {
        ConnectionToServer connectionToServer = new ConnectionToServer(ADDRESSOFMASTER, ConnectionToServer.DEFAULT_SERVER_PORT);
        connectionToServer.Connect();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter name: ");
        String message = scanner.nextLine();
        System.out.println("Game is started, your deck is: " + connectionToServer.sendForInit(message));
        String[] response= new String[2];
        response[0]="1";
        while ((!message.equals("QUIT") && (!response[0].equals("4"))))
        {
            System.out.println("Select your move: ");
            message = scanner.nextLine();
            response=connectionToServer.SendForAnswer(message);
            System.out.println("Response from server: " + response[1]);
        }
        connectionToServer.Disconnect();
    }
}

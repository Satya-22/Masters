/*
         1. Name / Date: satya Yoganand Addala / 05-10-2022

         2. Java version used (java -version), if not the official version for the class: 18.0.2

         3. Precise command-line compilation examples / instructions:
           > javac AsyncJokeServer.java
           > javac AsyncJokeClientAdmin.java
           > javac AsyncJokeClient.java

         4. Precise examples / instructions to run this program:

            In separate shell windows run all the below commands :

                > java AsyncJokeServer.java
                > java AsyncJokeClient.java
                > java AsyncJokeClientAdmin.java

            All acceptable commands are displayed on the various consoles.

         5. Notes:

           * Running this file will toggle between the Jokes/Proverbs.
           * For this the user has to enter the word Joke/Proverb for each toggle.
           * For each statement there is a check of which server the data has to go i.e., Primary or Secondary
           * Haven't changed anything that of the previously submitted JokeAdminClient.java

*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class AsyncJokeAdminClient {
    private static String mode;
    public static void main(String args[]) throws IOException {

        /* servername  used for connection */
        String serverName;
        /* Setting the ServerType to primary by default*/
        boolean serverType = false;
        if (args.length < 1) {
            /* Setting the Severname to localhost */
            serverName = "localhost";
            System.out.println("Satya Yoganand's Client Admin, 1.8.\n");
            System.out.println("Using Server : " + serverName + ", Port: 5050");
        }
        else if(args.length == 1 && args[0].equals("secondary")){
            /* Setting the Severname to localhost */
            serverName = "localhost";
            /* Setting the SeverType to Secondary Server */
            serverType = true;
            System.out.println("<S2> Satya Yoganand's Secondary Client Admin, 1.8.\n");
            System.out.println("<S2> Using Server : " + serverName + ", Port: 5051");
        }
        else {
            serverName = args[0];
        }
        /* Buffered Reader for reading Inputs */
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        /* Variable for Server Mode whether it is a Joke / Proverb */


        do {
            if(!serverType) { /* Checking ServerType */
                System.out.println("Enter Joke/Proverb to Switch Modes, (quit) to end: ");
            }
            else{System.out.println("<S2> Enter Joke/Proverb to Switch Modes, (quit) to end: ");}
            /* This is for sending data Immediately */
            System.out.flush();
            /* Enter the word Joke/Proverb to set the mode for primary/secondary server */
            mode = in.readLine();
            if(!serverType) { /* Checking ServerType */
                System.out.println("Mode in Client Admin : " + mode);
            }
            else{System.out.println("<S2> Mode in Client Admin : " + mode);}
            /* If Entered quit then the process gets stopped or else it will fetch the HostName and HostIp */
            if (mode.indexOf("quit") < 0)
                modeControl(serverType,serverName,mode);
        } while (mode.indexOf("quit") < 0);

    }

    public static void modeControl(boolean sType,String sName, String sMode) {
        /* Creating the Socket Variable */
        Socket sock;
        /* Creating the variable for BufferedReader */
        BufferedReader fromServer;
        /* Creating the variable for PrintStream */
        PrintStream toServer;
        /* Variable to read text from Server */
        String textFromServer;
        try{
            if(!sType){ /* Checking ServerType */
                /* connecting to server @port 5050 and servername */
                sock =new Socket(sName,5050);
            }
            else{
                /* connecting to server @port 5051 and servername */
                sock =new Socket(sName,5051);
            }

            /* Creating Buffered reader to store the inputs from Server */
            fromServer=new BufferedReader(new InputStreamReader(sock.getInputStream()));
            /* Creating PrintStream to send data to the server */
            toServer=new PrintStream(sock.getOutputStream());

            /* Sending Mode to the Server */
            toServer.println(sMode);
            /* sending the server mode to the server immediately */
            toServer.flush();
            /* Reading Input from Server */
            textFromServer=fromServer.readLine();
            /* Condition to check if the text for server is null */
            if(textFromServer !=null) System.out.println(textFromServer);
            /* Closing the socket once the task is done */
            sock.close();
        }catch (IOException x) { /* Catch for capturing the IOException */
            System.out.println("socket error");
            x.printStackTrace();
        }
    }
}

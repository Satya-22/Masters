/*
*         1. Name / Date: satya Yoganand Addala / 22-09-2-22

         2. Java version used (java -version), if not the official version for the class: 17.0.2

         3. Precise command-line compilation examples / instructions:
           > javac JokeServer.java
           > javac JokeClientAdmin.java
           > javac JokeClient.java

         4. Precise examples / instructions to run this program:

            In separate shell windows run all the below commands :

                > java JokeServer.java
                > java JokeClient.java
                > java JokeClientAdmin.java

            All acceptable commands are displayed on the various consoles.

         5. Notes:

           *  Running this file will ask the user to enter a username and send it to Server and after Clicking enter the Server will give Joke or Proverb Accordingly.
           *  Generating Unique Id through UUID.randomUUID() and storing it in Server.
           *  For each statement there is a check of which server the data has to go i.e., Primary or Secondary

*/

import java.io.*;
import java.net.*;
import java.util.UUID;

public class JokeClient {
    public static void main(String args[]) throws IOException {
        //The Server used for connection is a localhost
        String serverName;
        boolean serverType = false;
        if (args.length < 1) {
            /* Setting the Severname to localhost */
            serverName = "localhost";
            System.out.println("Satya Yoganand's Joke Client, 1.8.\n");
            System.out.println("Using Server : " + serverName + ", Port: 4545");
        }
        else if(args.length == 1 && args[0].equals("secondary")){
            /* Setting the Severname to localhost */
            serverName = "localhost";
            /* Setting the SeverType to Secondary Server */
            serverType = true;
            System.out.println("<S2> Satya Yoganand's Secondary Joke Client, 1.8.\n");
            System.out.println("<S2> Using Server : " + serverName + ", Port: 4546");
        }
        else {
            serverName = args[0];
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        if(!serverType) { /* Checking ServerType */
            System.out.println("Enter the User Name, (quit) to end: ");
        }
        else{
            System.out.println("<S2> Enter the User Name, (quit) to end: ");
        }
        System.out.flush();
        String name;
        name = in.readLine();
        while(name.equals("")){
            if(!serverType){
                System.out.println("Please enter a valid name : ");
            }
            else{System.out.println("<S2> Please enter a valid name : ");}

            name = in.readLine();
        }
            try {
                String nextLine;
                /* Generating a random UUID for each client */
                String userId = UUID.randomUUID().toString();

                do {
                    /* Entering the Domain name or IP Address */
                    nextLine = in.readLine();
                    /*If Entered quit then the process gets stopped or else it will Display jokes /proverbs as requested */
                    if (nextLine.indexOf("quit") < 0)
                        displayJokes(serverType,userId, name, serverName);
                } while (nextLine.indexOf("quit") < 0);
                System.out.println("Cancelled by user request.");
            }
        catch (IOException x){ /* Catch for handling the IOException*/
            x.printStackTrace();
        }
    }
    static void displayJokes(boolean sType,String userId,String name,String serverName){
        /* Creating a Socket variable */
         Socket sock;
         /* Creating a BufferedReader variable to send output from client to server */
         BufferedReader from_Server;
         /* Creating a PrintStream variable to receive input from server */
         PrintStream to_Server;
         /* Creating a variable for receiving text from server */
         String textFromServer;
         /* Variable for receiving joke status from server */
         String Joke_Status;

         try{
//             sock = new Socket(serverName,4545);
             if(!sType){
                 //connecting to server @port 4545 and servername
                 sock =new Socket(serverName,4545);
             }
             else{
                 //connecting to server @port 4546 and servername
                 sock =new Socket(serverName,4546);
             }
             /* Fetching the inputs for the server using BufferedReader through inputStream */
             from_Server = new BufferedReader(new InputStreamReader(sock.getInputStream()));
             /* used to send output  from the client using PrintStream */
             to_Server = new PrintStream(sock.getOutputStream());
             /* Sending Username to JokeServer */
             to_Server.println(name);
             /* Sending Unique Userid to JokeServer */
             to_Server.println(userId);
             /* Used for sending data immediately to server */
             to_Server.flush();
             /* Reading data from server */
             textFromServer = from_Server.readLine();
             if(textFromServer != null) System.out.println(textFromServer);
             /* Reading Joke Status from Server */
             Joke_Status = from_Server.readLine();
             if(Joke_Status != null) System.out.println(Joke_Status);
             /* Close the Socket after the Connection is done */
             sock.close();

         } catch (UnknownHostException e) { /* Catch block for handling UnknownHostException */
             throw new RuntimeException(e);
         } catch (IOException e) {          /* Catch block for handling IOException */
             throw new RuntimeException(e);
         }
    }
}
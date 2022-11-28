/*
*         1. Name / Date: satya Yoganand Addala / 05-11-2022

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

           *  Implemented UDP for making a conversation, and I have used port 9999 for this connection.
           *  Also, I have implemented a free port for the addition conversation to happen
           *  Induced a sleep of 40 sec in between the responses
           *  By Executing this file the client will ask the server for a joke/proverb and in between receiving of messages the client will start the addition looper to fetch the result of the entered values.
           *  Generating Unique ID through UUID.randomUUID() and storing it in Server.
           *  At the end of the joke/proverb a cycle completed message will get displayed
           *  Modified the previously implemented JokeClient to accept UDP Connections.
           *  I have taken reference for the next free port from stackoverflow and here is the link : https://stackoverflow.com/questions/2675362/how-to-find-an-available-port
           *  I have used a piece of code that i got referenced from web for converting the response bytes to string and here is the link: https://www.geeksforgeeks.org/working-udp-datagramsockets-java/

*/

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AsyncJokeClient {

    public static  Boolean dataRec = false;
    public static Boolean sumFlag;

    public static void main(String args[]) throws IOException {
        /* Server used is localhost */
        String serverName;
        boolean serverType = false;
        /* Creating port variable */
        int port1;
        if (args.length < 1) {
            /* Setting the Severname to localhost */
            serverName = "localhost";
            System.out.println("Satya Yoganand's Joke Client, 1.8.\n");
            System.out.println("Using Server : " + serverName + ", Port: 9999");
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        ServerSocket s = new ServerSocket(0);
        /* Initializing port to a next available port for the Addition thread udp connections */
        port1 = s.getLocalPort();
        if (!serverType) { /* Checking ServerType */
            System.out.println("Enter the User Name, (quit) to end: ");
        }
        System.out.flush();
        String name;
        name = in.readLine();
        while (name.equals("")) {

            System.out.println("Please enter a valid name : ");
            name = in.readLine();
        }
        try {
            String nextLine;
            /* Generating a random UUID for each client */
            String userId = UUID.randomUUID().toString();

            do {
                System.out.println("Enter the User Name or Enter, (quit) to end:");
                /* Entering the Domain name or IP Address */
                nextLine = in.readLine();
                /*If Entered quit then the process gets stopped or else it will Display jokes /proverbs as requested */
                if (nextLine.indexOf("quit") < 0)
                    displayJokes(userId, name,port1);
            } while (nextLine.indexOf("quit") < 0);
            System.out.println("Cancelled by user request.");
        } catch (IOException x) { /* Catch for handling the IOException*/
            x.printStackTrace();
        }
    }

    static void displayJokes(String userId, String name,int portp1) {

        /* Variable for receiving Joke,Proverb,Joke Cycle Status  and Proverb Cycle Status from server */

        String random_Joke;
        String random_Proverb;
        String proverb_Cycle_Status = "";
        String joke_Cycle_Status = "";

            try {

                /* Creating a datagram socket to establish udp connections */
                DatagramSocket ds = new DatagramSocket();
                /* Fetching the InetAddress */
                InetAddress ip = InetAddress.getLocalHost();
                byte buf[] = null;
                /* Concatinating the userID,Name,Port number and address */
                String inp = userId + "@" + name + "#" + portp1+";"+ip;
                /* Converting it to bytes */
                buf = inp.getBytes();
                /* Creating a datagram packet to send the data to server */
                DatagramPacket DpSend = new DatagramPacket(buf, buf.length, ip, 9999);
                /* sending data to server */
                ds.send(DpSend);

                AsyncJokeClient.sumFlag = true;
                /* Initializing the addition looper thread */
                Addition_looper al = new Addition_looper(portp1,Thread.currentThread());
                /* Creating a thread object */
                Thread t = new Thread(al);
                /* Starting the thread */
                t.start();

                byte[] b1 = new byte[1024];
                /* Datagram Packet to receive the data from server */
                DatagramPacket dp1 = new DatagramPacket(b1, b1.length);
                /* Receiving data from server */
                ds.receive(dp1);

                AsyncJokeClient.sumFlag = false;
                /* Fetching the joke/proverb/cyclestatus and storing them in variables */
                random_Joke = data(b1).toString().split("joke")[0].toString();
                random_Proverb = data(b1).toString().split("joke")[1].split("proverb")[0].toString();
                joke_Cycle_Status = data(b1).toString().split("proverb")[1].split("cycleStatus")[1].toString();
                proverb_Cycle_Status = data(b1).toString().split("proverb")[1].split("cycleStatus")[0].toString();
                /* Making the thread to wait till the addition is completed */
                AsyncJokeClient.dataRec = false;
                /* Waiting the current thread till the Addition looper completes */
                t.join();
                /* Printing the received data on the server */
                if(!(random_Joke.equals("null"))){
                    System.out.println("Joke Received");
                    System.out.println(random_Joke);
                }
                if(!(random_Proverb.equals("null"))){
                    System.out.println("Proverb Received");
                    System.out.println(random_Proverb);
                }
                if(!(joke_Cycle_Status.equals("null"))){

                    System.out.println(joke_Cycle_Status);
                }
                if(!(proverb_Cycle_Status.equals("null"))){

                    System.out.println(proverb_Cycle_Status);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }
    public static StringBuilder data(byte[] b)
    {
        /* Checking null for byte */
        if (b == null)
            /* Return Null */
            return null;
        /* Forming String builder */
        StringBuilder result = new StringBuilder();
        /* Initializing i */
        int i = 0;
        /* Looping till byte is 0 */
        while (b[i] != 0)
        {
            /* Appending result to the String Builder  */
            result.append((char) b[i]);
            /* Incrementing i */
            i++;
        }
        return result;
    }
}
/* Addition looper thread to perform additions */
class Addition_looper extends Thread{
    /* Creating datagramSock object */
    DatagramSocket socket;
    int port;
    byte[] buf;
    byte[] receive;
    String result;
    Thread t;
    /* Initializing Buffered reader  */
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    /* Constructor for accepting port */
    public Addition_looper(int port,Thread t){

        this.port = port;
        this.t = t;
    }
    public void run(){
        try {
            if(socket == null) {
                socket = new DatagramSocket();
            }

            while(AsyncJokeClient.sumFlag) {
                System.out.println("Enter numbers to sum:");
                try {
                    AsyncJokeClient.dataRec = false;
                    /* Accepting inputs from user console */
                    String input = in.readLine();
                    /* fetching InetAddress */
                    InetAddress ip = InetAddress.getLocalHost();
                    /* Converting the received inout to bytes */
                    buf = input.getBytes();
                    /* Creating a Datagram packet to send the digits */
                    DatagramPacket dp = new DatagramPacket(buf, buf.length, ip, port);
                    /* Sending the input to server */
                    socket.send(dp);
                    /* Initializing a byte variable */
                    receive = new byte[1024];
                    /* Datagram packet to receive data from server */
                    DatagramPacket dp1 = new DatagramPacket(receive, receive.length);
                    /* Receiving the data */
                    socket.receive(dp1);
                    /* Converting the data to string */
                    result = AsyncJokeClient.data(receive).toString().trim();
                    /* Printing the result */
                    System.out.println("Result is " + result);
                    AsyncJokeClient.dataRec = true;

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }
}

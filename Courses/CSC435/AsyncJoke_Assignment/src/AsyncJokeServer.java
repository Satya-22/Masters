/*
         1. Name / Date: satya Yoganand Addala / 05-11-2022

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

           *  Implemented Async Joke Server gets started with the udp connection on the port 9999 and for Async Joke client Admin I am using tcp with the port 5050
           *  Implemented an Addition Looper to start processing the addition requests till the joke/proverb response appears.
           *  There is a Unique ID used for each client and based on this i have used HapMap which holds Unique Id and set of Jokes
           *  There is a Joke Indicator Included which checks if the jokes in the list exceeds 4 if it does them it deletes all the four jokes.
           *  There is a Proverb Indicator Included which checks if the jokes in the list exceeds 4 if it does them it deletes all the four Proverbs.
           *  In this I have used random method to shuffle between jokes and a condition to remove duplicates.
           *  Methods RemoveJokes() and RemoveProverbs() are used for removing jokes/proverbs respectively from the list.
           *  Jokes have been taken from  https://www.rd.com/list/funniest-one-liners-you-havent-heard-yet/
           *  I have used a piece of code that I got referenced from web for converting the response bytes to string and here is the link: https://www.geeksforgeeks.org/working-udp-datagramsockets-java/
           *  Modified the previously implemented JokeServer Code to accept the UDP Connections.

*/

import java.io.IOException;
import java.net.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class AsyncJokeServer {
    /* Initializing the mode to Joke by default */
    public static String mode = "joke";
    /* Setting the serverType to primary by default */
    public static boolean serverType = false;

    public static void main(String args[]) throws IOException, InterruptedException {
        /* Initializing servsock to null */
        DatagramSocket ds = new DatagramSocket(9999);

        if (args.length < 1) {         /* Checking for Arguments */
            System.out.println("Satya Yoganand's Joke server starting up...,listening at port 9999.\n");
        }

        /* Creating an Admin Looper Thread */
        AdminLooper AL = new AdminLooper();
        Thread t = new Thread(AL);
        /* Starting the thread it, waiting for administration input */
        t.start();

        Worker worker = new Worker(ds);
        Thread w = new Thread(worker);

        w.start();
    }

    /* Method for converting received byte data to string */
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

class AdminLooper implements Runnable {
    public static boolean adminControlSwitch = true;
    public void run() {                /* Running the Admin listen loop */
        /* Number of requests for OS to queue */
        int q_len = 6;
        int port;

        port = 5050;
        System.out.println("Satya Yoganand's Admin Looper starting up...,listening at port : "+port +".\n");
        /* Creating Socket Variable */
        Socket sock;
        ServerSocket servsock = null;
        try {
            servsock = new ServerSocket(port, q_len);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while (adminControlSwitch) {
            /* Accepting the connections */
            try {
                sock = servsock.accept();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            /* Starting the ModeWorker */
            new ModeWorker(sock).start();
        }
    }
}

class ModeWorker extends Thread {

    /* Creating a Socket Variable */
    Socket sock;
    // Constructor for fetching the Socket
    ModeWorker(Socket s) {
        sock = s;
    }
    public void run() {
        /* Initializing out variable to null and this is used to send the data from Server to Client */
        PrintStream out = null;
        /* Initializing in variable to null and this is used to receive data from client to server */
        BufferedReader in = null;

        try {
            /* Fetching the inputs for the server using BufferedReader through inputStream */
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            /* Used to send information from the server to client through PrintStream */
            out = new PrintStream(sock.getOutputStream());

            try {
                String mode;
                /* Blocking Call - the server code will pauses at this point and starts listening to the input from client */
                /* This will capture the mode if it is a Joke or Proverb */
                mode = in.readLine();

                if(!AsyncJokeServer.serverType) {System.out.println("Switching Mode to  : " + mode );}
                else{System.out.println("<S2> Switching Mode to  : " + mode );}
                if(mode.equalsIgnoreCase("joke")) {
                    /* Setting the mode to Joke */
                    AsyncJokeServer.mode = "Joke";
                    /* Checking the serverType */
                    if(!AsyncJokeServer.serverType){System.out.println("Mode set to Joke ");}
                    else{System.out.println("<S2> Mode set to Joke ");}
                }
                if(mode.equalsIgnoreCase("proverb")){
                    /* Setting the mode to Proverb */
                    AsyncJokeServer.mode = "Proverb";
                    /* Checking the server Type */
                    if(!AsyncJokeServer.serverType) {System.out.println("Mode set to Proverb");}
                    else {System.out.println("<S2> Mode set to Proverb");}
                }
                else {
                    /* Checking the serverType */
                    if(!AsyncJokeServer.serverType){System.out.println("Bad Request");}
                    else {System.out.println("<S2> Bad Request");}
                }
            } catch (IOException x) { /* Handling the IOException if the name variable is not valid */
                if(!AsyncJokeServer.serverType) {System.out.println("Server read error");}
                else{System.out.println("<S2> Server read error");}
                x.printStackTrace();
            }
            sock.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
class Worker extends Thread {

    //Create a Socket Object
    public static Socket sock;
    /* Array List to contain all the Jokes */
    private static ArrayList<String> Jokeslist = new ArrayList<String>();

    /* Array List to contain all the proverbs */
    private static ArrayList<String> Proverbslist = new ArrayList<String>();

    /* Array List to contain all the copies of Jokes that are used */
    private static ArrayList<String> JokeslistCopy = new ArrayList<String>();

    /* Hashmap to map main the conversations of client and server with respect to userId*/
    private static HashMap<String, ArrayList<String>> map = new HashMap<>();

    /* Array List to contain all the copies of Proverbs that are used */
    private  static ArrayList<String> ProverbslistCopy = new ArrayList<String>();

    /* List for holding all the unique userID's*/
    private static LinkedList<String> UUIDList = new LinkedList<>();

    /* Creating a static variable for username */
    private static String user_Name;

    /* This Joke_Indicator will help to keep track of the Jokes completed per cycle*/
    private static int Joke_Indicator = 0;

    /* This Proverb_Indicator will help to keep track of the Procerb completed per cycle*/
    private static int Proverb_Indicator = 0;

    /* Initializing the out variable to null */
    private static PrintStream out = null;

    /* Initializing the in variable to null */
    private static BufferedReader in = null;

    /* Create a Datagram Socket */
    DatagramSocket socket;

    /* Constructor for fetching the Socket */

    public Worker(DatagramSocket ds){
        this.socket = ds;
    }

    public void run() {
        /* Accepting and sending all the Sockets from Client */
        while(true){

        try {

            byte[] receive = new byte[65535];
            /* Creating a datagram packet object and initializing it to null */
            DatagramPacket DpReceive = null;
            /* Fetching the Inet Address */
            InetAddress ip = InetAddress.getLocalHost();

            try {
                String name;
                String userId;
                int port = 0;
                String address = "";
                /* Initializing the new Datagram Packet to receive packets from clients */
                DpReceive = new DatagramPacket(receive, receive.length);
                /* Fetching the packet */
                socket.receive(DpReceive);

                /* Storing User ID  */
                userId = AsyncJokeServer.data(receive).toString().split("@")[0];
                /* Storing Name  */
                name = AsyncJokeServer.data(receive).toString().split("#")[0].split("@")[1];
                /* Storing UDP Port Number  */
                port = Integer.parseInt(AsyncJokeServer.data(receive).toString().split("#")[1].split(";")[0]);
                /* Storing InetAddress  */
                address = AsyncJokeServer.data(receive).toString().split(";")[1];

                /* Printing the obtained details */
                System.out.println("UserID,Name : "+ userId + " "+name);
                System.out.println("Port Number : "+port +", Address :"+address);

                if(!AsyncJokeServer.serverType) { /* Checking Server Type */
                    System.out.println("UUID for the current Session : " + userId);
                    System.out.println("Providing a "+ AsyncJokeServer.mode +" ......");
                }
                else{
                    System.out.println("<S2> UUID for the current Session : " + userId);
                    System.out.println("<S2> Providing a "+ AsyncJokeServer.mode +" ......");
                }
                /* Initiating Addition Looper Thread */
                AdditionLooper Al = new AdditionLooper(port,address);
                Thread t = new Thread(Al);
                /* Starting the thread */
                t.start();
                /* Inducing sleep for 40 seconds */
                TimeUnit.SECONDS.sleep(40);
                /* Invoking RandomSelection method for generating random Jokes/Proverbs */
                randomSelection(userId,name,DpReceive,socket);

            } catch (IOException x) { /* Handling the IOException if the name variable is not valid */
                if(!AsyncJokeServer.serverType){System.out.println("Server read error");}
                else{System.out.println("<S2> Server read error");}
                x.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException ioe) {
            System.out.println(ioe);
        }}
    }

    static void randomSelection(String userId,String name,DatagramPacket dp,DatagramSocket ds) throws IOException {
            user_Name = name;
            String Random_Joke = null;
            String jokeCycle_Status = null;

            String Random_Proverb = null;
            String proverbCycleStatus = null;
            /* Checking for the userId in UUID List */
            if (UUIDList.contains(userId)) {
                if (!AsyncJokeServer.serverType) { /* Checking Server Type */
                    System.out.println("UserId Already Exists !!!!");
                } else {
                    System.out.println("<S2> UserId Already Exists !!!!");
                }

                if (AsyncJokeServer.mode.equalsIgnoreCase("joke")) {
                    String random_Joke = randomJoke();
                    /* Loop for preventing Duplicate Jokes from adding to Hashmap */
                    while (map.get(userId).contains(random_Joke)) {
                        random_Joke = randomJoke();
                    }
                    /* Adding the Joke to Hashmap for respective UserId */
                    map.get(userId).add(random_Joke);
                    int jokesCopyListSize = JokeslistCopy.size();

                    /* Checking for Elements in Hashmap and if they are greater than or equal to 4 then incrementing the Joke_Indicator and if it is
                     *  equal to 4 then remove the four jokes from hashmap and reset the counter  */
                    if (map.get(userId).stream().count() >= 4) {
                        /* Resetting the joke Indicator after every Cycle */
                        Joke_Indicator = 0;
                        for (String joke : map.get(userId).stream().toList()) {
                            if (joke.startsWith("J")) {
                                /* Incrementing Joke Indicator */
                                Joke_Indicator++;
                                if (Joke_Indicator == 4) { /* Checking for four Jokes in the Hashmap arraylist*/
                                    /* Calling RemoveJokes to get the remove count of Jokes */
                                    int rmCount = RemoveJokes(map, userId);
                                    /* Resetting the Joke Indication with the number of jokes removed */
                                    Joke_Indicator = Joke_Indicator - rmCount;

                                }
                            }
                        }
                    }

                    if (!AsyncJokeServer.serverType) { /* Checking Server Type */
                        /* Storing random Joke to Client */

                        Random_Joke = random_Joke;

                        if (jokesCopyListSize == 0) {
                            jokeCycle_Status = "Joke Cycle Completed";
                        }
                    }
                }
                /* Server mode is set to Proverb */
                else if (AsyncJokeServer.mode.equalsIgnoreCase("proverb")) {
                    String random_Proverb = randomProverb();
                    /* Loop for preventing duplicate proverbs from adding to Hashmap */
                    while (map.get(userId).contains(random_Proverb)) {
                        random_Proverb = randomProverb();
                    }
                    /* Adding the Proverb to Hashmap with respect to the UserId*/
                    map.get(userId).add(random_Proverb);
                    int proverbCopyListSize = ProverbslistCopy.size();
                    /* Checking for Elements in Hashmap and if they are greater than or equal to 4 then incrementing the Proverb_Indicator and if it is
                     *  equal to 4 then remove the four Proverbs from hashmap and reset the counter  */
                    if (map.get(userId).stream().count() >= 4) {
                        /* Resetting the joke Indicator after every Cycle */
                        Proverb_Indicator = 0;
                        for (String proverb : map.get(userId).stream().toList()) {
                            if (proverb.startsWith("P")) {
                                /* Incrementing Proverb Indicator */
                                Proverb_Indicator++;
                                if (Proverb_Indicator == 4) { /* checking for 4 proverbs in the arraylist of the hashmap */
                                    /* Calling Remove Proverbs to get the remove count of proverbs */
                                    int rmCount = RemoveProverbs(map, userId);
                                    /* Resetting the Joke Indication with the number of jokes removed */
                                    Proverb_Indicator = Proverb_Indicator - rmCount;
                                }
                            }
                        }
                    }
                    if (!AsyncJokeServer.serverType) { /* Checking Server Type */
                        /* Storing random proverb to Client */
                        Random_Proverb = random_Proverb;
                        if (proverbCopyListSize == 0) {
                            proverbCycleStatus = "Proverb Cycle Completed ...";
                        }
                    }
                }
            } else {
                if (!AsyncJokeServer.serverType) { /* Checking Server Type */
                    System.out.println("Creating new User...");
                } else {
                    System.out.println("<S2> Creating new User...");
                }
                /* Adding the Userid to UUIDList */
                UUIDList.add(userId);
                if (AsyncJokeServer.mode.equalsIgnoreCase("joke")) {
                    String random_Joke = randomJoke();

                    /* Creating a seperate ArrayList for the newly created userId and adding the first joke to Hashmap with the userId */
                    map.put(userId, new ArrayList<>());
                    map.get(userId).add(random_Joke);

                    if (!AsyncJokeServer.serverType) { /* Checking Server Type */
                        Random_Joke = random_Joke;
                    }
                } else if (AsyncJokeServer.mode.equalsIgnoreCase("proverb")) {
                    String random_Proverb = randomProverb();

                    /* Creating a seperate ArrayList for the newly created userId and adding the first proverb to Hashmap with the userId */
                    map.put(userId, new ArrayList<>());
                    map.get(userId).add(random_Proverb);

                    if (!AsyncJokeServer.serverType) {/* Checking Server Type */
                        /* printing random proverb in primary server */
                        Random_Proverb = random_Proverb;
                    }
                }
            }
            /* Creating a byte variable to send the data */
            byte[] buf = null;
            /* Concatinating the data that has to be sent */
            String packetData = Random_Joke +"joke"+ Random_Proverb+ "proverb" + proverbCycleStatus +"cycleStatus"+ jokeCycle_Status;
            /* Converting it to bytes */
            buf = packetData.getBytes();
            /* Creating a datagram packet to send the joke/proverb/cycle status to the client */
            DatagramPacket dp3 = new DatagramPacket(buf, buf.length, dp.getAddress(), dp.getPort());
            /* Sending the data  */
            ds.send(dp3);

    }

    /* Method for removing Jokes when Joke cycle Completes */
    static int RemoveJokes(Map<String,ArrayList<String>> map,String userId){
        int Remove_Count = 0;
        for(String joke : map.get(userId).stream().toList()) {
            if (joke.contains("J")) {           /* Removing Jokes from hashmap for the userId */
                map.get(userId).remove(joke);
                /* Incrementing remove count */
                Remove_Count++;
            }
        }
        /* Returning the count of removed jokes */
        return Remove_Count;
    }

    /* Method for removing Proverbs when Proverb cycle Completes */
    static int RemoveProverbs(Map<String,ArrayList<String>> map,String userId){
        int Remove_Count = 0;
        for(String joke : map.get(userId).stream().toList()) {
            if (joke.contains("P")) {         /* Removing Proverbs from hashmap for the userId */
                map.get(userId).remove(joke);
                /* Incrementing remove count */
                Remove_Count++;
            }
        }
        /* Returning the count of removed proverbs */
        return Remove_Count;
    }

    /* Method for fetching random Joke from the Jokes List */
    static String randomJoke() throws IOException {

        // out = new PrintStream(sock.getOutputStream());
        /* Clearing the JokesList */
        Jokeslist.clear();
        Jokeslist.add("JA "+user_Name + ":"+" What did one DNA say to the other DNA? “Do these genes make me look fat?”");
        Jokeslist.add("JB "+user_Name+ ":" +" My IQ test results came back. They were negative.");
        Jokeslist.add("JC "+user_Name+ ":" +" What do you get when you cross a polar bear with a seal? A polar bear.");
        Jokeslist.add("JD "+user_Name+ ":" +" Why was six afraid of seven? Because seven eight nine.");
        /* Removing Joke Duplicates */
        removeJokeDuplicates();
        /* Initializing the random method */
        Random random = new Random();
        int select = random.nextInt(Jokeslist.size());
        String selectedJoke = Jokeslist.get(select);
        /* Adding selectedjoke to the jokelistCopy */
        JokeslistCopy.add(selectedJoke);
        /* Calculating the size of JokeCopylist */
        int jokesCopyListSize = JokeslistCopy.size();
        if (jokesCopyListSize==4) {
            /* Clearing the JokeCopyList */
            JokeslistCopy.clear();
            if(!AsyncJokeServer.serverType) {
                System.out.println("Joke Cycle has been Completed ...");
            }
            else {System.out.println("<S2> Joke Cycle has been Completed ...");
            }
        }
        /* Returning the selected Joke */
        return selectedJoke;
    }

    /* Method for fetching random proverb from Proverbs List */
    static String randomProverb(){
        /*Clearing the proverb List */
        Proverbslist.clear();
        Proverbslist.add("PA "+user_Name +":"+" Honesty is the best policy.");
        Proverbslist.add("PB "+user_Name +":"+ " Strike while the iron is hot.");
        Proverbslist.add("PC "+user_Name +":"+" Don’t judge a book by its cover.");
        Proverbslist.add("PD "+user_Name +":"+" An apple a day keeps the doctor away.");
        /* Removing the proverb Duplicates */
        removeProverbDuplicates();
        /* Initializing the random method */
        Random random = new Random();
        /* Getting the size of proverbslist */
        int select = random.nextInt(Proverbslist.size());
        String selectedProverb = Proverbslist.get(select);
        /* Adding the selected proverb to proverbs copy list */
        ProverbslistCopy.add(selectedProverb);
        /* Calculating the size of proverb copy list */
        int proverbCopyListSize = ProverbslistCopy.size();
        if (proverbCopyListSize==4) {
            /* Clearing the proverCopylist */
            ProverbslistCopy.clear();
            if(!AsyncJokeServer.serverType) {System.out.println("Proverb Cycle has been Completed ...");}
            else{System.out.println("<S2> Proverb Cycle has been Completed ...");}
        }
        /* Returning the selected proverb */
        return selectedProverb;
    }

    /* Method for removing Joke Duplicates from Copied JokeList */
    static void removeJokeDuplicates(){
        for(String joke : JokeslistCopy){
            if(Jokeslist.contains(joke)){
                Jokeslist.remove(joke);
            }
        }
    }

    /* Method for removing Proverb Duplicates from Copied ProverbList */
    static void removeProverbDuplicates(){
        for(String proverb : ProverbslistCopy){
            if(Proverbslist.contains(proverb)){
                Proverbslist.remove(proverb);
            }

        }
    }

}
/* Addition looper thread to perform sum of the two digits  */
class AdditionLooper extends Thread {
    /* Initializing a datagram Socket */
    DatagramSocket socket;
    int port;
    String address;
    int num1 =0;
    int num2 =0;
    int result = 0;
    String result_String = "";
    byte[] results;
    /* Constructor to receive port and Inet Address */
    AdditionLooper(int port,String address){
        this.port = port;
        this.address = address;
    }
    public void run() {

            try {
                /* Checking if the datagram socket is null */
                if(socket == null) {
                    socket = new DatagramSocket(port);
                }
                System.out.println("Started Addition looper");

                /* Accepting and sending all the packets */

                while(true) {

                    try {
                        byte[] buf = new byte[1024];
                        /* Datagram packet to receive the data from client */
                        DatagramPacket dp = new DatagramPacket(buf,buf.length);
                        /* Receiving data */
                        socket.receive(dp);
                        /* Storing the received data in a string */
                        String receivedData = AsyncJokeServer.data(buf).toString();
                        /* Printing the received data */
                        System.out.println("Printing received Digits : " + receivedData);
                        /* Fetching the digits from the received data */
                        num1 = Integer.parseInt(receivedData.split(" ")[0].trim());
                        num2 = Integer.parseInt(receivedData.split(" ")[1].trim());
                        /* Performing Addition */
                        result = num1 + num2;
                        /* Converting the result to string */
                        result_String = result + "";
                        /* Converting the string to bytes */
                        results = result_String.getBytes();
                        /* Datagram packet to send the data to client */
                        DatagramPacket dp1 = new DatagramPacket(results, results.length, dp.getAddress(), dp.getPort());
                        /* Sending the data to client */
                        socket.send(dp1);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            catch (SocketException e) {
            }
    }
}
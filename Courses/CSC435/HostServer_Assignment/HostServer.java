/**
        1. Name / Date: satya Yoganand Addala / 06-11-2022

        2. Java version used (java -version), if not the official version for the class: 18.0.2

        3. Precise command-line compilation examples / instructions:

        > javac HostServer.java

        4. Precise examples / instructions to run this program:

        In separate shell windows run all the below commands :

        > java HostServer.java

        All acceptable commands are displayed on the various consoles.

TO EXECUTE:

1. Start the HostServer in some shell. >> java HostServer

1. start a web browser and point it to http://localhost:4242. Enter some text and press
the submit button to simulate a state-maintained conversation.

2. start a second web browser, also pointed to http://localhost:4242 and do the same. Note
that the two agents do not interfere with one another.

3. To suggest to an agent that it migrate, enter the string "migrate"
in the text box and submit. The agent will migrate to a new port, but keep its old state.

During migration, stop at each step and view the source of the web page to see how the
server informs the client where it will be going in this stateless environment.

-----------------------------------------------------------------------------------

COMMENTS:

This is a simple framework for hosting agents that can migrate from
one server and port, to another server and port. For the example, the
server is always localhost, but the code would work the same on
different, and multiple, hosts.

State is implemented simply as an integer that is incremented. This represents the state
of some arbitrary conversation.

The example uses a standard, default, HostListener port of 4242.

-----------------------------------------------------------------------------------

DESIGN OVERVIEW

Here is the high-level design, more or less:

HOST SERVER
  Runs on some machine
  Port counter is just a global integer incrememented after each assignment
  Loop:
    Accept connection with a request for hosting
    Spawn an Agent Looper/Listener with the new, unique, port

AGENT LOOPER/LISTENER
  Make an initial state, or accept an existing state if this is a migration
  Get an available port from this host server
  Set the port number back to the client which now knows IP address and port of its
         new home.
  Loop:
    Accept connections from web client(s)
    Spawn an agent worker, and pass it the state and the parent socket blocked in this loop

AGENT WORKER
  If normal interaction, just update the state, and pretend to play the animal game
  (Migration should be decided autonomously by the agent, but we instigate it here with client)
  If Migration:
    Select a new host
    Send server a request for hosting, along with its state
    Get back a new port where it is now already living in its next incarnation
    Send HTML FORM to web client pointing to the new host/port.
    Wake up and kill the Parent AgentLooper/Listener by closing the socket
    Die

WEB CLIENT
  Just a standard web browser pointing to http://localhost:4242 to start.

 -------------------------------------------------------------------------------*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * AgentWorker
 *
 * This Class gets called when the Agentworker.start() method gets called in the Agent Listener class.
 * This class process the users inputs from the browser there are three conditions that we are handling this,
 * Firstly,if the user inputs a string then we process it and send a response saying that the state conversation has been started and incremented by 1 each time the user clicks submit.
 * Secondly,if the user inputs the migrate keyword then the port gets migrated to the new port in our case the next available port and kills off the parent listener,Once the new client uses the new port.
 * Lastly,if the user input is not a valid one then the error response will be send accordingly.
 * */

/* AgentWorker class will gets start when the start method in AgentListener gets called. This Class will work  */
class AgentWorker extends Thread {

    /* Create a Socket Object */
    Socket sock;
    /* Create an AgentHolder Object to maintain Agent State and Socket */
    agentHolder parentAgentHolder;
    /* Create a variable to hold the local port */
    int localPort;

    /* Agent Worker Constructor for fetching the socket port an agentholder */
    AgentWorker (Socket s, int prt, agentHolder ah) {
        sock = s;
        localPort = prt;
        parentAgentHolder = ah;
    }
    public void run() {

        /* Initializing printStream variable */
        PrintStream out = null;
        /* Initializing Buffered Reader variable to fetch the input sent */
        BufferedReader in = null;
        /* Initialize newhost to localhost */
        String NewHost = "localhost";
        /* Initializing the newhostport to 4242 */
        int NewHostMainPort = 4242;
        /* Initialize a variable to hold the input received through buffered reader */
        String buf = "";
        /* Initialize a variable to hold new port */
        int newPort;
        /* Create a new Client socket variable */
        Socket clientSock;
        /* Buffered reader to hold the inputs sent from host server */
        BufferedReader fromHostServer;
        /* Printstream object to sent output to the host server */
        PrintStream toHostServer;

        try {
            /* Creating new printstream to send output  */
            out = new PrintStream(sock.getOutputStream());
            /* Creating new bufferedreader to receive input */
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            /* Reading Input from the Client */
            String inLine = in.readLine();
            /* Initializing a new String Builder to form a html string */
            StringBuilder htmlString = new StringBuilder();

            /* printing client input  */
            System.out.println();
            System.out.println("Request line: " + inLine);

            /* Condition to check if the input has migrate in it  */
            if(inLine.indexOf("migrate") > -1) {

                /* Initialize client socket with local host and using port 4242  redirect on using a new port */
                clientSock = new Socket(NewHost, NewHostMainPort);
                /* Initialize buffered reader to receive input from host server using client socket */
                fromHostServer = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
                /* Initialize a print stream to send data to the host server */
                toHostServer = new PrintStream(clientSock.getOutputStream());
                /* Sending a print message that has state in it */
                toHostServer.println("Please host me. Send my port! [State=" + parentAgentHolder.agentState + "]");
                /* Send data to the host server */
                toHostServer.flush();

                /* Looping through the response received from host server */
                for(;;) {
                    /* Storing the input in a variable and checking for the port string to match and the breaking it.This is to extract the newly migrated port number */
                    buf = fromHostServer.readLine();
                    if(buf.indexOf("[Port=") > -1) {
                        break;
                    }
                }

                /* Breaking the above loop to get the input from hostserver which has port number in it an d it is extracted and stored in a string */
                String tempbuf = buf.substring( buf.indexOf("[Port=")+6, buf.indexOf("]", buf.indexOf("[Port=")) );
                /* Converting the obtained string value to Integer and storing in a new variable */
                newPort = Integer.parseInt(tempbuf);
                /* Printing the new port to the console  */
                System.out.println("newPort is: " + newPort);

                /* Appending the new HTML String with the localhost and the newly obtained port */
                htmlString.append(AgentListener.sendHTMLheader(newPort, NewHost, inLine));
                /* Appending the user message to let him know which port it is getting migrated */
                htmlString.append("<h3>We are migrating to host " + newPort + "</h3> \n");
                /* Appending the user String to check for the page source */
                htmlString.append("<h3>View the source of this page to see how the client is informed of the new location.</h3> \n");
                /* Providing a submit button to the new web page */
                htmlString.append(AgentListener.sendHTMLsubmit());
                /* Printing a message specifying that we are killing parent listening loop */
                System.out.println("Killing parent listening loop.");
                /* Storing the previous port into the Serversocket variable */
                ServerSocket ss = parentAgentHolder.sock;
                /* Closing the old port */
                ss.close();
            }
            /* Condition to check if the input message contains a person string in it */
            else if(inLine.indexOf("person") > -1) {
                /* Incrementing the agent state because the user did not enter migrate key word in the input and this means that the state is being maintained */
                parentAgentHolder.agentState++;
                /* After clicking submit we are forming a html response with the port which is already present */
                htmlString.append(AgentListener.sendHTMLheader(localPort, NewHost, inLine));
                /* Informing user that they are having a conversation */
                htmlString.append("<h3>We are having a conversation with state   " + parentAgentHolder.agentState + "</h3>\n");
                /* Appending the submit button for the user */
                htmlString.append(AgentListener.sendHTMLsubmit());
            } else {
                /* If the request is not a valid request */
                /* Appending the html header with the current port and hostname*/
                htmlString.append(AgentListener.sendHTMLheader(localPort, NewHost, inLine));
                /* Appending the message to the user saying that the request is not a valid one */
                htmlString.append("You have not entered a valid request!\n");
                /* Appending the submit button html into the html string */
                htmlString.append(AgentListener.sendHTMLsubmit());
            }
            /* Sending the generated html response String */
            AgentListener.sendHTMLtoStream(htmlString.toString(), out);
            /* Closing the Socket */
            sock.close();
            /* Catching for any other exceptions that might come into play */
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }

}

/**
 * AgentHolder
 *
 * This Class holds the Socket and State of the current request which are being processed by the Agent Listener.
 * The Constructor is used to fetch the current values of both the sock and state.
 * The Agent State gets updated for each request which it is being resetted to zero but in the Agent Worker the state variable gets incremented by 1.

 * */
class agentHolder {
    /* Create a Socket Object */
    ServerSocket sock;
    /* Creating a variable to hold the state of the conversation */
    int agentState;

    /* Constructor to fetch the current socket  */
    agentHolder(ServerSocket s) { sock = s;}
}

/**
 *  AgentListener
 *
 *  Agent Listener gets called when the start method in AgentListener.start() gets called in the host server class.
 *  This class will listen for the requests and used the port which is sent by the host server and by using string builder it forms a http request by using the defined methods.
 *  And the state variable which is initialized to zero has been assigned to the state variable present in the agent holder class by creating a new agent holder object to maintain state for each request.
 *  Now by using the port and socket it spawns of a new agent worker thread which will process the entered inputs.
*/
class AgentListener extends Thread {
    /* Create a Socket Object */
    Socket sock;
    /* Create a variable to store local port */
    int localPort;

    /* Constructor to fetch the current values of Socket and port */
    AgentListener(Socket As, int prt) {
        sock = As;
        localPort = prt;
    }
    /* Initializing the agent State to zero */
    int agentState = 0;

    /* Run Method will get executed when the start method in hostserver gets called */
    public void run() {
        /* Initializing the buffered reader to store the input */
        BufferedReader in = null;
        /* Initializing the printstream to send data */
        PrintStream out = null;
        /* Initializing the new host to null */
        String NewHost = "localhost";
        System.out.println("In AgentListener Thread");
        try {
            /* Variable to hold the received input */
            String buf;
            /* Creating a new printstream using socket and storing it in a variable */
            out = new PrintStream(sock.getOutputStream());
            /* Creating a new Buffered Reader using socket and storing it in a variable */
            in =  new BufferedReader(new InputStreamReader(sock.getInputStream()));

            /* Reading the received Input */
            buf = in.readLine();

            /* Condition to check if the input received is not null and contains State in it */
            if(buf != null && buf.indexOf("[State=") > -1) {
                /* Fetching the state from the received input */
                String tempbuf = buf.substring(buf.indexOf("[State=")+7, buf.indexOf("]", buf.indexOf("[State=")));
                /* Converting the current state into an integer and storing it in a variable */
                agentState = Integer.parseInt(tempbuf);
                /* printing the state to console */
                System.out.println("agentState is: " + agentState);

            }

            System.out.println(buf);
            /* Creating a new StringBuilder to hold the html response */
            StringBuilder htmlResponse = new StringBuilder();
            /* Generating the html response with the local port and NewHost */
            htmlResponse.append(sendHTMLheader(localPort, NewHost, buf));
            /* Appending the Start message to the String */
            htmlResponse.append("Now in Agent Looper starting Agent Listening Loop\n<br />\n");
            /* Appending the port to the response */
            htmlResponse.append("[Port="+localPort+"]<br/>\n");
            /* Appending the submit button html into the string */
            htmlResponse.append(sendHTMLsubmit());
            /* Displaying the html response  */
            sendHTMLtoStream(htmlResponse.toString(), out);

            /* Initializing a new server socket with the local port */
            ServerSocket servsock = new ServerSocket(localPort,2);
            /* Creating a agentholder with the server socket */
            agentHolder agenthold = new agentHolder(servsock);
            /* Setting the agentstate to agentholder variable which is 0 as the process hasn't been started */
            agenthold.agentState = agentState;

            /* Accepting the connections */
            while(true) {
                sock = servsock.accept();
                /* Printing a message saying that we received a connection  */
                System.out.println("Got a connection to agent at port " + localPort);
                /* Spawning off a new agent worker with socket and local port */
                new AgentWorker(sock, localPort, agenthold).start();
            }

        } catch(IOException ioe) {
            System.out.println("Either connection failed, or just killed listener loop for agent at port " + localPort);
            System.out.println(ioe);
        }
    }
    /* Method to send html header */
    static String sendHTMLheader(int localPort, String NewHost, String inLine) {

        /* Initializing a new String Builder */
        StringBuilder htmlString = new StringBuilder();
        /* Appending all the html tags to the string builder */
        htmlString.append("<html><head> </head><body>\n");
        htmlString.append("<h2>This is for submission to PORT " + localPort + " on " + NewHost + "</h2>\n");
        /* Sending the received input response */
        htmlString.append("<h3>You sent: "+ inLine + "</h3>");
        htmlString.append("\n<form method=\"GET\" action=\"http://" + NewHost +":" + localPort + "\">\n");
        /* Entering an option whether to enter a text or migrate */
        htmlString.append("Enter text or <i>migrate</i>:");
        htmlString.append("\n<input type=\"text\" name=\"person\" size=\"20\" value=\"YourTextInput\" /> <p>\n");

        return htmlString.toString();
    }
    /* Method to send html button along with the html header */
    static String sendHTMLsubmit() {
        return "<input type=\"submit\" value=\"Submit\"" + "</p>\n</form></body></html>\n";
    }

    /* Sending the formed http response to the server by inputting the html string  */
    static void sendHTMLtoStream(String html, PrintStream out) {
        /* Setting the http status request */
        out.println("HTTP/1.1 200 OK");
        /* Setting the content length with the help of length method */
        out.println("Content-Length: " + html.length());
        /* Setting the content type to text/html */
        out.println("Content-Type: text/html");
        out.println("");
        /* Send the http request to server  */
        out.println(html);
    }

}

/**
 *  HostServer
 *
 *  Host Server Class uses port 4242 and creates a new socket connection with it.
 * Then opens the socket to receive requests,Once there is any request then we have initialized the next port to 3000.So that the next request will use the port 3001.Then it spawns off a new Agent worker thread.
 * The Incoming requests will be assigned a new port which is incremented by 1 of the old port
 */
public class HostServer {
    /* Initializing the next port  */
    public static int NextPort = 3000;

    public static void main(String[] a) throws IOException {
        /* Number of requests for OS to queue */
        int q_len = 6;
        /* Initializing the default port to 4242 */
        int port = 4242;
        /* Create a Socket Object */
        Socket sock;
        /* Creating new Sock with the port and qlength */
        ServerSocket servsock = new ServerSocket(port, q_len);
        System.out.println("Elliott/Reagan DIA Master receiver started at port 4242.");
        System.out.println("Connect from 1 to 3 browsers using \"http:\\\\localhost:4242\"\n");
        /* Listening to new requests */
        while(true) {
            /* Incrementing the Next port by 1 */
            NextPort = NextPort + 1;
            /* Opening new socket to accept requests */
            sock = servsock.accept();
            /* Printing the Start message on to the console */
            System.out.println("Starting AgentListener at port " + NextPort);
            /* Spawning a new Agentlistener with the Next port */
            new AgentListener(sock, NextPort).start();
        }

    }
}
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

//Worker Class is used to handle multiple clients connected to a single server
class Worker extends Thread {

    //Create a Socket Object
    Socket sock;
    // Constructor for fetching the Socket
    Worker(Socket s) {
        sock = s;
    }

    public void run() {
        /* PrintStream is used to send information to the Client and BufferedReader is used to get the input from the Client */
        PrintStream out = null;
        BufferedReader in = null;

        try {
            /* Fetching the inputs for the server using BufferedReader through inputStream */
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            /* Used to send information from the server to client through PrintStream */
            out = new PrintStream(sock.getOutputStream());

            try {
                String name;
                /* Blocking Call - the server code will pauses at this point and starts listening to the input from client */
                name = in.readLine();
                System.out.println("Looking up " + name);
                printRemoteAddress(name, out); /* Calling up the method to fetch HostName and IP Address and write it to client */
            } catch (IOException x) { /* Handling the IOException if the name variable is not valid */
                System.out.println("Server read error");
                x.printStackTrace();
            }
           /* Closing the socket connection after the process is done */
               sock.close();
        } catch (IOException ioe) {
            System.out.println(ioe);
        }

    }

    /* Method for printing the Information on the Client */
    static void printRemoteAddress(String name, PrintStream out) {
        try {
            out.println("Looking up " + name + "....");
            InetAddress machine = InetAddress.getByName(name);
            out.println("Host Name : " + machine.getHostName());
            out.println("Host IP : " + toText(machine.getAddress()));
        } catch (UnknownHostException ex) {
            out.println("Failed oin attempt to look up " + name);
        }

    }

    /* Method used for converting Byte to String and it is used for fetching Host Ip */
    static String toText(byte ip[]) {
        StringBuffer result = new StringBuffer();

        for(int i = 0; i < ip.length; ++i) {
            if (i > 0) {
                result.append(".");
            }
            result.append(0xff & ip[i]);
        }
        return result.toString();
    }
}


public class InetServer {

    public static void main(String[] var0) throws IOException {
        int q_len = 6;   /* Allowed number of connections from client to server at the same time */
        int port = 1565; /* Port used for making connection */
        Socket sock;
        ServerSocket servsock = new ServerSocket(port, q_len); /* The ServerSocket will take the portNumber and queue length on which it is going to communicate */
        System.out.println("Satya Yoganand's Inet server 1.8 starting up,listening at port 1565.\n");

        while(true) {
            sock = servsock.accept(); /* This is an accept method for listening from the client and is also a blocking call */
            new Worker(sock).start(); /* This Start method will invoke the run method in Worker class */
        }
    }
}
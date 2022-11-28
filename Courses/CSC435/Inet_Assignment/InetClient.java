import java.io.*;
import java.net.*;
public class InetClient {
    public static void main(String args[]) {
        //The Server used for connection is a localhost
        String serverName;
        if (args.length < 1) serverName = "localhost";
        else serverName = args[0];

        System.out.println("Satya Yoganand's Inet Client, 1.8.\n");
        System.out.println("Using Server : " + serverName + ", Port: 1565");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try {
            String name;
            do {
                System.out.println("Enter a hostname or an IP address, (quit) to end: ");
                System.out.flush();
                //Entering the Domain name or IP Address
                name = in.readLine();
                //If Entered quit then the process gets stopped or else it will fetch the HostName and HostIp
                if (name.indexOf("quit") < 0)
                    getRemoteAddress(name, serverName);
            } while (name.indexOf("quit") < 0);
            System.out.println("Cancelled by user request.");
        } catch (IOException x) {
            x.printStackTrace();
        }
    }

    //Method for Converting Byte to String but it is not being used execution
    static  String toText(byte ip[]){
        StringBuffer result = new StringBuffer();
        for(int i = 0;i < ip.length; ++i){
            if(i > 0) result.append(".");
            result.append(0xff & ip[i]);
        }
        return result.toString();
    }

    // Method used for fetching HostAddress and HostIp from the server and printing it on client
    static void getRemoteAddress(String name,String serverName){
        Socket sock;
        BufferedReader fromServer;
        PrintStream toServer;
        String textFromServer;

        try{
            sock = new Socket(serverName,1565);
            //Fetching the input Stream from Server and storing ot on from Server Variable
            fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            //Writing the outputStream from client to server
            toServer = new PrintStream(sock.getOutputStream());
            toServer.println(name);
            //Flush helps to send small bits of data
            toServer.flush();

            for(int i = 1;i<=3;i++){
                textFromServer = fromServer.readLine();
                if(textFromServer != null) System.out.println(textFromServer);
            }
            //Closing the Socket after the process is done
            sock.close();
            //Handling the IOException for socket
        }  catch(IOException x){
            System.out.println("Socket error. ");
            x.printStackTrace();
        }
    }
}
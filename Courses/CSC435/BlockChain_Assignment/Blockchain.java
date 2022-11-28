/*
         1. Name / Date: satya Yoganand Addala / 26-10-2022

         2. Java version used (java -version), if not the official version for the class: 18.0.2

         3. Precise command-line compilation examples / instructions:

           > javac Blockchain.java

         4. Files Used for this process :

           > BlockInput0.txt
           > BlockInput1.txt
           > BlockInput2.txt

         5. Precise examples / instructions to run this program:

            In separate shell windows run all the below commands :

                > java -cp ".:gson-2.8.2.jar" Blockchain.java 0
                > java -cp ".:gson-2.8.2.jar" Blockchain.java 1
                > java -cp ".:gson-2.8.2.jar" Blockchain.java 2


            All acceptable commands are displayed on the various consoles.

         6. External Libraries Used :   GSON Jar- gson-2.8.2.jar

         7. Referenced Programs from Clark Elliott :

            > Blockchain utilities sample program Version J
            > Blockchain input utilty program,
            > Sample Work Program
            > Process Coordination

         8. Notes:

           *  The Blockchain.java file holds various classes and methods that helps to execute the BlockChain
           *  This file runs three processes in parallel where are currently i am using three input files namely BlockInput0.txt,BlockInput1.txt,BlockInput2.txt.
           *  Now the process ID will take by the process from commandline and sets up all the required ports with respect to processID.
           *  Then all the process waits for process 2 to run in which once the process2 Starts running it will multicast a start message to all the running processes.
           *  Then the other process starts to fetch all the three processes public keys and it will try to read there Input files based on their processID's.
           *  Once it has been done then all the records present in the files are converted into unverified blocks and are added to the priority Queue for further processing.
           *  All the Unverified blocks will be shared among all the three processes.
           *  Then the Work will come into play which induces a simple puzzle to solve and helps to verify the unverified blocks and add them into a blockchain.
           *  Now at the end of the process user will be given an opportunity to analyze the statistics of the processes by giving an user Input which inturn will give the processes records and credits for all the three processes.


         9. Extra Notes :

           The sleep time has been incremented to 10000 milliseconds because when trying to start the second process the first process is not able to pick the pub,ic key which has been settled using increased sleep time

         10.Referenced Web Sources :

           https://mkyong.com/java/how-to-parse-json-with-gson/
           http://www.java2s.com/Code/Java/Security/SignatureSignAndVerify.htm
           https://www.mkyong.com/java/java-digital-signatures-example/
           https://javadigest.wordpress.com/2012/08/26/rsa-encryption-example/
           https://www.programcreek.com/java-api-examples/index.php?api=java.security.SecureRandom
           https://www.mkyong.com/java/java-sha-hashing-example/
           https://stackoverflow.com/questions/19818550/java-retrieve-the-actual-value-of-the-public-key-from-the-keypair-object
           https://www.java67.com/2014/10/how-to-pad-numbers-with-leading-zeroes-in-Java-example.html
           https://www.javacodegeeks.com/2013/07/java-priority-queue-priorityqueue-example.html


*/

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;


public class Blockchain {

    /* ServerName Set to Localhost */
    static String serverName = "localhost";

    /* Total Processes that are being used */
    static int numProcesses = 3;

    /* Initialize Process ID */
    static int PID = 0;

    /* BlockList to hold records of unverified blocks */
    static List<BlockRecord> blockArr = new ArrayList<BlockRecord>();
    /* Variable to read the file name that is picked with respect to pid */
    private static String fileName;

    /* Initializing an alpha numeric string for creating a random seed  */
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    /* BlockList to hold records of all the verified blocks */
    public static LinkedList<BlockRecord> blockChain = new LinkedList<>();
    /* List to hold all the publicKeys with the list Object as PublicKeyObject where the public key and processID has been stored */
    public static final List<PublicKeyObject> publicKeyArray = new ArrayList<>();
    /* Variable to store private key */
    public static PrivateKey privateKey;
    /* Start variable to begin the process and is set to default as wait */
    public static String start = "wait";
    /* Setting  first name Index*/
    private static final int fName_Index = 0;
    /* Setting last name Index */
    private static final int lName_Index = 1;
    /* Setting DateOfBirth Index */
    private static final int dOB_Index = 2;
    /* Setting SSN Index */
    private static final int iSSNUM_Index = 3;
    /* Setting Diag Index */
    private static final int iDIAG_Index = 4;
    /* Setting Treatment Index */
    private static final int treatment_Index = 5;
    /* Setting iRX Index */
    private static final int iRX = 6;

    /* Creating a publicKeyObject of the type PublicKeyObject to use it in the process */
    public static PublicKeyObject publicKeyObj = new PublicKeyObject();
    public static Comparator<BlockRecord> blockTSComparator = new Comparator<BlockRecord>()
    {
        static Queue<BlockRecord> blockPriorityQueue = new PriorityQueue<>(4, blockTSComparator);
        /* Overriding the default compare method to do the compare task with respect to timestamps */
        @Override
        public int compare(BlockRecord r1, BlockRecord r2)
        {
            /* Fetching the time stamps for each block to compare against time */
            String t1 = r1.getTimeStamp();
            String t2 = r2.getTimeStamp();
            /* If both time stamps are equal then return 0  */
            if (t1 == t2) {return 0;}
            /* If either t1 or t2 are null then return -1 0r 1 respectively */
            if (t1 == null) {return -1;}
            if (t2 == null) {return 1;}
            /* return the standard compare method with respect to the timestamps */
            return t1.compareTo(t2);
        }
    };
    /* Setting up the Priority Queue */
    static Queue<BlockRecord> blockPriorityQueue = new PriorityQueue<>(4, blockTSComparator);

    /* Hashing the whole block to set the winning hash */
    public static String hashBlock(String blockContents){
        /* Setting up the hash Algorithm to SHA-256 */
        String hashAlgorithm = "SHA-256";
        String SHA256String = "";

        try{
            /* Message Digest Class has been used to get an instance of hash Algorithm */
            MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
            md.update (blockContents.getBytes());
            /* Converting the contents to bytes */
            byte byteData[] = md.digest();

            /* Initializing a new String Buffer Class which is thread safe */
            StringBuffer sb = new StringBuffer();
            /* Looping on the hash bytes */
            for (int i = 0; i < byteData.length; i++) {
                /* Appending the hash values into the string buffer */
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }
            /* Converting appended string buffer hex value to String */
            SHA256String = sb.toString();

        } catch (NoSuchAlgorithmException x) {
            /* Printing Stack Trace */
            x.printStackTrace();
        }
        /* Returning the Converted String */
        return SHA256String.toUpperCase();

    }
    /* Method for creating the Initial block with dummy data */
    public static LinkedList<BlockRecord> blockChainInit(){
        /* Initializing a linked list to hold the blockrecords */
        LinkedList<BlockRecord> blockRecord = new LinkedList<>();
        /* Initializing first block */
        BlockRecord block0 = new BlockRecord();
        /* Generating the UniqueId with random UUID method and converting it to string */
        String unique_ID = UUID.randomUUID().toString();
        /* Setting the blockID for block0 with uniqueId that has been generated */
        block0.setBlockID(unique_ID);
        /* Setting the block number to initial block with is 0 */
        block0.setBlockNum(0);
        /* Making the thread to sleep for 1001 milliseconds */
        try{
            Thread.sleep(1001);
        }
        catch(InterruptedException ex){
            /* Printing Stack Trace */
            ex.getStackTrace();
        }
        /* Initializing the date Variable */
        Date date = new Date();
        /* Generating the time stamp with respect to the date variable, and it generates something like year-month-date.hour:minutes:seconds */
        String timeStamp = String.format("%1$s %2$tF.%2$tT", "", date);
        /* Concatinating  the ProcessID with the generated timestamp*/
        String timeStampString = timeStamp + "." + Blockchain.PID;
        /* Setting the TimeStamp to block0 */
        block0.setTimeStamp(timeStampString);
        /* Setting the verification ProcessID to 0*/
        block0.setVerificationProcessID("0");
        /* Setting the previous hash to a random value */
        block0.setPreviousHash("0000");
        /* Setting the FirstName,LastName and DateOfBirth to match my details */
        block0.setFname("Satya Yoganand"); block0.setLname("Addala"); block0.setDOB("05-22-1998");
        /* Setting some dummy SSN Number */
        block0.setSSNum("123-45-6789");
        /* Setting the diag to Blockchain Initial Block */
        block0.setDiag("Blockchain ");
        /* Setting RX to Blockchain pills */
        block0.setRx("Blockchain");
        /* Setting Treat to Writing more blockChain code  */
        block0.setTreat("BlockChain");
        /* Setting some random seed */
        block0.setRandomSeed("03L810QS");
        /* Combining all the block record data into a single string */
        String combinedData = block0.getTimeStamp() + block0.getBlockNum() + block0.getBlockID() + block0.getSignedID() + block0.getPreviousHash() + block0.getFname() + block0.getLname() + block0.getDOB() + block0.getSSNum() + block0.getVerificationProcessID() + block0.getDiag() + block0.getTreat() + block0.getRx() + block0.getTimeStamp();
        /* Generating the block data with the help of random seed */
        String blockData = combinedData + "03L810QS";
        /* Setting the Winning hash with the block data */
        block0.setWinningHash(hashBlock(blockData));
        /* Adding this block0 to blockRecord */
        blockRecord.add(block0);
        System.out.println("Collected Data from Initial Block ........");
        return blockRecord;
    }
    /* Method for generating public keys for each process */
    public static PublicKeyObject publicKeyInit(int pid) throws Exception {

        /* Initializing the random variable with the help of Random method */
        Random random = new Random();
        long randomNum = random.nextInt(1000);
        /* Generating the Key Value Pair with the help of randomNum */
        KeyPair keyPair = generateKeyPair(randomNum);
        /* Fetching the private key through the generated KeyPair  */
        privateKey = keyPair.getPrivate();
        System.out.println("Key Pair : "+ keyPair);
        /* Fetch the public key from the key value pair,and store it in the bytePublicKey variable of the type byte array */
        byte[] bytePublicKey = keyPair.getPublic().getEncoded();
        /* By using base64 encoding convert the public key which is in byte array to a string */
        String stringKey = Base64.getEncoder().encodeToString(bytePublicKey);
        /* Set the converted string to the public key variable in publicKeyObj Class */
        publicKeyObj.setPublicKey(stringKey);
        /* Set the ProcessId of the process to ProcessId variable in publicKeyObj Class */
        publicKeyObj.setProcessID(pid);
        System.out.println("Printing Public Key : "+publicKeyObj);
        /* Returning the publickey object */
        return publicKeyObj;
    }

    public static boolean verifySig(byte[] data, PublicKey key, byte[] sig) throws Exception {
        /* Getting an Instance of Signature Class and using SHA1withRSA Algorithm  */
        Signature signer = Signature.getInstance("SHA1withRSA");
        /* Initializing the data to get signed with the help of public key */
        signer.initVerify(key);
        signer.update(data);
        /* Returning the status of verifying  */
        return (signer.verify(sig));
    }

    /* Method for generating public and private keys */
    public static KeyPair generateKeyPair(long seed) throws Exception {
        /* Fetching a new Instance of KeyPairGenerator and using RSA Algorithm */
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        /* Fetching a new instance of SHA1PRNG to generate random values */
        SecureRandom rng = SecureRandom.getInstance("SHA1PRNG", "SUN");
        /* Setting the random seed */
        rng.setSeed(seed);
        /* Initializing the key generator with random seed */
        keyGenerator.initialize(1024, rng);
        /* Returning the generated Key Pair */
        return (keyGenerator.generateKeyPair());
    }

    /* Method for Signing the winning hash */
    public static byte[] signData(byte[] data, PrivateKey key) throws Exception {
        /* Initializing the Signature Instance of SHA1withRSA Algorithm */
        Signature signer = Signature.getInstance("SHA1withRSA");
        /* Initializing  signer Object for Signing */
        signer.initSign(key);
        /* This updates the data to be Signed */
        signer.update(data);
        return (signer.sign());
    }
    /* Method for getting randomSeed based on the count variable */
    public static String randomAlphaNumeric(int count) {
        /* Initializing String Builder to new for getting different random seeds for different processes */
        StringBuilder builder = new StringBuilder();
        /* Running loop based on count so that the random seed has the same number of characters */
        while (count-- != 0) {
            /* fetching single character from the Alpha numeric string */
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            /* Appending each character to the builder String */
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        /* Returning the random Seed */
        return builder.toString();
    }
    public static BlockRecord Work(BlockRecord blockRecord){
        /* Initializing the randomSeed to null */
        String randomSeed = "";
        /* Initializing dataSeed to null */
        String dataSeed = "";
        /* Initializing hash to null */
        String hash = "";
        /* Initializing workNumber to 0 */
        int workerNum = 0;
        /* Setting the previous Hash with that of the Winning hash of the previous block  */
        blockRecord.setPreviousHash(Blockchain.blockChain.get(0).getWinningHash());
        /* Setting the block number such that it will always be one number higher than that of the previous block number */
        blockRecord.setBlockNum(Blockchain.blockChain.get(0).getBlockNum() + 1);
        /* Setting verification ProcessID to the current process */
        blockRecord.setVerificationProcessID(Integer.toString(Blockchain.PID));
        /* Concatenating all the above values and stored it in a string */
        String blockData = blockRecord.getTimeStamp() + blockRecord.getBlockNum() + blockRecord.getBlockID() + blockRecord.getSignedID() + blockRecord.getPreviousHash() + blockRecord.getFname() + blockRecord.getLname() + blockRecord.getDOB() + blockRecord.getSSNum() + blockRecord.getVerificationProcessID() + blockRecord.getDiag() + blockRecord.getTreat() + blockRecord.getRx() + blockRecord.getTimeStamp();;
        try {
            while (true) {
                /* Calling the randomAlphaNumeric Method and setting count to 8 to generate a random seed */
                randomSeed = randomAlphaNumeric(8);
                /* Combining the BlockData with that of random seed to generate dataSeed */
                dataSeed = blockData + randomSeed;
                /* Initializing a message digest algorithm Instance */
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                /* Hashing the dataSeed into bytes and storing ot on a variable */
                byte[] bytesHash = md.digest(dataSeed.getBytes("UTF-8"));
                /* Converting the hashbytes to hex using stringBuilder  */
                StringBuilder stringBuilder = new StringBuilder();
                /* Looping through the bytes */
                for ( byte b : bytesHash) {
                    /* Appending each byte by converting it using String format to the stringBuilder */
                    stringBuilder.append(String.format("%02x", b));
                }
                hash = stringBuilder.toString();
                /* Fetching the first four digits by using hexadecimal number system of the converted hash and store it in a variable */
                System.out.println("Hash is: " + hash);

                workerNum = Integer.parseInt(hash.substring(0,4),16);
                System.out.println("Printing the first four digits from hash in decimal along with the hex value : " + hash.substring(0,4) +" and " + workerNum);
                /* Checking for the puzzle condition that if the worknumber is less than 200000 then the puzzle is solved and the block gets verified. */
                if (!(workerNum < 20000)){
                    System.out.format("%d is not less than 20,000 so we did not solve the puzzle\n\n", workerNum);
                }
                if (workerNum < 20000){
                    System.out.println("block verified! with the value : "+ workerNum);
                    /* Setting the random seed to the verified block */
                    blockRecord.setRandomSeed(randomSeed);
                    /* Setting the winning hash for the verified block */
                    blockRecord.setWinningHash(hash);
                    /* Signing the Winning hash with the help of private key */
                    byte[] signedWinHash = signData(bytesHash, privateKey);
                    /* Converting the signed winning hash to string using base64 encoder */
                    String signedWinHashStr = Base64.getEncoder().encodeToString(signedWinHash);
                    /* Setting the SignedWinningHash for the current verified block */
                    blockRecord.setSignedWinningHash(signedWinHashStr);
                    break;
                }
                /* Looping through blockchain to verify whether any unverified blocks are added to blockchain by any other process */
                for (BlockRecord b: blockChain){
                    /* Condition to check for any matched BlockID's */
                    if (b.getBlockID().equals(blockRecord.getBlockID())){
                        /* If there is any match then the block should be abandoned */
                        System.out.println("Abandoning block...");
                        /* Creating a new block record for abandoned Block */
                        BlockRecord abandonedBlock = new BlockRecord();
                        /* Setting the BlockID of that block to Abandoned */
                        abandonedBlock.setBlockID("Abandoned");
                        /* Returning the abandoned block */
                        return abandonedBlock;
                    }
                }
                /* Inducing sleep to do work*/
                try{
                    Thread.sleep(7001);
                }catch(InterruptedException exec) {
                    throw new RuntimeException(exec);
                }
            }
        }catch(Exception exec) {
            /* Printing Stack Trace */
            exec.printStackTrace();
        }
        /* Return a verified block */
        return blockRecord;
    }

    /* Method for multicasting publicKeys over different processes */
    public void multiCastPublicKey(PublicKeyObject publicKey) {
        /* Create a Socket Object */
        Socket sock;
        /* Create a PrintStream Object */
        PrintStream toServer;
        /* Initializing the gson variable with the help of gson library */
        Gson gson = new GsonBuilder().create();

        /* Fetching PublicKey and converting it to json Object */
        String JSON = gson.toJson(publicKey);
        try{
            /* Looping all the processes */
            for(int i=0; i<numProcesses; i++){
                /* Creating a new connection to each port here we are using the port number which is spaced out based on the processID */
                /* Creating a new Socket  */
                sock = new Socket(serverName, (Ports.publicKeyServerBase + (i)));
                /* Setting up the Output Stream  */
                toServer = new PrintStream(sock.getOutputStream());
                /* Sending the public key with that of processID in jSON Format */
                toServer.println(JSON);
                /* Flushing the data to server */
                toServer.flush();
            }
        }catch (Exception exec) {
            /* Printing Stack Trace */
            exec.printStackTrace ();
        }
    }

    /* Method for multicasting start message to all the running processes */
    public void multiCastStart(){
        /* Create a Socket Object */
        Socket sock;
        /* Create a print stream object */
        PrintStream toServer;
        try{

            /* Looping for all the processes */
            for(int i=0; i<numProcesses; i++){
                /* Creating a new connection to each port here we are using the port number which is spaced out based on the processID */
                sock = new Socket(serverName, (Ports.StartServerBase + (i)));
                /* Setting the Output Print Stream  */
                toServer = new PrintStream(sock.getOutputStream());
                /* Sending the Start Message to all the other processes */
                toServer.println("go");
                /* Flushing the data to server */
                toServer.flush();
            }
        }catch (Exception exec) {
            /* Printing Stack Trace */
            exec.printStackTrace ();
        }
    }

    /* Method for multicasting unverified blocks to all the processes */
    public void multiCastUB(BlockRecord blockRecord, int serverBase){
        /* Create a Socket Object */
        Socket sock;
        /* Create a PrintStream Object */
        PrintStream toServer;

        try{
            /* Initializing new gson variable  */
            Gson gson = new GsonBuilder().create();
            /* Converting the BlockRecord object to json object */
            String jSon = gson.toJson(blockRecord);
            /* Looping for all the processes */
            for(int i=0; i< numProcesses; i++){
                /* Creating a new Socket connection with the port number which is spaced out based on ProcessID */
                sock = new Socket(serverName, (serverBase + (i)));
                /* Setting up Output Print Stream */
                toServer = new PrintStream(sock.getOutputStream());
                /* Sending the JSON Object along with the processID over all the processes */
                toServer.println(jSon);
                /* Flushing the data to server */
                toServer.flush();
            }

        }catch (Exception exec) {
            /* Printing Stack Trace */
            exec.printStackTrace ();
        }
    }

    /* Method for Multicasting BlockChain to all the other processes */
    public void multiCastBC(LinkedList<BlockRecord>bc, int serverBase){
        /* Create a Socket Object */
        Socket sock;
        /* Create a PrintStream Object */
        PrintStream toServer;

        try{
            /* Converting the blockchain record into a JSon using the method blockchain_JSon_Converter */
            String jSon = blockChain_JSon_Converter(bc);
            /* Loop for all the running processes */
            for(int i=0; i< numProcesses; i++){
                /* Creating a new Socket connection with the port which is based on the processID */
                sock = new Socket(serverName, (serverBase + (i)));
                /* Setting up an Output PrintStream  */
                toServer = new PrintStream(sock.getOutputStream());
                /* Send the JSON to all the other processes */
                toServer.println(jSon);
                /* Flushing the data to server */
                toServer.flush();
            }

        }catch (Exception exec) {
            /* Printing Stack Trace */
            exec.printStackTrace ();
        }
    }

    /* Method for reading an INPUT File and Converting it to block record */
    public static List<BlockRecord> readFile(int pid){
        /* Record list of the type BlockRecord to contain the records in the file */
        List<BlockRecord> recordList = new ArrayList<>();

        /* Switch Condition to pick the appropriate file based on ProcessID */
        switch(pid){
            /* Read BlockInput1 for processID 1 */
            case 1: fileName = "BlockInput1.txt";
                break;
            /* Read BlockInput2 for processID 2 */
            case 2: fileName = "BlockInput2.txt";
                break;
            /* Read BlockInput3 for processID 0 */
            default: fileName= "BlockInput0.txt";
                break;
        }

        System.out.println("Processing BlockInput"+pid+".txt  File...");
        /* Buffered Reader to go through the contents of the file */
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));

            String[] tokens;
            String inputLineStr;
            String unique_ID;

            /* Variable to keep track of the records present in a file */
            int recordCount = 0;

            while ((inputLineStr = br.readLine()) != null) {
                /* Creating new Record for each line in the file */
                BlockRecord blockRecord = new BlockRecord();
                /* Initializing a thread sleep so that it will give some time to read the data of each line */
                try{
                    Thread.sleep(1001);
                }catch(InterruptedException exec) {
                    /* Printing Stack Trace */
                    exec.printStackTrace();
                }
                /* Creating a new date variable to generate the timestamp */
                Date date = new Date();
                /* Generating the TimeStamp using string format */
                String T1 = String.format("%1$s %2$tF.%2$tT", "", date);
                /* Adding the processID to the generated TimeStamp */
                String TimeStampString = T1 + "." + pid;
                /* Setting the timeStamp for the blockRecord */
                blockRecord.setTimeStamp(TimeStampString);
                /* Setting the recordCount to the BlockNumber */
                blockRecord.setBlockNum(recordCount);

                /* Generating a UniqueID using randomUUID call */
                unique_ID = new String(UUID.randomUUID().toString());
                /* Setting the UniqueID to BlockID */
                blockRecord.setBlockID(unique_ID);
                /* Signing the UniqueID with the privateKey */
                byte[] digitalSignature = signData(unique_ID.getBytes(), privateKey);
                /* Converting the Signed Variable to String using Base64 Encoding */
                String SignedSHA256ID = Base64.getEncoder().encodeToString(digitalSignature);
                /* Setting the Signed ID */
                blockRecord.setSignedID(SignedSHA256ID);
                /* Splitting the contents of the line and setting up the data */
                tokens = inputLineStr.split(" +");
                /* Setting the firstname */
                blockRecord.setFname(tokens[fName_Index]);
                /* Setting the lastname */
                blockRecord.setLname(tokens[lName_Index]);
                /* Setting SSN Number */
                blockRecord.setSSNum(tokens[iSSNUM_Index]);
                /* Setting dateOfBirth */
                blockRecord.setDOB(tokens[dOB_Index]);
                /* Setting diag */
                blockRecord.setDiag(tokens[iDIAG_Index]);
                /* Setting Treat */
                blockRecord.setTreat(tokens[treatment_Index]);
                /* Setting Rx */
                blockRecord.setRx(tokens[iRX]);
                /* Fetching VerificationID */
                blockRecord.setVerificationProcessID(Integer.toString(Blockchain.PID));
                /* Add the block record to the recordList */
                recordList.add(blockRecord);
                /* Incrementing the record Count */
                recordCount++;
            }
        } catch (Exception exec){
            /* Printing Stack Trace */
            exec.printStackTrace();
        }
        /* Return the unverified blocks */
        return recordList;
    }

    /* Method for write the verified blocks to a file */
    public static void writeToFile(LinkedList<BlockRecord> bc){
        /* For printing the Json in a readable format we use gson pretty */
        Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
        /* The JSON Write variable initialized to open square bracket as the Json starts with this */
        String JSONWrite = "[";
        /* Looping each block in block Chain */
        for (BlockRecord block: bc){
            JSONWrite += gsonPretty.toJson(block);
            /* Adding a comma separator between each block */
            if (bc.indexOf(block) != bc.size() - 1)
                JSONWrite += ",";
        }
        /* Setting the closing square bracket at the end of each block */
        JSONWrite = JSONWrite + "]";
        /* Writing all the data to a file */
        try (FileWriter writer = new FileWriter("BlockchainLedger.json", false)) {
            writer.write(JSONWrite);
        } catch (IOException e) {
            /* Printing Stack Trace */
            e.printStackTrace();
        }
    }

    /* Method for converting BlockRecord object to a json Object */
    public static String blockChain_JSon_Converter(LinkedList<BlockRecord> bc){
        /* Created a new GsonBuilder Instance */
        Gson gson = new GsonBuilder().create();
        /* Initializing the Json Object to open Square bracket */
        String jSON = "[";
        /* looping through block record */
        for (BlockRecord blockRecord: bc){
            jSON += gson.toJson(blockRecord);

            /* Adding commas between the values */
            if (bc.indexOf(blockRecord) != bc.size() - 1)
                jSON += ",";
        }
        /* Closing the Json with a closed Square Bracket */
        jSON = jSON + "]";
        /* Return the converted Json */
        return jSON;
    }

    public static void main(String args[]) throws Exception {

        /* Checking for the Arguments that are received from command line */
        if (args.length < 1)
            PID = 0;
        else if (Integer.parseInt(args[0]) > 2){
            System.out.println("Process numbers are 0, 1, or 2");
            throw new IllegalArgumentException();
        }
        else
            PID = Integer.parseInt(args[0]);

        System.out.println("Satya Yoganand's Blockchain program. Ctl-c to quit\n");
        System.out.println("Using processID " + PID + "\n");


        /* Setting up ports for the current process with respect to processID */
        new Ports().setPorts();

        /* Creating a blockChain new Variable  */
        Blockchain blockChain1 = new Blockchain();
        /* Initializing the publicKey with respect to porocess ID */
        PublicKeyObject publicKey = publicKeyInit(PID);

        System.out.println("PublicKey for processID : "+PID+" ----- "+ publicKey.getPublicKey());

        /* Generating a new Start Server Thread */
        new Thread(new StartServer()).start();
        /* Generating a new PublickeyServer Thread */
        new Thread(new PublicKeyServer()).start();
        /* Generating a new UnverifiedBlockServer Thread */
        new Thread(new UnverifiedBlockServer()).start();
        /* Generating a new BlockChainServer Thread */
        new Thread(new BlockchainServer()).start();
        System.out.println("All the Servers have been set, waiting to receive start message");
        /* If the processID is 2 then multicast the start message to all the running processes */
        if (PID == 2){
            new Blockchain().multiCastStart();
        }
        /* Inducing sleep such that the start message will be multicasted then all the processes start doing the blockchain work  */
        System.out.println("Before Start Status : "+ start);
        try{Thread.sleep(12000);} catch(InterruptedException exec){
            /* Printing Stack Trace */
            exec.printStackTrace();
        }
        /* After receiving start message the blockChain process will Start */

        if (start.equals("go")){
            /* Multicasting the public key */
           blockChain1.multiCastPublicKey(publicKey);
            /* Inducing sleep so that all the processes will receive all the publicKeys */
            try{Thread.sleep(12000);}catch(InterruptedException exec){
                /* Printing Stack Trace */
                exec.printStackTrace();
            }
            /* Printing all the public keys on the console */
            String publicKeyProcess = "";
            for (PublicKeyObject pubK: publicKeyArray){
                publicKeyProcess = "ProcessID "+pubK.getProcessID() + " Using : " + pubK.getPublicKey();
                System.out.println(publicKeyProcess);
            }
            System.out.println("----------------------------------------------");
            /* Initializing blockchain with a dummy block data */
            blockChain = blockChainInit();
            /* Read the files with respect to the processID and store the blocks in a block array */
            blockArr = readFile(PID);
            /* Looping through blockArray so that all the blocks will be marked as Unverified and then added to priority Queue for further processing */
            for (BlockRecord block: blockArr)
                new Blockchain().multiCastUB(block, Ports.UnverifiedBlockServerPortBase);
            System.out.println("Unverified Blocks sent to Priority Queue...");
            /* Inducing sleep so that all the process will fetch all their unverified blocks */
            try{Thread.sleep(4000);}catch(InterruptedException e){
                /* Printing Stack Trace */
                e.printStackTrace();
            }

            /* Waiting to accept all the connections */
            while (true){
                /* Inducing sleep on the thread so that the work will be done on each unverified block */
                try{Thread.sleep(2001);}catch(InterruptedException e){
                    /* Printing Stack Trace */
                    e.printStackTrace();
                }

                System.out.println(blockPriorityQueue.size() + " unverified blocks remaining");
                /* Checking for the items in priorityQueue if null then break the loop */
                BlockRecord tempBlock = blockPriorityQueue.poll();
                if (tempBlock == null)
                    break;
                BlockRecord verifiedBlock = new BlockRecord();
                /* Setting up a flag for block which refers whether the block is present in the blockchain or not */
                boolean blockExists = false;
                /* Initializing a temporary public key variable */
                String tempPubKey = "";
                for (PublicKeyObject pub: publicKeyArray){
                    if (Integer.toString(pub.getProcessID()).equals(tempBlock.getVerificationProcessID())){
                        /* Fetch the public key for the current process and store it in the temp variable */
                        tempPubKey = pub.getPublicKey();
                        System.out.println("Using the public key from process: " + pub.getProcessID());
                    }
                }
                /* Converting this public key into bytes */
                byte[] publicKeyInBytes = Base64.getDecoder().decode(tempPubKey);
                /* converting the signed blockID into bytes to get ready for verification */
                byte[] idSignature = Base64.getDecoder().decode(tempBlock.getSignedID());
                /* converting the bytes of the public key into a public key object reference */
                X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyInBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PublicKey RestoredKey = keyFactory.generatePublic(publicKeySpec);
                /* Now by using verify Sig method with the key and both byte arrays, verify if it is signed */
                boolean verified = verifySig(tempBlock.getBlockID().getBytes(), RestoredKey, idSignature);
                if(!verified){
                    System.out.println("This block is not signed by the correct owner of the private key, moving on...");
                }
                else {

                    try{Thread.sleep(1000);}catch(InterruptedException e){}
                    /* Condition for whether the  block exists in the blockchain */
                    for (BlockRecord b: blockChain){
                        if (b.getBlockID().equals(tempBlock.getBlockID())){
                            blockExists = true;
                            System.out.println("Block already in blockchain");
                        }
                    }
                    /* If the block is not present in the blockchain then start verifying it */
                    while (!blockExists){
                        System.out.println("Attempting to verify block");
                        /* performing work for the current record */
                        verifiedBlock = Work(tempBlock);
                        /* Condition to check if the blockchain was changed */
                        String previousHash = blockChain.get(0).getWinningHash();
                        /* If the block is an abandoned block then process the next block in the PriorityQueue */
                        if (verifiedBlock.getBlockID().equals("Abandoned"))
                            break;
                        if (!(verifiedBlock.getBlockID().equals("Abandoned"))){
                            /* if the blockChain was not changed then add it to the blockchain */
                            if (verifiedBlock.getPreviousHash().equals(previousHash)){
                                System.out.println("Block has been verified and added to the blockchain and started multicasting to all the other running processes");
                                /* Multicasting the modified blockchain to all the other processes */
                                blockChain.addFirst(verifiedBlock);
                                /* Multicasting the modified blockchain to all the other processes */
                                blockChain1.multiCastBC(blockChain, Ports.BlockchainServerPortBase);
                                /* Change the status of the blockExists to true */
                                blockExists = true;
                            }
                            /* If the block is modified */
                            else {
                                /* Checking  to see again if the block is in the blockchain, if it exists then start processing the next block in PriorityQueue */
                                for (BlockRecord b: blockChain){
                                    if (b.getBlockID().equals(verifiedBlock.getBlockID())){
                                        blockExists = true;
                                    }
                                }
                                System.out.println("processing to rework on block again...");
                            }
                        }
                    }
                }
            }
            /* The program is done with processing all the unverified blocks and  adding them into the blockChainlist is complete */
            System.out.println("BLOCKCHAIN COMPLETE");
        }
    }
}
class Ports{
    /* Setting the StartServerBase to 4600 */
    public static int StartServerBase = 4600;

    /* Setting the UnverifiedBlockServerPortBase to 4820 */
    public static int UnverifiedBlockServerPortBase = 4820;
    /* Setting the BlockchainServerPortBase to 4930 */
    public static int BlockchainServerPortBase = 4930;

    /* Setting the publicKeyServerBase to 4710 */
    public static int publicKeyServerBase = 4710;
    public static int publicKeyServerPort;
    public static int StartServerPort;
    public static int UnverifiedBlockServerPort;
    public static int BlockchainServerPort;
    /* Methods to set ports for all the running processID's */
    public void setPorts(){
        /* Setting the StartServerPort to StartServerBase incremented with processID */
        StartServerPort = StartServerBase + (Blockchain.PID);
        /* Setting the publicKeyServerPort to publicKeyServerBase incremented with processID */
        publicKeyServerPort = publicKeyServerBase + (Blockchain.PID);
        /* Setting the UnverifiedBlockServerPort to UnverifiedBlockServerBase incremented with processID */
        UnverifiedBlockServerPort = UnverifiedBlockServerPortBase + (Blockchain.PID);
        /* Setting the BlockChainServerPort to BlockChainServerBase incremented with processID */
        BlockchainServerPort = BlockchainServerPortBase + (Blockchain.PID);
    }
}

class BlockRecord{
    /* Block ID for the current Block  */
    String BlockID;
    /* Block Number for the current Block */
    int blockNum;
    /* Time Stamp for the block */
    String TimeStamp;
    /* VerificationID for the process */
    String VerificationProcessID;
    /* PreviousHash for the block which is the winning hash of previous block */
    String PreviousHash;
    /* Unique ID */
    UUID uuid;

    /* First Name for the current Block */
    String fName;

    /* Last Name for the current Block */
    String lName;

    /* SSN for the current Block */
    String SSNum;
    /* DateOfBirth for the current Block */
    String DOB;
    /* Random Seed is our guess */
    String RandomSeed;
    String winningHash;
    String signedID;
    String signedWinningHash;
    /* Creating an object for diag */
    String Diag;
    /* Creating an object for Treatment */
    String Treat;
    /* Creating an object  for Rx */
    String Rx;

    /* Initializing setters and getters for all the variables defined */
    public String getBlockID() {return BlockID;}
    public void setBlockID(String BID){this.BlockID = BID;}

    public int getBlockNum() {return blockNum;}
    public void setBlockNum(int blockNum){this.blockNum = blockNum;}

    public String getTimeStamp() {return TimeStamp;}
    public void setTimeStamp(String TS){this.TimeStamp = TS;}

    public String getVerificationProcessID() {return VerificationProcessID;}
    public void setVerificationProcessID(String VID){this.VerificationProcessID = VID;}

    public String getPreviousHash() {return this.PreviousHash;}
    public void setPreviousHash (String PH){this.PreviousHash = PH;}

    public UUID getUUID() {return uuid;}
    public void setUUID (UUID ud){this.uuid = ud;}

    public String getSignedID() {return signedID;}
    public void setSignedID (String signedID){this.signedID = signedID;}

    public String getLname() {return lName;}
    public void setLname (String lName){this.lName = lName;}

    public String getFname() {return fName;}
    public void setFname (String fName){this.fName = fName;}

    public String getSSNum() {return SSNum;}
    public void setSSNum (String ssNum){this.SSNum = ssNum;}

    public String getDOB() {return DOB;}
    public void setDOB (String dOB){this.DOB = dOB;}

    public String getDiag() {return Diag;}
    public void setDiag (String diag){this.Diag = diag;}

    public String getTreat() {return Treat;}
    public void setTreat (String treat){this.Treat = treat;}

    public String getRx() {return Rx;}
    public void setRx (String rx){this.Rx = rx;}

    public String getRandomSeed() {return RandomSeed;}
    public void setRandomSeed (String randomSeed){this.RandomSeed = randomSeed;}

    public String getWinningHash() {return winningHash;}
    public void setWinningHash (String winningHash){this.winningHash = winningHash;}

    public String getSignedWinningHash() {return signedWinningHash;}
    public void setSignedWinningHash (String signedWinningHash){this.signedWinningHash = signedWinningHash;}


}

class PublicKeyObject {
    String publicKey;
    int processID;
    /* Initializing get method for public key */
    public String getPublicKey(){return this.publicKey;}
    /* Initializing set method for public key */
    public void setPublicKey(String pk){this.publicKey = pk;}
    /* Initializing get method for processID */
    public int getProcessID(){return this.processID;}
    /* Initializing set method for processID */
    public void setProcessID(int id){this.processID = id;}
}

/* Worker thread for processing incoming public keys */
class PublicKeyWorker extends Thread {
    /* Create a Socket Object */
    Socket sock;
    /* Initializing a new gson variable */
    Gson gson = new Gson();
    PublicKeyWorker (Socket s) {sock = s;}
    public void run(){
        try{
            /* Fetching the inputs for the server using BufferedReader through inputStream */
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            /* Storing the received input */
            String input = in.readLine ();

            System.out.println("Public Key Received: " + input);
            /* Fetching the publicKey,ProcessID from PublicKeyObject using from JSon */
            PublicKeyObject publicKey = gson.fromJson(input, PublicKeyObject.class);
            /* Adding the obtained publicKeyObject to publicKeyArray */
            Blockchain.publicKeyArray.add(publicKey);
            System.out.println("PublicKeyArray : "+Blockchain.publicKeyArray);
            /* Closing the Socket */
            sock.close();
        } catch (IOException ex){
            /* Printing Stack Trace */
            ex.printStackTrace();
        }
    }
}

class PublicKeyServer implements Runnable {
    /* Allowed number of connections from client to server at the same time */
    int q_len = 6;
    Socket sock;

    public void run(){

        try{
            /* The ServerSocket will take the portNumber and queue length on which it is going to communicate */
            ServerSocket servSock = new ServerSocket(Ports.publicKeyServerPort, q_len);
            /* Accepting all the connections */
            while (true) {
                /* This is an accept method for listening from the client */
                sock = servSock.accept();
                /* This Start method will invoke the run method in PublicKeyWorker class */
                new PublicKeyWorker (sock).start();
            }
        }catch (IOException ioExec) {
            /* Printing Stack Trace */
            ioExec.printStackTrace();
        }
    }
}

class UnverifiedBlockWorker extends Thread {

    /* Create a Socket Object */
    Socket sock;
    /* Create a new gson Object */
    Gson gson = new Gson();
    public UnverifiedBlockWorker(Socket s){
        this.sock = s;
    }
    public void run(){

        try{
            /* Fetching the inputs for the server using BufferedReader through inputStream */
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            /* Blocking Call - the server code will pauses at this point and starts listening to the input from client */
            String input = in.readLine();
            /* Converting it to BlockRecord object which holds all the Unverified blocks */
            BlockRecord recordData = gson.fromJson(input, BlockRecord.class);
            /* Adding the block to priorityQueue */
            Blockchain.blockPriorityQueue.add(recordData);
        }catch(IOException exec){
            /* Printing Stack Trace */
            System.out.print(exec);
        }
    }
}

class UnverifiedBlockServer implements Runnable {
    /* Allowed number of connections from client to server at the same time */
    int q_len = 6;
    /* Create a Socket Object */
    Socket sock;

    public void run() {
        try {
            /* The ServerSocket will take the portNumber and queue length on which it is going to communicate */
            ServerSocket servSock = new ServerSocket(Ports.UnverifiedBlockServerPort, q_len);
            /* Accepting all the connections */
            while (true) {
                /* This is an accept method for listening from the client */
                sock = servSock.accept();
                /* This Start method will invoke the run method in UnverifiedBlockWorker class */
                new UnverifiedBlockWorker(sock).start();
            }
        } catch (IOException exec) {
            /* Printing Stack Trace */
            exec.printStackTrace();
        }
    }
}

class BlockChainWorker extends Thread {
    /* Create a Socket Object */
    Socket sock;
    /* Create a new gson Object */
    Gson gson = new Gson();
    /* Constructor to assign the incoming sock to the local variable */
    BlockChainWorker (Socket s) {sock = s;}
    public void run(){

        try{
            /* Fetching the inputs for the server using BufferedReader through inputStream */
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            /* Blocking Call - the server code will pauses at this point and starts listening to the input from client */
            String input = in.readLine();
            /* Converting the String input to array of BlockRecord through fromJson */
            BlockRecord[] blockRecordData = gson.fromJson(input, BlockRecord[].class);
            /* Empty the Blockchain list first */
            Blockchain.blockChain.clear();
            /* Now Adding all the blocks to blockchain */
            for (BlockRecord record: blockRecordData){
                Blockchain.blockChain.add(record);
            }
            /* If the process 0 hears any new update it will rewrite it to a file */
            if (Blockchain.PID == 0){
                /* Write method will be called to update the blockchain */
                Blockchain.writeToFile(Blockchain.blockChain);
            }
        } catch (IOException exec){
            /* Printing Stack Trace */
            exec.printStackTrace();
        }
    }
}

class BlockchainServer implements Runnable {
    /* Allowed number of connections from client to server at the same time */
    int q_len = 6;
    /* Create a Socket Object */
    Socket sock;

    public void run(){
        try{
            /* The ServerSocket will take the portNumber and queue length on which it is going to communicate */
            ServerSocket servSock = new ServerSocket(Ports.BlockchainServerPort, q_len);
            /* Accepting all the connections */
            while (true) {
                /* This is an accept method for listening from the client */
                sock = servSock.accept();
                /* This Start method will invoke the run method in BlockChainWorker class */
                new BlockChainWorker(sock).start();
            }
        }catch(IOException exec){
            /* Printing Stack Trace */
            exec.printStackTrace();
        }
    }
}

class StartWorker extends Thread {
    /* Create a Socket Object */
    Socket sock;
    /*constructor for assigning incoming connection to a local variable */
    public StartWorker(Socket s){
        this.sock = s;
    }
    public void run(){
        try{
            /* Fetching the inputs for the server using BufferedReader through inputStream */
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            /* Setting up the Global start variable in BlockChain class as per the incoming output typically "go" */
            Blockchain.start = in.readLine();

        }catch(IOException exec){
            /* Printing Stack Trace */
            exec.printStackTrace();
        }
    }
}

class StartServer implements Runnable {
    /* Allowed number of connections from client to server at the same time */
    int q_len = 6;
    /* Create a Socket Object */
    Socket sock;

    public void run(){
        try{
            /* The ServerSocket will take the portNumber and queue length on which it is going to communicate */
            ServerSocket servSock = new ServerSocket(Ports.StartServerPort, q_len);
            /* Accepting all the connections */
            while (true) {
                /* This is an accept method for listening from the client */
                sock = servSock.accept();
                /* This Start method will invoke the run method in StartWorker class */
                new StartWorker(sock).start();
            }
        }catch(IOException exec){
            /* Printing Stack Trace */
            exec.printStackTrace();
        }
    }
}


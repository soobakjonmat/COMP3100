import java.net.Socket;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.IOException;

public class DSClient {
    static final String IP_ADDRESS = "localhost";
    static final String AUTH_INFO = "isac";

    static int portNum = 50000;

    static Socket socket;
    static BufferedReader reader;
    static DataOutputStream outStream;
    static Scanner scanner;

    static int serverNum;
    static String largestServerType;
    static int largestServerLimit;
    static int curServerID;

    static void sendMessage(String msg) throws IOException {
        outStream.write((msg+"\n").getBytes());
        outStream.flush();
        System.out.println("C SENT: " + msg);
    }

    static String readMessage() throws IOException {
        String msg = reader.readLine();
        if (msg == "ERR") {
            quitProgram();
        }
        System.out.println("C RCVD: " + msg);
        return msg;
    }

    static void quitProgram() throws IOException  {
        sendMessage("QUIT");
        readMessage();

        reader.close();
        outStream.close();
        socket.close();
    }

    public static void main(String args[]) throws Exception {
        socket = new Socket(IP_ADDRESS, portNum);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        outStream = new DataOutputStream(socket.getOutputStream());

        String receivedMsg;
        String command;
        // Handshake
        // Send HELO
        sendMessage("HELO");
        // Receive OK first time
        readMessage();
        sendMessage("AUTH " + AUTH_INFO);
        // Receive OK second time
        readMessage();
        // Send ready message
        sendMessage("REDY");
        readMessage();

        // get server state information
        sendMessage("GETS All");
        receivedMsg = readMessage();
        scanner = new Scanner(receivedMsg);
        scanner.next(); // skip DATA
        serverNum = scanner.nextInt();
        scanner.close();
        sendMessage("OK");
        for (int i = 0; i < serverNum - 1; i++) {
            readMessage();
        }
        receivedMsg = readMessage();
        scanner = new Scanner(receivedMsg);
        largestServerType = scanner.next();
        largestServerLimit = scanner.nextInt() + 1;
        scanner.close();
        curServerID = 0;

        sendMessage("OK");
        readMessage();
        sendMessage("REDY");

        mainLoop: while (true) {
            receivedMsg = readMessage();
            scanner = new Scanner(receivedMsg);
            command = scanner.next();

            switch (command) {
                case "ERR":
                    break mainLoop;
                case "NONE":
                    break mainLoop;
                case "OK":
                    sendMessage("REDY");
                    break;
                case "JOBN":
                    // int sumbitTime =
                    scanner.nextInt();
                    int jobID = scanner.nextInt();
                    // int estRunTime = scanner.nextInt();
                    // int core = scanner.nextInt();
                    // int memory = scanner.nextInt();
                    // int disk = scanner.nextInt();
                    // LRR algorithm i guess?
                    String msgToSend = "SCHD " + jobID + " " + largestServerType + " " +curServerID;
                    curServerID = (curServerID + 1) % largestServerLimit;
                    sendMessage(msgToSend);
                    break;
                case "JOBP":
                    break mainLoop;
                case "JCPL":
                    sendMessage("REDY");
                    break;
                case "RESF":
                    break mainLoop;
                case "RESR":
                    break mainLoop;
                case "CHKQ":
                    break mainLoop;
            }
            scanner.close();
        }

        quitProgram();
    }
}
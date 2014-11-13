

import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 *
 * @author Sjurdur
 */
public class Put {

    public static void main(String[] args) throws Exception {

        InetAddress nodeIP = InetAddress.getLocalHost();
        int nodePort;
        int key;
        String value;

        if (args.length == 4) {
            nodeIP = InetAddress.getByName(args[0]);
            nodePort = Integer.parseInt(args[1]);
            key = Integer.parseInt(args[2]);
            value = args[3];

            Socket socketToServer = new Socket(nodeIP, nodePort);
            ObjectOutputStream outStream = new ObjectOutputStream(socketToServer.getOutputStream());

            System.out.println("Sending Get Request object to Node at: " + nodeIP + " port: " + nodePort);
            outStream.writeObject(new PutRequest(key, value));

            outStream.flush();
            outStream.close();

            System.exit(0);

        } else {
            throw new Exception("Incorrect number of arguements.");
        }

    }
}

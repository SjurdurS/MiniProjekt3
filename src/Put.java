import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 *
 * @author Sjurdur
 */
public class Put {

    public static void main(String[] args) throws Exception {

        InetAddress nodeIP;
        int nodePort;
        int key;
        String value;

        if (args.length == 4) {
            nodeIP = InetAddress.getByName(args[0]);
            nodePort = Integer.parseInt(args[1]);
            key = Integer.parseInt(args[2]);
            value = args[3];

            Socket socket = new Socket(nodeIP.getHostName(), nodePort);

            try (ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());) {
                System.out.println("Sending Put Request object to Node at: " + nodeIP + " port: " + nodePort);
                outStream.writeObject(new PutRequest(key, value));
                socket.close();
                
                System.exit(1);

            } catch (Exception e) {
                System.out.println(e);
            }

        } else {
            throw new Exception("Incorrect number of arguements.");
        }

    }
}

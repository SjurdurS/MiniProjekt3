import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sjurdur
 */
public class Get {

    public static void main(String[] args) throws Exception {

        // Get request
        InetAddress nodeIP;
        int nodePort;
        int key;

        // Client
        String localHost = InetAddress.getLocalHost().getHostAddress();
        int localPort;

        if (args.length == 4) {
            nodeIP = InetAddress.getByName(args[0]);
            localPort = Integer.parseInt(args[1]);
            nodePort = Integer.parseInt(args[2]);
            key = Integer.parseInt(args[3]);

            GetRequest getRequest = new GetRequest(key, localHost, localPort);

            new GetSender(nodeIP, nodePort, getRequest).start();
            new PutListener(localPort).start();

        } else {
            System.out.println("Incorrect number of arguements.");
        }

    }

    static class GetSender extends Thread {

        // Node to send to
        InetAddress nodeIP = null;
        int nodePort = 0;

        // Request to send
        GetRequest getRequest = null;

        // Socket to send over
        Socket client = null;

        public GetSender(InetAddress nodeIP, int nodePort, GetRequest getRequest) throws IOException {
            super("GetSenderThread");
            this.nodeIP = nodeIP;
            this.nodePort = nodePort;

            this.getRequest = getRequest;
        }

        @Override
        public void run() {

            try (Socket senderSocket = new Socket(nodeIP, nodePort);
                    ObjectOutputStream outStream = new ObjectOutputStream(senderSocket.getOutputStream())) {
                System.out.println("Sending Get Request object to Node at: " + nodeIP + " port: " + nodePort);
                outStream.writeObject(getRequest);

            } catch (ConnectException ce) {
                System.err.println(ce.getClass() + " - " + ce.getMessage());
                System.exit(1);

            } catch (IOException ex) {
                Logger.getLogger(Get.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(1);

            }
        }
    }

    static class PutListener extends Thread {

        ServerSocket ss;
        Socket client = null;

        public PutListener(int localPort) throws IOException {
            super("PutListenerThread");

            this.ss = new ServerSocket(localPort);
        }

        @Override
        public void run() {
            try {
                client = ss.accept();
                System.out.println("Incoming connection made : " + client.getLocalAddress() + " - " + client.getLocalPort());

                try (ObjectInputStream is = new ObjectInputStream(client.getInputStream())) {
                    Object obj = is.readObject();
                    if (obj instanceof PutRequest) {
                        PutRequest p = (PutRequest) obj;

                        System.out.println("Message received: " + p.value);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Get.class.getName()).log(Level.SEVERE, null, ex);
                }

            } catch (IOException ex) {
                Logger.getLogger(Get.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Get.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
}

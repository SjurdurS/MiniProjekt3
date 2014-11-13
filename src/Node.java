import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sjurdur
 */
public class Node {

    // Kun indsætte eller overskrive, ikke slette.
    public static HashMap<Integer, String> messages = new HashMap<>();

    public static HashSet<NodeTuple> nt = new HashSet<>();

    public static void main(String[] args) throws Exception {

        int localPort;
        InetAddress localhost;
        InetAddress nodeIP;
        int nodePort;

        //if (args.length == 1) {
        System.out.println("Argument 1");
        localPort = 7007;
        new ListenerServer(localPort).start();
        /*
         } else if (args.length == 3) {
         System.out.println("Argument 3");

         localPort = Integer.parseInt(args[0]);
         nodeIP = InetAddress.getByName(args[1]);
         nodePort = Integer.parseInt(args[2]);

         boolean b = nt.add(new NodeTuple(nodePort, nodeIP.getHostName()));

         new ListenerServer(localPort);

         } else {
         throw new Exception("Incorrect number of arguments.");
         }
         */
    }

    static class ListenerServer extends Thread {

        ServerSocket ss;
        Socket client = null;

        public ListenerServer(int localPort) throws IOException {
            this.ss = new ServerSocket(localPort);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    System.out.println("Listening for incoming connections");
                    client = ss.accept();
                    System.out.println("Connection made");

                    new ListenerThread(client).start();

                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    static class ListenerThread extends Thread {

        Socket listenerSocket = null;

        ListenerThread(Socket listenerSocket) {
            super("ListenerThread");
            this.listenerSocket = listenerSocket;
        }

        @Override
        public void run() {
            try (
                    ObjectInputStream is = new ObjectInputStream(listenerSocket.getInputStream());
                    ObjectOutputStream os = new ObjectOutputStream(listenerSocket.getOutputStream());) {

                Object obj = null;

                System.out.println("Reading for object");

                obj = is.readObject();
                System.out.println("Object found");

                if (obj instanceof NodeRequest) {
                    NodeRequest nodeRequest = (NodeRequest) obj;

                    int nodeRequestPort = nodeRequest.port;
                    String nodeRequestIP = nodeRequest.ip;

                    nt.add(new NodeTuple(nodeRequestPort, nodeRequestIP));

                    for (NodeTuple n : nt) {
                        NodeInform nodeInform = new NodeInform(nt);

                        // Send NodeInform to (n.getPort(), n.getHostName())
                    }

                    // Send all Puts to request node.
                } else if (obj instanceof NodeInform) {
                    NodeInform nodeInform = (NodeInform) obj;
                    nt.addAll(nodeInform.nt);

                } else if (obj instanceof GetRequest) {
                    GetRequest getMessage = (GetRequest) obj;
                    //Do logic here.
                    String message = messages.get(getMessage.key);
                    System.out.println("MESSAGE RECEIVED MOTHER FUCKER: " + message);
                    System.out.println("Node Port: " + listenerSocket.getLocalPort() + " - GET MESSAGE RECEIVED.");

                    // Send Put back to Get
                } else if (obj instanceof PutRequest) {
                    PutRequest putMessage = (PutRequest) obj;

                    messages.put(putMessage.key, putMessage.value);
                    System.out.println("Node Port: " + listenerSocket.getLocalPort() + " - PUT MESSAGE RECEIVED.");

                    // Send Put to all NodeTuples.
                }

                System.out.println("Yo Object receivingers Done");
                listenerSocket.close();
                System.out.println("Socket closed.");

            } catch (IOException ex) {
                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}

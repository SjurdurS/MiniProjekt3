import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
        String localhost = "localhost";
        InetAddress nodeIP;
        int nodePort;

        if (args.length == 1) {
            System.out.println("Argument 1");
            localPort = Integer.parseInt(args[0]);
            new ListenerServer(localPort).start();

        } else if (args.length == 3) {
            System.out.println("Argument 3");

            localPort = Integer.parseInt(args[0]);
            nodeIP = InetAddress.getByName(args[1]);
            nodePort = Integer.parseInt(args[2]);

            nt.add(new NodeTuple(nodePort, nodeIP.getHostName()));
            
            new NodeRequestSender(nodeIP.getHostName(), nodePort, new NodeRequest(localhost, localPort)).start();

            new ListenerServer(localPort).start();

        } else {
            throw new Exception("Incorrect number of arguments.");
        }

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

        /**
         * Add a NodeTuple to the common Collection of NodeTuples
         *
         * @param n NodeTuple to add.
         */
        public synchronized void addNodeTuple(NodeTuple n) {
            nt.add(n);
        }

        /**
         * Add a collection of NodeTuples to the common Collection of NodeTuples
         *
         * @param nodeTuples The Collection of NodeTuples to add.
         */
        public synchronized void addNodeTuples(HashSet<NodeTuple> nodeTuples) {
            nt.addAll(nodeTuples);
            
            System.out.println("NodeTuples stored: " + nt.size());
        }

        /**
         * Add a message to the messages collection.
         *
         * @param key The key of the message
         * @param message The content of the message
         */
        public synchronized void addPutMessage(int key, String message) {
            messages.put(key, message);
        }

        @Override
        public void run() {
            try (
                    ObjectInputStream is = new ObjectInputStream(listenerSocket.getInputStream());
                    ObjectOutputStream os = new ObjectOutputStream(listenerSocket.getOutputStream());) {

                Object obj = is.readObject();

                if (obj instanceof NodeRequest) {
                    System.out.println("NodeRequest RECEIVED");

                    NodeRequest nodeRequest = (NodeRequest) obj;

                    int nodeRequestPort = nodeRequest.port;
                    String nodeRequestIP = nodeRequest.ip;
                    NodeTuple nodeTuple = new NodeTuple(nodeRequestPort, nodeRequestIP);
                    addNodeTuple(nodeTuple);

                    // Send nodeinform to all Nodes known by this Node.
                    Set syncSet = Collections.synchronizedSet(nt);
                    synchronized (syncSet) {
                        Iterator i = syncSet.iterator();
                        // Display elements
                        while (i.hasNext()) {
                            NodeTuple n = (NodeTuple) i.next();
                            NodeInform nodeInform = new NodeInform(nt);

                            new NodeInformSender(n, nodeInform).start();
                        }
                    }

                    Map map = Collections.synchronizedMap(messages);
                    Set set = map.entrySet();
                    synchronized (map) {
                        Iterator i = set.iterator();
                        // Display elements
                        while (i.hasNext()) {
                            Map.Entry me = (Map.Entry) i.next();
                            int key = (Integer) me.getKey();
                            String message = (String) me.getValue();
                            new PutSender(nodeRequestIP, nodeRequestPort, key, message).start();
                        }
                    }

                } else if (obj instanceof NodeInform) {
                    System.out.println( "NodeInform RECEIVED");

                    NodeInform nodeInform = (NodeInform) obj;
                    addNodeTuples(nodeInform.nt);

                } else if (obj instanceof GetRequest) {
                    System.out.println("GetRequest RECEIVED");

                    GetRequest getMessage = (GetRequest) obj;
                    String message = messages.get(getMessage.key);

                    // Send Put back to Get
                    if (message != null) {
                        new PutSender(getMessage.hostName, getMessage.port, message).start();
                    }

                } else if (obj instanceof PutRequest) {
                    System.out.println("PutRequest RECEIVED");

                    PutRequest putRequest = (PutRequest) obj;
                    int key = putRequest.key;

                    String message = putRequest.value;

                    addPutMessage(key, message);

                    // Send Put to all NodeTuples.
                    for (NodeTuple n : nt) {
                        new PutSender(n.getHostName(), n.getPort(), key, message).start();
                    }
                }

                listenerSocket.close();
                System.out.println("Socket closed.");

            } catch (IOException ex) {
                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    static class PutSender extends Thread {

        String hostName;
        int port;

        int key = 0;
        String message;

        public PutSender(String hostName, int port, int key, String message) {
            super("PutSenderThread");
            this.hostName = hostName;
            this.port = port;
            this.key = key;
            this.message = message;
        }

        public PutSender(String hostName, int port, String message) {
            super("PutSenderThread");
            this.hostName = hostName;
            this.port = port;
            this.message = message;
        }

        @Override
        public void run() {
            try (Socket senderSocket = new Socket(hostName, port);
                    ObjectOutputStream outStream = new ObjectOutputStream(senderSocket.getOutputStream())) {
                System.out.println("Sending PutMessage to: " + hostName + " port: " + port);
                outStream.writeObject(new PutRequest(key, message));

            } catch (ConnectException ce) {
                System.err.println(ce.getClass() + " - " + ce.getMessage());
            } catch (IOException ex) {
                Logger.getLogger(Get.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    static class NodeInformSender extends Thread {

        NodeTuple nodeTuple;
        NodeInform nodeInform;

        public NodeInformSender(NodeTuple nodeTuple, NodeInform nodeInform) {
            super("NodeInformSenderThread");
            this.nodeTuple = nodeTuple;
            this.nodeInform = nodeInform;
        }

        @Override
        public void run() {
            try (Socket senderSocket = new Socket(nodeTuple.getHostName(), nodeTuple.getPort());
                    ObjectOutputStream outStream = new ObjectOutputStream(senderSocket.getOutputStream())) {
                System.out.println("Sending NodeInform to: " + nodeTuple.getHostName() + " port: " + nodeTuple.getPort());
                outStream.writeObject(nodeInform);

            } catch (ConnectException ce) {
                System.err.println(ce.getClass() + " - " + ce.getMessage());
            } catch (IOException ex) {
                Logger.getLogger(Get.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    static class NodeRequestSender extends Thread {

        String hostName;
        int port;

        NodeRequest nodeRequest;

        public NodeRequestSender(String hostName, int port, NodeRequest nodeRequest) {
            super("NodeInformSenderThread");
            this.hostName = hostName;
            this.port = port;
            this.nodeRequest = nodeRequest;
        }

        @Override
        public void run() {
            try (Socket senderSocket = new Socket(hostName, port);
                    ObjectOutputStream outStream = new ObjectOutputStream(senderSocket.getOutputStream())) {
                System.out.println("Sending NodeRequest to: " + hostName + " port: " + port);
                outStream.writeObject(nodeRequest);

            } catch (ConnectException ce) {
                System.err.println(ce.getClass() + " - " + ce.getMessage());
            } catch (IOException ex) {
                Logger.getLogger(Get.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}

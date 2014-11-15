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
 * STUFF TO FIX SENDS PUTS INCOMING PUTS GET SENT AS PUTS AND INFINITE LOOP
 * INBOUND
 *
 * @author Sjurdur
 */
public class Node {

    // Kun indsætte eller overskrive, ikke slette.
    public static Map<Integer, String> messages = Collections.synchronizedMap(new HashMap<>());

    public static Set<NodeTuple> nt = Collections.synchronizedSet(new HashSet<>());

    public static NodeTuple thisNode;

    public static void main(String[] args) throws Exception {

        int localPort;
        String localhost = "localhost";
        InetAddress nodeIP;
        int nodePort;

        if (args.length == 1) {
            System.out.println("Argument 1");
            localPort = Integer.parseInt(args[0]);
            thisNode = new NodeTuple(localPort, localhost);

            new ListenerServer(localPort).start();

        } else if (args.length == 3) {
            System.out.println("Argument 3");

            localPort = Integer.parseInt(args[0]);
            nodeIP = InetAddress.getByName(args[1]);
            nodePort = Integer.parseInt(args[2]);

            thisNode = new NodeTuple(localPort, localhost);

            nt.add(new NodeTuple(nodePort, nodeIP.getHostName()));

            new NodeRequestSender(nodeIP.getHostName(), nodePort, new NodeRequest(localhost, localPort)).start();

            new ListenerServer(localPort).start();

        } else {
            System.out.println("Incorrect number of arguments.");
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

                } catch (IOException ex) {
                    Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
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
            if (!n.equals(thisNode)) {
                nt.add(n);
            }
        }

        /**
         * Add a collection of NodeTuples to the common Collection of NodeTuples
         *
         * @param nodeTuples The Collection of NodeTuples to add.
         */
        public synchronized void addNodeTuples(Set<NodeTuple> nodeTuples) {
            nodeTuples.remove(thisNode);
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
                    ObjectInputStream is = new ObjectInputStream(listenerSocket.getInputStream());) {

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

                    PutList putList = new PutList(messages);
                    new PutListSender(nodeRequestIP, nodeRequestPort, putList).start();
                } else if (obj instanceof NodeInform) {
                    System.out.println("NodeInform RECEIVED");

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
                    // Send nodeinform to all Nodes known by this Node.
                    Set syncSet = Collections.synchronizedSet(nt);
                    synchronized (syncSet) {
                        Iterator i = syncSet.iterator();
                        // Display elements
                        while (i.hasNext()) {
                            NodeTuple n = (NodeTuple) i.next();
                            System.out.println("Are they equal: " + n.getHostName() + n.getPort() + " + " + thisNode.getHostName() + thisNode.getPort() + " = " + n.equals(thisNode));
                            if (!n.equals(thisNode)) {

                                PutList putList = new PutList(messages);
                                new PutListSender(n.getHostName(), n.getPort(), putList).start();
                            }
                        }
                    }
                } else if (obj instanceof PutList) {
                    System.out.println("PutList RECEIVED");

                    PutList putList = (PutList) obj;

                    for (Map.Entry pairs : putList.messages.entrySet()) {
                        addPutMessage((Integer) pairs.getKey(), (String) pairs.getValue());
                    }
                }

                listenerSocket.close();

            } catch (IOException ex) {
                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    static class PutListSender extends Thread {

        String hostName;
        int port;
        PutList putList;

        public PutListSender(String hostName, int port, PutList putList) {
            this.hostName = hostName;
            this.port = port;
            this.putList = putList;
        }

        @Override
        public void run() {
            try (Socket senderSocket = new Socket(hostName, port);
                    ObjectOutputStream outStream = new ObjectOutputStream(senderSocket.getOutputStream())) {
                System.out.println("Sending PutList to: " + hostName + " port: " + port);
                outStream.writeObject(putList);

            } catch (ConnectException ce) {
                Logger.getLogger(Get.class.getName()).log(Level.SEVERE, null, ce);
            } catch (IOException ex) {
                Logger.getLogger(Get.class.getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(Get.class.getName()).log(Level.SEVERE, null, ce);
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
                Logger.getLogger(Get.class.getName()).log(Level.SEVERE, null, ce);
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
                Logger.getLogger(Get.class.getName()).log(Level.SEVERE, null, ce);
            } catch (IOException ex) {
                Logger.getLogger(Get.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}

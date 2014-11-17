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
 * @author Sjúrður í Sandagerði
 * @author Nicolai Thorndahl
 * @author Ans Uddin
 */
public class Node {

    public final static Map<Integer, String> messages = Collections.synchronizedMap(new HashMap<>());

    public final static Set<NodeTuple> nodeTuples = Collections.synchronizedSet(new HashSet<>());

    public static NodeTuple thisNode;

    public static void main(String[] args) throws Exception {

        int localPort;
        String localhost = "localhost";
        InetAddress nodeIP;
        int nodePort;

        if (args.length == 1) {
            localPort = Integer.parseInt(args[0]);
            thisNode = new NodeTuple(localPort, localhost);

            new ListenerServer(localPort).start();

        } else if (args.length == 3) {
            localPort = Integer.parseInt(args[0]);
            nodeIP = InetAddress.getByName(args[1]);
            nodePort = Integer.parseInt(args[2]);

            thisNode = new NodeTuple(localPort, localhost);

            nodeTuples.add(new NodeTuple(nodePort, nodeIP.getHostName()));

            new NodeRequestSender(nodeIP.getHostName(), nodePort, new NodeRequest(localhost, localPort)).start();

            new ListenerServer(localPort).start();

        } else {
            System.out.println("Incorrect number of arguments.");
        }

    }

    /**
     * Sends a message telling other Nodes to remove a specific node from their
     * list of Nodes.
     *
     * @param nodeRemove The NodeTuple to remove
     */
    public static synchronized void SendNodeRemoveToAllNodes(NodeRemove nodeRemove) {
        // Send NodeRemove to all Nodes known by this Node.
        synchronized (nodeTuples) {
            nodeTuples.remove(nodeRemove);

            Iterator i = nodeTuples.iterator();
            while (i.hasNext()) {
                NodeTuple n = (NodeTuple) i.next();
                if (!n.equals(thisNode)) {
                    new NodeRemoveSender(n.getHostName(), n.getPort(), nodeRemove).start();
                }
            }
        }
    }

    /**
     * Send a PutList to all Nodes.
     *
     * @param putList The PutList to send.
     */
    public static synchronized void SendPutListToAllNodes(PutList putList) {
        synchronized (nodeTuples) {
            Iterator i = nodeTuples.iterator();
            // Display elements
            while (i.hasNext()) {
                NodeTuple n = (NodeTuple) i.next();
                if (!n.equals(thisNode)) {
                    new PutListSender(n.getHostName(), n.getPort(), putList).start();
                }
            }
        }
    }

    /**
     * Send NodeInform to all Nodes known by this Node.
     *
     * @param nodeInform The NodeInform object to send.
     */
    public static synchronized void SendNodeInformToAllNodes(NodeInform nodeInform) {
        synchronized (nodeTuples) {
            Iterator i = nodeTuples.iterator();
            // Display elements
            while (i.hasNext()) {
                NodeTuple n = (NodeTuple) i.next();
                new NodeInformSender(n, nodeInform).start();
            }
        }
    }

    /**
     * Add a NodeTuple to the common Collection of NodeTuples
     *
     * @param n NodeTuple to add.
     */
    public static synchronized void addNodeTuple(NodeTuple n) {
        if (!n.equals(thisNode)) {
            nodeTuples.add(n);
        }
    }

    /**
     * Add a collection of NodeTuples to the common Collection of NodeTuples
     *
     * @param nodeTuples The Collection of NodeTuples to add.
     */
    public static synchronized void addNodeTuples(Set<NodeTuple> nodeTuples) {
        nodeTuples.remove(thisNode);
        Node.nodeTuples.addAll(nodeTuples);
    }

    /**
     * Add a message to the messages collection.
     *
     * @param key The key of the message
     * @param message The content of the message
     */
    public static synchronized void addPutMessage(int key, String message) {
        messages.put(key, message);
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
                    client = ss.accept();
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

        @Override
        public void run() {
            try (
                    ObjectInputStream is = new ObjectInputStream(listenerSocket.getInputStream());) {

                Object obj = is.readObject();

                if (obj instanceof NodeRequest) {
                    NodeRequest nodeRequest = (NodeRequest) obj;

                    int nodeRequestPort = nodeRequest.port;
                    String nodeRequestIP = nodeRequest.ip;
                    NodeTuple nodeTuple = new NodeTuple(nodeRequestPort, nodeRequestIP);
                    addNodeTuple(nodeTuple);

                    NodeInform nodeInform = new NodeInform(nodeTuples);
                    SendNodeInformToAllNodes(nodeInform);

                    PutList putList = new PutList(messages);
                    new PutListSender(nodeRequestIP, nodeRequestPort, putList).start();
                } else if (obj instanceof NodeInform) {
                    NodeInform nodeInform = (NodeInform) obj;
                    addNodeTuples(nodeInform.nt);

                } else if (obj instanceof GetRequest) {
                    GetRequest getMessage = (GetRequest) obj;
                    String message = messages.get(getMessage.key);

                    // Send Put back to Get
                    if (message != null) {
                        new PutSender(getMessage.hostName, getMessage.port, message).start();
                    }

                } else if (obj instanceof PutRequest) {
                    PutRequest putRequest = (PutRequest) obj;
                    int key = putRequest.key;

                    String message = putRequest.value;

                    addPutMessage(key, message);

                    PutList putList = new PutList(messages);
                    SendPutListToAllNodes(putList);

                } else if (obj instanceof PutList) {
                    PutList putList = (PutList) obj;

                    for (Map.Entry pairs : putList.messages.entrySet()) {
                        addPutMessage((Integer) pairs.getKey(), (String) pairs.getValue());
                    }
                } else if (obj instanceof NodeRemove) {
                    NodeRemove nodeRemove = (NodeRemove) obj;
                    nodeTuples.remove(nodeRemove);
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
                outStream.writeObject(putList);

            } catch (ConnectException ce) {
                NodeRemove nodeRemove = new NodeRemove(port, hostName);
                SendNodeRemoveToAllNodes(nodeRemove);
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
                outStream.writeObject(new PutRequest(key, message));

            } catch (ConnectException ce) {
                NodeRemove nodeRemove = new NodeRemove(port, hostName);
                SendNodeRemoveToAllNodes(nodeRemove);
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
                outStream.writeObject(nodeInform);

            } catch (ConnectException ce) {
                NodeRemove nodeRemove = new NodeRemove(nodeTuple.getPort(), nodeTuple.getHostName());
                SendNodeRemoveToAllNodes(nodeRemove);
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
                outStream.writeObject(nodeRequest);

            } catch (ConnectException ce) {
                NodeRemove nodeRemove = new NodeRemove(port, hostName);
                SendNodeRemoveToAllNodes(nodeRemove);
            } catch (IOException ex) {
                Logger.getLogger(Get.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    static class NodeRemoveSender extends Thread {

        String hostName;
        int port;

        NodeTuple nodeTuple;

        public NodeRemoveSender(String hostName, int port, NodeTuple nodeTuple) {
            super("NodeRemoveSenderThread");
            this.hostName = hostName;
            this.port = port;
            this.nodeTuple = nodeTuple;
        }

        @Override
        public void run() {
            try (Socket senderSocket = new Socket(hostName, port);
                    ObjectOutputStream outStream = new ObjectOutputStream(senderSocket.getOutputStream())) {
                outStream.writeObject(nodeTuple);

            } catch (ConnectException ce) {
                NodeRemove nodeRemove = new NodeRemove(port, hostName);
                SendNodeRemoveToAllNodes(nodeRemove);
            } catch (IOException ex) {
                Logger.getLogger(Get.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}

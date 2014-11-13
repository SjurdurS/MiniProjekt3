/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package miniprojekt3;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author Sjurdur
 */
public class Node {

    // Kun indsætte eller overskrive, ikke slette.
    public static HashMap<Integer, String> messages = new HashMap<>();

    public static HashSet<NodeTuple> nt = new HashSet<>();

    public static void main(String[] args) throws Exception {

        int localPort = 1025;
        InetAddress localhost = InetAddress.getLocalHost();
        InetAddress nodeIP = InetAddress.getLocalHost();
        int nodePort = 1026;

        if (args.length == 1) {
            localPort = Integer.parseInt(args[0]);
            new ListenerServer(localPort);
        }
        if (args.length == 3) {
            localPort = Integer.parseInt(args[0]);
            nodeIP = InetAddress.getByName(args[1]);
            nodePort = Integer.parseInt(args[2]);

            boolean b = nt.add(new NodeTuple(nodePort, nodeIP.getHostName()));

            new ListenerServer(localPort);
            
        } else {
            throw new Exception("Incorrect number of arguments.");
        }
    }

    static class ListenerServer extends Thread {

        ServerSocket ss;
        Socket client = null;

        public ListenerServer(int localPort) throws IOException {
            this.ss = new ServerSocket(localPort);
            this.start();
        }

        public void run() {
            while (true) {
                try {
                    client = ss.accept();
                    System.out.println("Connected to Node : " + client.getInetAddress().getHostName());

                    new ListenerThread(client);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    static class ListenerThread extends Thread {

        Socket listenerSocket = null;

        ListenerThread(Socket listenerSocket) {
            this.listenerSocket = listenerSocket;
            this.start();
        }

        public void run() {
            try (
                    ObjectInputStream is = new ObjectInputStream(listenerSocket.getInputStream());
                    OutputStream os = System.out;) {

                Object obj = null;

                while (true) {
                    obj = is.readObject();

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
                        System.out.println("Node Port: " + listenerSocket.getPort() + "\nGET MESSAGE RECEIVED.\n");

                        // Send Put back to Get
                    } else if (obj instanceof PutRequest) {
                        PutRequest putMessage = (PutRequest) obj;

                        messages.put(putMessage.key, putMessage.value);
                        System.out.println("Node Port: " + listenerSocket.getPort() + "\nPUT MESSAGE RECEIVED.\n");

                        // Send Put to all NodeTuples.
                    }
                }
            } catch (Exception ex) {
                System.out.println("Connection died:" + ex.getMessage());
            }
        }
    }
}

package miniprojekt3;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Sjurdur
 */
public class Get {

    public static void main(String[] args) throws Exception {

        // Get request
        InetAddress nodeIP = InetAddress.getLocalHost();
        int nodePort;
        int key;

        // Client
        String localHost = InetAddress.getLocalHost().getHostAddress();
        int localPort;

        if (args.length == 4) {
            localPort = Integer.parseInt(args[0]);
            nodeIP = InetAddress.getByName(args[1]);
            nodePort = Integer.parseInt(args[2]);
            key = Integer.parseInt(args[3]);

            new GetSender(nodeIP, nodePort, key, localHost, localPort);
            new PutListener(localPort);

        } else {
            throw new Exception("Incorrect number of arguments.");
        }

    }

    static class GetSender extends Thread {

        // Receiver
        InetAddress nodeIP = null;
        int nodePort = 0;

        //Message
        int key = 0;
        String localHost = null;
        int localPort = 0;

        Socket client = null;

        public GetSender(InetAddress nodeIP, int nodePort, int key, String localHost, int localPort) throws IOException {
            this.nodeIP = nodeIP;
            this.nodePort = nodePort;

            this.key = key;
            this.localHost = localHost;
            this.localPort = localPort;

            this.start();
        }

        public void run() {
            try {
                Socket senderSocket = new Socket(nodeIP, nodePort);
                ObjectOutputStream outStream = new ObjectOutputStream(senderSocket.getOutputStream());

                System.out.println("Sending Get Request object to Node at: " + nodeIP + " port: " + nodePort);
                outStream.writeObject(new GetRequest(key, localHost, localPort));
                outStream.flush();
                outStream.close();

            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    static class PutListener extends Thread {

        ServerSocket ss;
        Socket client = null;

        public PutListener(int sinkPort) throws IOException {
            this.ss = new ServerSocket(sinkPort);
            this.start();
        }

        public void run() {
            try {
                client = ss.accept();
                System.out.println("Connected to Sink : " + client.getInetAddress().getHostName());

                ObjectInputStream is = new ObjectInputStream(client.getInputStream());

                Object o = is.readObject();
                if (o instanceof PutRequest) {
                    PutRequest p = (PutRequest) o;

                    System.out.println("Message received: " + p.value);
                }

                is.close();

            } catch (Exception e) {
                System.err.println(e.getMessage());
            }

        }
    }
}

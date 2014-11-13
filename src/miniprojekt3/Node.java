/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package miniprojekt3;

import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
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

    public static HashSet<NodeTuple> tuple = new HashSet<>();

    public static void main(String[] args) throws Exception {

        int localPort = 1025;
        InetAddress localhost = InetAddress.getLocalHost();
        InetAddress nodeIP = InetAddress.getLocalHost();
        int nodePort = 1026;

        if (args.length == 0) {
            //localPort = Integer.parseInt(args[0]);

            Socket receiverSocket = new Socket(localhost, localPort);

            try (
                    ObjectInputStream is = new ObjectInputStream(receiverSocket.getInputStream());
                    OutputStream os = System.out;) {

                Object obj = null;

                while (true) {
                    obj = is.readObject();

                    if (obj instanceof GetRequest) {
                        GetRequest getMessage = (GetRequest) obj;
                        //Do logic here.
                        String message = messages.get(getMessage.key);
                        System.out.println("MESSAGE RECEIVED MOTHER FUCKER: " + message);
                        System.out.println("Node Port: " + localPort + "\nGET MESSAGE RECEIVED.\n");
                    } else if (obj instanceof PutRequest) {
                        PutRequest putMessage = (PutRequest) obj;

                        messages.put(putMessage.key, putMessage.value);
                        System.out.println("Node Port: " + localPort + "\nPUT MESSAGE RECEIVED.\n");
                    }
                }
            } catch (Exception ex) {
                System.out.println("Connection died:" + ex.getMessage());
            }

        } if (args.length == 0) {
            //localPort = Integer.parseInt(args[0]);
            //nodeIP = InetAddress.getByName(args[1]);
            //nodePort = Integer.parseInt(args[2]);

            boolean b = tuple.add(new NodeTuple(nodePort, nodeIP.getHostName()));

        } else {
            throw new Exception("Incorrect number of arguments.");
        }
    }
}

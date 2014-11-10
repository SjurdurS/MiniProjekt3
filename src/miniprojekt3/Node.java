/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package miniprojekt3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

/**
 *
 * @author Sjurdur
 */
public class Node {

    // Kun indsætte eller overskrive, ikke slette.
    public HashMap<Integer, String> messages = new HashMap<>();

    public static void main(String[] args) throws Exception {

        int localPort = 1025;
        InetAddress localhost = InetAddress.getLocalHost();
        InetAddress nodeIP = InetAddress.getLocalHost();
        int nodePort = 1026;

        if (args.length == 1) {
            localPort = Integer.parseInt(args[0]);

            Socket receiverSocket = new Socket(localhost, localPort);

            try (
                    InputStream is = receiverSocket.getInputStream();
                    OutputStream os = System.out;) {

                byte[] Buf = new byte[1024];

                int eof;
                do {
                    eof = is.read(Buf);
                    if (eof > 0) {
                        os.write(Buf, 0, eof);
                    }
                } while (eof >= 0);

            } catch (IOException ex) {
                System.out.println("Connection died:" + ex.getMessage());
            }

        } else if (args.length == 3) {
            localPort = Integer.parseInt(args[0]);
            nodeIP = InetAddress.getByName(args[1]);
            nodePort = Integer.parseInt(args[2]);
        } else {
            throw new Exception("Incorrect number of arguements.");
        }

    }

}

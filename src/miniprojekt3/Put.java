/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package miniprojekt3;

import java.net.InetAddress;

/**
 *
 * @author Sjurdur
 */
public class Put {

    public static void main(String[] args) throws Exception {

        InetAddress nodeIP = InetAddress.getLocalHost();
        int nodePort;
        int key;
        String value;

        if (args.length == 4) {
            nodeIP = InetAddress.getByName(args[0]);
            nodePort = Integer.parseInt(args[1]);
            key = Integer.parseInt(args[2]);
            value = args[3];
        
        } else {
            throw new Exception("Incorrect number of arguements.");
        }
        
        
    }
}

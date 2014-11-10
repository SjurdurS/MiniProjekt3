/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package miniprojekt3;

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
        String nodeIP;
        int nodePort;

        if (args.length == 1) {
            localPort = Integer.parseInt(args[0]);
        } else if (args.length == 3) {
            localPort = Integer.parseInt(args[0]);
            nodeIP = args[1];
            nodePort = Integer.parseInt(args[2]);
        } else {
            throw new Exception("Incorrect number of arguements.");
        }

    }
}

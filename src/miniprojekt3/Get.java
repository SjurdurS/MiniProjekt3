/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package miniprojekt3;

/**
 *
 * @author Sjurdur
 */
public class Get {

    public static void main(String[] args) throws Exception {

        String nodeIP;
        int nodePort;
        int key;

        if (args.length == 4) {
            nodeIP = args[0];
            nodePort = Integer.parseInt(args[1]);
            key = Integer.parseInt(args[2]);
        } else {
            throw new Exception("Incorrect number of arguements.");
        }
    }
}

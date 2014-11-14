import java.util.HashMap;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Sjurdur
 */
public class PutList implements java.io.Serializable {

    public HashMap<Integer, String> messages = new HashMap<>();

    public PutList(HashMap<Integer, String> messages) {
        this.messages = messages;
    }
}

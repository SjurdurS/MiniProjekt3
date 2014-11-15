import java.util.HashMap;
import java.util.Map;

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

    public Map<Integer, String> messages = new HashMap<>();

    public PutList(Map<Integer, String> messages) {
        this.messages = messages;
    }
}

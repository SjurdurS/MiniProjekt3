import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sjúrður í Sandagerði
 * @author Nicolai Thorndahl
 * @author Ans Uddin
 */
public class PutList implements java.io.Serializable {

    public Map<Integer, String> messages = new HashMap<>();

    public PutList(Map<Integer, String> messages) {
        this.messages = messages;
    }
    
    public PutList(int key, String value) {
        messages.put(key, value);
    }
}

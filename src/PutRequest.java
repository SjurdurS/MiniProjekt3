/**
 *
 * @author Sjúrður í Sandagerði
 * @author Nicolai Thorndahl
 * @author Ans Uddin
 */
public class PutRequest implements java.io.Serializable {

    public int key;
    public String value;

    public PutRequest(int key, String value) {
        this.key = key;
        this.value = value;
    }
}

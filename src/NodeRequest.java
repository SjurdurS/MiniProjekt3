/**
 *
 * @author Sjúrður í Sandagerði
 * @author Nicolai Thorndahl
 * @author Ans Uddin
 */
public class NodeRequest implements java.io.Serializable {

    public String ip;
    public int port;

    public NodeRequest(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

}

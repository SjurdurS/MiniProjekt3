package miniprojekt3;

/**
 *
 * @author Sjurdur
 */
public class NodeRequest implements java.io.Serializable {

    public String ip;
    public int port;

    public NodeRequest(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

}

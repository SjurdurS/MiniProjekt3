

public class GetRequest implements java.io.Serializable {
    public int key;
    public String hostName;
    public int port;

    public GetRequest(int key, String hostName, int port) {
        this.key = key;
        this.hostName = hostName;
        this.port = port;
    }
    
 
}

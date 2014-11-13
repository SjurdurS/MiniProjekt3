package miniprojekt3;

public class PutRequest implements java.io.Serializable {

    public int key;
    public String ip;
    public int port;
    public String value;

    public PutRequest(int key, String value) {
        this.key = key;
        this.value = value;
    }
}

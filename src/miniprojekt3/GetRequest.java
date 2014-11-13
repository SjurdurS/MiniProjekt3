package miniprojekt3;

public class GetRequest implements java.io.Serializable {
    public int key;
    public String value;
    
    public GetRequest(int key, String value) {
        this.key = key;
        this.value = value;
    }
}

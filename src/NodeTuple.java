/**
 *
 * @author Sjúrður í Sandagerði
 * @author Nicolai Thorndahl
 * @author Ans Uddin
 */
class NodeTuple implements java.io.Serializable {

    private int port;
    private String hostName;

    public NodeTuple(int port, String hostName) {

        this.port = port;
        this.hostName = hostName;

    }

    public int getPort() {
        return port;
    }

    public String getHostName() {
        return hostName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof NodeTuple)) {
            return false;
        }

        NodeTuple n = (NodeTuple) obj;
        return n.getHostName().equals(this.hostName) && n.getPort() == this.port;
    }

    @Override
    public int hashCode() {
        return this.port * hostName.hashCode();
    }

}

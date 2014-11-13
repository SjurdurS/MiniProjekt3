

/**
 *
 * @author Nicolai
 */
class NodeTuple {

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

        if (this == obj) {
            return true;
        }
        NodeTuple n = (NodeTuple) obj;
        if (n.getHostName().equals(this.hostName) && n.getPort() == this.port) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.port * hostName.hashCode();
    }

}

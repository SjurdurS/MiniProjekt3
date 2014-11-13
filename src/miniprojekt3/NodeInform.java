package miniprojekt3;

import java.util.HashSet;

/**
 *
 * @author Sjurdur
 */
public class NodeInform implements java.io.Serializable {

    public HashSet<NodeTuple> nt = new HashSet<>();

    public NodeInform(HashSet<NodeTuple> nt) {
        this.nt = nt;
    }
}



import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Sjurdur
 */
public class NodeInform implements java.io.Serializable {

    public Set<NodeTuple> nt = new HashSet<>();

    public NodeInform(Set<NodeTuple> nt) {
        this.nt = nt;
    }
}

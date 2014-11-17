import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Sjúrður í Sandagerði
 * @author Nicolai Thorndahl
 * @author Ans Uddin
 */
public class NodeInform implements java.io.Serializable {

    public Set<NodeTuple> nt = new HashSet<>();

    public NodeInform(Set<NodeTuple> nt) {
        this.nt = nt;
    }
}

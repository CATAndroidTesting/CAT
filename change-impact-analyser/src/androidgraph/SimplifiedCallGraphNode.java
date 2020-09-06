package androidgraph;

import java.util.*;

public class SimplifiedCallGraphNode {
    public static final int NODE_TYPE_FUNCTION = 1;
    public static final int NODE_TYPE_VIEW = 2;
    public static final int NODE_TYPE_LAYOUT = 4;
    public static final int NODE_TYPE_ACTIVITY = 8;

    private final int nodeType;
    private final String nodeValue;
    private final int uniqueID;

    private static int idCounter = 0;

    public SimplifiedCallGraphNode(int _nodeType, String _nodeValue) {
        this.nodeType = _nodeType;
        this.nodeValue = _nodeValue;
        this.uniqueID = idCounter++;
    }

    public int getUniqueID() {
        return uniqueID;
    }

    public int getNodeType() {
        return nodeType;
    }

    public String getNodeValue() {
        return nodeValue;
    }

    @Override
    public String toString() {
        return switch (this.nodeType) {
            case NODE_TYPE_FUNCTION -> "Function " + this.nodeValue;
            case NODE_TYPE_VIEW -> "View " + this.nodeValue;
            case NODE_TYPE_ACTIVITY -> "Activity " + this.nodeValue;
            case NODE_TYPE_LAYOUT -> "Layout " + this.nodeValue;
            default -> "UNKNOWN NODE " + this.nodeValue;
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimplifiedCallGraphNode that = (SimplifiedCallGraphNode) o;
        return nodeType == that.nodeType &&
                Objects.equals(nodeValue, that.nodeValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeType, nodeValue);
    }
}

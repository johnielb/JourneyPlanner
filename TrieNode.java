import java.util.*;

public class TrieNode implements Iterable<TrieNode>{
    List<Stop> stops;
    HashMap<Character, TrieNode> children;

    public TrieNode() {
        stops = new ArrayList<>();
        children = new HashMap<>();
    }

    /**
     * Get all Character keys that belong to this node's children
     * @return set of keys that bind to all of this node's children
     */
    public Set<Character> getChildChars() {
        return children.keySet();
    }

    /**
     * Get child node corresponding to input Character
     * @param c Character/key to find
     * @return child node corresponding to Character
     */
    public TrieNode getChild(Character c) {
        return children.get(c);
    }

    /**
     * Get stops belonging to this node only
     * @return List of Stops in this node
     */
    public List<Stop> getStops() {
        return stops;
    }

    /**
     * Add a new and empty child node to this current node
     * Must call addStop() to add objects to this node
     * @param c Character that binds to this new node
     */
    public void addChild(Character c) {
        children.put(c, new TrieNode());
    }

    /**
     * Add Stop belonging to this node
     * @param s Stop belonging to this node
     */
    public void addStop(Stop s) {
        stops.add(s);
    }

    /**
     * Allows us to iterate through all children of this node
     * e.g. using foreach loop
     * @return iterator of children collection
     */
    public Iterator<TrieNode> iterator() {
        return children.values().iterator();
    }
}

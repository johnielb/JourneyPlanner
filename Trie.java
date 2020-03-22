import java.util.ArrayList;
import java.util.List;

public class Trie {
    TrieNode root = new TrieNode();

    /**
     * Traverse through the trie to add a new Stop
     * @param word array of Characters that act as the key to this Stop
     * @param s Stop to add to trie
     */
    public void add(char[] word, Stop s) {
        TrieNode n = root;
        for (Character c : word) {
            if (!n.getChildChars().contains(c)) {
                n.addChild(c);
            }
            n = n.getChild(c);
        }
        n.addStop(s);
    }

    /**
     * Traverse through the trie to get a Stop given its key
     * @param word key used to find stop
     * @return Stop corresponding to key (null if not found)
     */
    public List<Stop> get(char[] word) {
        TrieNode n = root;
        for (Character c : word) {
            if (!n.getChildChars().contains(c)) {
                return null;
            }
            n = n.getChild(c);
        }
        return n.getStops();
    }

    /**
     * Get all Stops belonging to this node and its children
     * @param word prefix used to find all stops
     * @return List of Stops containing all stops belonging to node and children
     */
    public List<Stop> getAll(char[] word) {
        List<Stop> stops = new ArrayList<>();
        TrieNode n = root;
        for (Character c : word) {
            if (!n.getChildChars().contains(c)) {
                return stops; // return an empty list
            }
            n = n.getChild(c);
        }
        getAllFrom(n, stops);
        return stops;
    }

    /**
     * Helper method that collects all the stops from a node,
     * recursively calls itself to collect all the stops from each child
     * @param n Root node
     * @param stops List of Stops currently collected
     * @return List of Stops containing all stops belonging to current node and children
     */
    public List<Stop> getAllFrom(TrieNode n, List<Stop> stops) {
        stops.addAll(n.getStops());
        for (TrieNode child : n) {
            getAllFrom(child, stops);
        }
        return stops;
    }

    /**
     * Get all the keys belonging to this node
     * @return formatted String of all Character keys in node
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Character c : root.getChildChars()) {
            sb.append(c).append(", ");
        }
        return sb.toString();
    }

    /**
     * Make all the nodes 'disappear' by making a new root node
     */
    public void clear() {
        root = new TrieNode();
    }
}

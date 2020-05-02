import java.util.*;

/**
 * A QuadTreeNode makes up an equal quadrant region of its parent's region.
 * The root object contains the minimum 2D space required to represent all Stops processed onLoad()
 * Each node can store 4 stops, reducing the chance of creating tall trees.
 * Insert and subdivide methods adapted from Wikipedia: https://en.wikipedia.org/wiki/Quadtree
 * Node checking adapted from patricksurry's d3js Block: http://bl.ocks.org/patricksurry/6478178
 */

public class QuadTreeNode {
    private final Set<Stop> stops;
    final int MAX_SET_LENGTH = 4;
    private final Location origin;
    private final Location centre;
    private final Location bottomRight;
    private QuadTreeNode[] children;

    public QuadTreeNode(Location origin, Location centre) {
        this.origin = origin;
        this.centre = centre;
        this.bottomRight = new Location(centre.x + (centre.x-origin.x), centre.y - (origin.y-centre.y));
        stops = new HashSet<>();
    }

    /**
     * Divide a leaf node into 4 leaf nodes.
     * x---a---+---b---+    x - origin
     * |       |       |    o - centre
     * c   0   |   1   |    a - quarterX
     * |       |       |    b - threeQuarterX
     * +-------o-------+    c - quarterY
     * |       |       |    d - threeQuarterY
     * d   2   |   3   |
     * |       |       |
     * +-------+-------+
     */
    public void subdivide() {
        assert children == null : "Subdividing a non-leaf node.";
        double quarterX = origin.x + (centre.x-origin.x)/2;
        double quarterY = origin.y - (origin.y-centre.y)/2;
        double threeQuarterX = centre.x + (centre.x-origin.x)/2;
        double threeQuarterY = centre.y - (origin.y-centre.y)/2;

        children = new QuadTreeNode[4];
        children[0] = new QuadTreeNode(origin, new Location(quarterX, quarterY));
        children[1] = new QuadTreeNode(new Location(centre.x, origin.y), new Location(threeQuarterX, quarterY));
        children[2] = new QuadTreeNode(new Location(origin.x, centre.y), new Location(quarterX, threeQuarterY));
        children[3] = new QuadTreeNode(centre, new Location(threeQuarterX, threeQuarterY));

        if (stops.size() > 0) {
            // remove stop from node
            for (Stop s : stops) {
                if (children[0].insert(s)) continue;
                if (children[1].insert(s)) continue;
                if (children[2].insert(s)) continue;
                if (children[3].insert(s)) continue;
                return;
            }
            stops.clear();
        }
    }

    /**
     * Insert stop into quad tree
     *  - Ensure stop actually belongs inside this current node
     *  - Only insert stop if node has space and is a child node
     *  - Otherwise, try to insert it into one of the 4 child nodes, making them if they don't exist
     * @param s Stop to add
     * @return boolean flag to previous insert call indicating success of insertion
     */
    public boolean insert(Stop s) {
        if (outsideBounds(s.getLoc())) return false;
        if (stops.size() < MAX_SET_LENGTH && children == null) { // if node has space and is a child node
            stops.add(s);
            return true;
        }
        if (children == null) subdivide(); // if child node, make 4 child nodes underneath

        // attempt to insert stop in each of the 4 child nodes
        if (children[0].insert(s)) return true;
        if (children[1].insert(s)) return true;
        if (children[2].insert(s)) return true;
        return children[3].insert(s);
    }

    /**
     * Determines what order of this node's children to search through next.
     * @param mouse click event containing its coordinates
     * @return a list ordered by the least likely to have candidates first, putting those most likely on top of stack
     */
    public List<QuadTreeNode> getNodesToCheck(Location mouse) {
        List<QuadTreeNode> ans = new ArrayList<>();
        if (children != null) {
            int rightOrLeft = 2*mouse.x > origin.x + bottomRight.x ? 1 : 0;
            int topOrBottom = 2*mouse.y < origin.y + bottomRight.y ? 1 : 0;

            // e.g. if mouse inside NW, i1 will be the NW node,
            // i2 will be the NE node, i3 SW, i4 SE which is the furthest away from the mouse
            int i1 = topOrBottom*2 + rightOrLeft;
            int i2 = topOrBottom*2 + (1-rightOrLeft);
            int i3 = (1-topOrBottom)*2 + rightOrLeft;
            int i4 = (1-topOrBottom)*2 + (1-rightOrLeft);

            // reverse ordering puts the most likely on top of stack
            if (children[i4] != null) ans.add(children[i4]);
            if (children[i3] != null) ans.add(children[i3]);
            if (children[i2] != null) ans.add(children[i2]);
            if (children[i1] != null) ans.add(children[i1]);
        }
        return ans;
    }

    /**
     * Find a Stop that is both closer to the mouse click than the current best candidate,
     * and the closest of the stops contained in this node.
     * @param mouse click event containing its coordinates
     * @param bestDistance current distance from mouse click of the best candidate
     * @return Stop that fulfils both criteria, can be null if none fulfilled
     */
    public Stop getBestStop(Location mouse, double bestDistance) {
        Stop bestStop = null;
        if (!stops.isEmpty()) {
            for (Stop s : stops) {
                double distance = s.getLoc().distance(mouse);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestStop = s;
                }
            }
        }
        return bestStop;
    }

    /**
     * Checks if a Location is outside any of the 4 edges of the bounding box that defines
     * a QuadTreeNode's region.
     * @param point Location being checked
     * @return if Location is out of bounds (true) or not
     */
    private boolean outsideBounds(Location point) {
        return point.x < origin.x || point.x > bottomRight.x ||
                point.y > origin.y || point.y < bottomRight.y;
    }

    /**
     * Checks if this node is not a candidate to have Stops potentially having
     * a smaller distance than the current best distance
     * @param mouse Location where mouse was clicked
     * @param dist current best distance
     * @return if Location is outside the current best distance
     */
    public boolean outsideBestDistance(Location mouse, double dist) {
        return mouse.x < origin.x - dist || mouse.x > bottomRight.x + dist ||
                mouse.y > origin.y + dist || mouse.y < bottomRight.y - dist;
    }
}

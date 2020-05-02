import java.awt.*;
import java.util.*;
import java.util.List;

public class Stop {
    private final String id;
    private final String name;
    private final Location loc;
    private final List<Connection> incoming;
    private final List<Connection> outgoing;
    private final Color color;
    private static double stopSize = 3;

    public Stop(String id, String name, double lat, double lon) {
        this.id = id;
        this.name = name;
        this.loc = Location.newFromLatLon(lat, lon);
        this.incoming = new ArrayList<>();
        this.outgoing = new ArrayList<>();
        Random random = new Random();
        this.color = new Color(0,0, random.nextFloat());
    }

    /**
     * Objects are equivalent if their (unique) IDs are equal.
     * @param o object to be compared
     * @return boolean if equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stop stop = (Stop) o;
        return id.equals(stop.id);
    }

    /**
     * Objects return same hash code if their (unique) IDs have the same hash code.
     * @return hash code of ID
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Return formatted string with its relevant identifying characteristics: stop name and ID
     * @return stop's identifying characteristics
     */
    @Override
    public String toString() {
        return "Stop Name: " + name + "; ID: " + id;
    }

    /**
     * Draw lines/connections connecting the Stops
     * @param g Graphics from GUI class
     * @param origin Location field for top left corner from JourneyPlanner
     * @param scale pixels per km from JourneyPlanner
     */
    public void drawConnections(Graphics g, Location origin, double scale, Set<Trip> trips) {
        for (Connection c : outgoing) {
            c.draw(g, origin, scale, trips);
        }
    }

    /**
     * Draw filled ovals representing Stops
     * Radius/diameter are cast to ints as STOP_SIZE is a double so it can
     * be resized by ZOOM_FACTOR without truncating info
     * @param g Graphics from GUI class
     * @param origin Location field for top left corner from JourneyPlanner
     * @param scale pixels per km from JourneyPlanner
     */
    public void draw(Graphics g, Location origin, double scale) {
        g.setColor(color);
        Point p = loc.asPoint(origin, scale);
        int radius = (int) stopSize /2;
        int diameter = (int) stopSize;
        g.fillOval(p.x-radius, p.y-radius, diameter, diameter);
    }

    /**
     * Highlight selected stop by drawing red oval around it.
     * Radius/diameter are cast to ints as STOP_SIZE is a double so it can
     * be resized by ZOOM_FACTOR without truncating info
     * @param g Graphics from GUI class
     * @param origin Location field for top left corner from JourneyPlanner
     * @param scale pixels per km from JourneyPlanner
     */
    public void highlight(Graphics g, Location origin, double scale) {
        g.setColor(Color.red);
        Point p = loc.asPoint(origin, scale);
        int radius = (int) stopSize /2;
        g.drawOval(p.x-radius, p.y-radius, radius*2, radius*2);
    }

    // GETTERS
    /**
     * Return human friendly name of Stop
     * @return name string
     */
    public String getName() { return name; }

    /**
     * Return coordinates of this stop
     * @return Location containing coordinates
     */
    public Location getLoc() { return loc; }

    /**
     * Return all Trips going to and from this Stop
     * @return HashSet of Trips in this stop
     */
    public Set<Trip> getTrips() {
        Set<Trip> ans = new HashSet<>();
        for (Connection i : incoming) ans.add(i.getTrip());
        for (Connection o : outgoing) ans.add(o.getTrip());
        return ans;
    }

    /**
     * Return a string of all Trips going through this stop.
     * Newline delimited for quick use in GUI text area.
     * @return formatted string with trips
     */
    public String tripsToString() {
        StringBuilder ans = new StringBuilder();
        Set<Trip> trips = getTrips();
        for (Trip t : trips) {
            ans.append("\n* ").append(t);
        }
        if (ans.length() == 0) return "None";
        return ans.toString();
    }

    // SETTERS

    /**
     * Add an incoming connection to this Stop
     * @param c connection to add
     */
    public void addIncomingConnection(Connection c) {
        incoming.add(c);
    }

    /**
     * Add an outgoing connection to this Stop
     * @param c connection to add
     */
    public void addOutgoingConnection(Connection c) {
        outgoing.add(c);
    }

    /**
     * Enables zooming in by changing the pixel size of Stops by a certain factor.
     * Implemented for my own aesthetic purposes (stop size shouldn't change proportionally
     * with the scale as they get very big, see JourneyPlanner.zoom())
     * @param factor zoom factor to multiply stop size by
     */
    public static void changeStopSize(double factor) { stopSize *= factor; }

    /**
     * Resets stop size to 3 - an arbitrary normal size, when resetting view,
     * may be different with a different sized map.
     */
    public static void resetStopSize() { stopSize = 3; }
}
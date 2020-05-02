import java.awt.*;
import java.util.Set;

public class Connection {
    private final Stop from;
    private final Stop to;
    private final Trip trip;

    public Connection(Stop from, Stop to, Trip trip) {
        this.from = from;
        this.to = to;
        this.trip = trip;
    }

    // GETTERS

    /**
     * Return trip this connection belongs to
     * @return trip object
     */
    public Trip getTrip() { return trip; }

    /**
     * Draw gray line connecting two stops, red line if highlighted
     * @param g Graphics from GUI class
     * @param origin Location field for top left corner from JourneyPlanner
     * @param scale pixels per km from JourneyPlanner
     */
    public void draw(Graphics g, Location origin, double scale, Set<Trip> trips) {
        if (trips.contains(trip)) g.setColor(Color.red);
        else g.setColor(Color.gray);
        Point start = from.getLoc().asPoint(origin, scale);
        Point end = to.getLoc().asPoint(origin, scale);
        g.drawLine(start.x, start.y, end.x, end.y);
    }
}

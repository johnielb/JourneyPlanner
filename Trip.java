import java.util.List;
import java.util.Objects;

public class Trip {
    private final String id;
    private final List<Stop> stops;

    public Trip(String id, List<Stop> stops) {
        this.id = id;
        this.stops = stops;
    }

    // GETTERS
    /**
     * Return all Stops belonging to this trip
     * @return ArrayList of Stops in this trip
     */
    public List<Stop> getStops() {
        return stops;
    }

    /**
     * Return a Trip's only relevant identifying characteristic: trip ID
     * @return trip's ID
     */
    @Override
    public String toString() { return id; }

    /**
     * Objects are equivalent if their (unique) IDs are equal.
     * @param o object to be compared
     * @return boolean if equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trip trip = (Trip) o;
        return Objects.equals(id, trip.id);
    }

    /**
     * Objects return same hash code if their (unique) IDs have the same hash code.
     * @return hash code of ID
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

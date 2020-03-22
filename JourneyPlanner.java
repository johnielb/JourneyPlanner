import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Visualisation of Darwin bus routes
 * @author Johniel Bocacao
 */
public class JourneyPlanner extends GUI {

	private final Map<String, Stop> stopsByID = new HashMap<>();
	private final Trie stopsByName = new Trie();
	private final Set<Trip> trips = new HashSet<>();
	private QuadTreeNode qtRoot;

	// top left corner of map (maxLeft, minTop)
	private Location origin;
	private Location tempOrigin;
	// where the mouse was pressed
	private Point mousePress;
	// pixels per km, based on y-axis as the window height abides by GUI defaults
	private double scale;
	// units in kms
	private double width;
	private double height;

	// highlighted stops by mouse/search input
	private Set<Stop> highlightedStops = new HashSet<>();
	// highlighted trips based on highlighted stops
	private Set<Trip> highlightedTrips = new HashSet<>();

	public JourneyPlanner() {
		redraw();
	}

	/**
	 * First draw connections to ensure they're underneath the stops
	 * Then draw filled Stop circles, with red borders if highlighted
	 * @param g Graphics object as required by GUI class
	 */
	@Override
	protected void redraw(Graphics g) {
		for (Stop s : stopsByID.values()) {
			s.drawConnections(g, origin, scale, highlightedTrips);
		}
		for (Stop s : stopsByID.values()) {
			s.draw(g, origin, scale);
			if (highlightedStops.contains(s)) s.highlight(g, origin, scale);
		}
	}

	/**
	 * Find the first Stop that is a `radius` distance away from the mouse click,
	 * i.e. mouse click inside this first Stop
	 * Searches backwards from List to find the most recently drawn circle that satisfies this condition.
	 * @param e MouseEvent containing the x/y coords
	 */
	@Override
	protected void onRelease(MouseEvent e) {
		if (origin == null) return; // not ready to process click
		Location releaseLoc = Location.newFromPoint(new Point(e.getX(), e.getY()), origin, scale);
		Location pressLoc = Location.newFromPoint(mousePress, origin, scale);
		mousePress = null;
		tempOrigin = null;

		// don't find a stop mouse dragged over 3 pixels
		if (!releaseLoc.isClose(pressLoc, 3/scale)) return;

		highlightedStops.clear();
		highlightedTrips.clear();
		getTextOutputArea().setText("");

		Stack<QuadTreeNode> fringe = new Stack<>();
		fringe.add(qtRoot);

		double bestDistance = height+width;
		Stop bestStop = null;
		Set<QuadTreeNode> visited = new HashSet<>();
		//int visitCount = 0;

		while (!fringe.isEmpty()) {
			QuadTreeNode current = fringe.pop();
			if (visited.contains(current) ||
					current.outsideBestDistance(releaseLoc, bestDistance)) continue;
			visited.add(current);

			Stop currentBestStop = current.getBestStop(releaseLoc, bestDistance);
			if (currentBestStop != null) { // if not null, guaranteed to be the better than best distance
				bestDistance = currentBestStop.getLoc().distance(releaseLoc);
				bestStop = currentBestStop;
			}
			fringe.addAll(current.getNodesToCheck(releaseLoc));
			//visitCount++;
		}

		//System.out.println("Visited nodes: "+visitCount);
		if (bestStop != null) {
			highlightedStops.add(bestStop);
			stopInfo();
		}
		redraw();
	}

	/**
	 * Establishes where the mouse was first clicked
	 * @param e click event containing coordinates of mouse originally
	 */
	protected void onPress(MouseEvent e) {
		if (origin == null) return;

		mousePress = new Point(e.getX(), e.getY());
		tempOrigin = new Location(origin.x, origin.y);
	}

	/**
	 * Get the difference between the press and drag coordinates, and move the origin from the
	 * original origin before the click according to this difference.
	 * @param e drag event containing coordinates of the mouse currently
	 */
	protected void onDrag(MouseEvent e) {
		if (mousePress == null) return;

		Point mouseDrag = new Point(e.getX(), e.getY());
		int dx = mousePress.x - mouseDrag.x;
		int dy = mousePress.y - mouseDrag.y;

		origin = tempOrigin.moveBy(dx/scale, -dy/scale);
		redraw();
	}

	/**
	 * Traverse trie using the given query in search box - CASE-INSENSITIVE
	 * Try to find an exact match first and display its info,
	 * otherwise find by prefix and list all Stops found.
	 */
	@Override
	protected void onSearch() {
		highlightedTrips.clear();

		String query = getSearchBox().getText().toLowerCase();
		List<Stop> exactStops = stopsByName.get(query.toCharArray());

		// if there is an exact match
		if (exactStops != null && !exactStops.isEmpty()) {
			getTextOutputArea().setText(" == Exact matches found == \n");
			highlightedStops = new HashSet<>(exactStops);
			stopInfo();
		} else { // find using query as prefix
			getTextOutputArea().setText(" == Stations matching query == ");
			highlightedStops = new HashSet<>(stopsByName.getAll(query.toCharArray()));
			// if stops are found, print each Stop's info
			if (!highlightedStops.isEmpty()) {
				for (Stop s : highlightedStops) {
					getTextOutputArea().append("\n"+s.toString());
					highlightedTrips.addAll(s.getTrips());
				}
			} else {
				getTextOutputArea().setText("No station found starting with " + query);
			}
		}
		redraw();
	}

	/**
	 * Display highlighted stops' information and highlight trips through said stop(s).
	 */
	private void stopInfo() {
		for (Stop s : highlightedStops) {
			getTextOutputArea().append("\nSelected: " + s.getName() +
					"\nTrips through this stop:" + s.tripsToString() + "\n");
			highlightedTrips.addAll(s.getTrips());
		}
	}

	/**
	 * Respond to buttons clicked on top pane.
	 * Zoom buttons call the zoom function with the appropriate argument.
	 * Move shifts the view by shifting the origin by a distance that is inversely
	 * 		related to the zoom factor, meaning that we move 20 pixels regardless of zoom.
	 * @param m contains what button was clicked
	 */
	@Override
	protected void onMove(Move m) {
		if (origin == null) return; // not ready to process move

		if (m.equals(Move.ZOOM_IN)) {
			zoom(true);
		} else if (m.equals(Move.ZOOM_OUT)) {
			zoom(false);
		}
		double panFactor = 20 / scale;
		if (m.equals(Move.NORTH)) origin = origin.moveBy(0, panFactor);
		else if (m.equals(Move.SOUTH)) origin = origin.moveBy(0, -panFactor);
		else if (m.equals(Move.WEST)) origin = origin.moveBy(-panFactor, 0);
		else if (m.equals(Move.EAST)) origin = origin.moveBy(panFactor, 0);
	}

	/**
	 * Modify drawing parameters to zoom in or out with respect to the centre of canvas.
	 * If zooming out, zoom factors are its reciprocal.
	 * Shift the origin to ensure it zooms to the centre of the view, then change the scale
	 * that redraw() uses, and the size of the Stop circles slightly less than the scale
	 * changes as they get intrusively big. Width and height changes opposite to the change
	 * in scale as the view gets smaller zooming in, larger zooming out.
	 * @param in true zooms in, false zooms out
	 */
	private void zoom(boolean in) {
		// constant coefficient used for zooming
		double zoomFactor = 1.5;
		// don't let stop circles get as large, they get obnoxiously big
		double stopZoomFactor = 0.85;

		if (!in) {
			zoomFactor = 1 / zoomFactor;
			stopZoomFactor = 1 / stopZoomFactor;
		}

		origin = origin.moveBy((width - width/ zoomFactor)/2,
				-(height - height/ zoomFactor)/2);
		scale *= zoomFactor;
		Stop.changeStopSize(zoomFactor * stopZoomFactor);
		width /= zoomFactor;
		height /= zoomFactor;
	}

	/**
	 * Zooms in the map in a constant factor, zooming in when turned up, out when turned down.
	 * Considered but does not zoom based how many notches turned, as zoom would be way too
	 * fast considering this method is called many times per second.
	 * @param e scroll event contains which direction turned
	 */
	protected void onScroll(MouseWheelEvent e) {
		if (origin == null) return; // not ready to process scroll

		int n = e.getWheelRotation();

		if (n > 0) {
			zoom(false);
		} else if (n < 0) {
			zoom(true);
		}
	}

	/**
	 * Reads in stops and trips files to organise them into:
	 *  - HashSet of Stops found by its ID
	 *  - Trie of Stops found by its name
	 *  - HashSet of Trips found by its ID
	 * It also finds the geographical bounds of the map by finding
	 * the top-left-most corner as the `origin` field, and the
	 * bottom-right-most corner to determine the map's dimensions.
	 * To assure that the files were loaded correctly, the
	 * @param stopFile the stops.txt file
	 * @param tripFile the trips.txt file
	 */
	@Override
	protected void onLoad(File stopFile, File tripFile) {
		// clear data
		trips.clear();
		stopsByID.clear();
		stopsByName.clear();
		highlightedStops.clear();
		highlightedTrips.clear();
		Stop.resetStopSize();
		
		try (BufferedReader stopReader = new BufferedReader(new FileReader(stopFile));
			 BufferedReader tripReader = new BufferedReader(new FileReader(tripFile))) {
			stopReader.readLine(); // throw away header line
			tripReader.readLine(); // throw away header line

			String line;
			double maxLat = -180;
			double maxLon = -180;
			double minLat = 180;
			double minLon = 180;

			// parse stops
			while ((line = stopReader.readLine()) != null) {
				String[] stopInfo = line.split("\t"); // assume tab-delimited

				double lat = Double.parseDouble(stopInfo[2]);
				if (lat > maxLat) maxLat = lat;
				if (lat < minLat) minLat = lat;

				double lon = Double.parseDouble(stopInfo[3]);
				if (lon > maxLon) maxLon = lon;
				if (lon < minLon) minLon = lon;

				String stopID = stopInfo[0];
				String stopName = stopInfo[1];
				Stop s = new Stop(stopID, stopName, lat, lon);
				// ensure no duplicates in Stop Map
				assert !stopsByID.containsKey(stopID) : "Adding stop with duplicate ID: " + stopID;
				stopsByID.put(stopID, s);
				// this particular trie will be case insensitive
				stopsByName.add(stopName.toLowerCase().toCharArray(), s);
			}

			// i.e. topLeft corner of map
			// shift by 0.5 to add padding so the entire Stop circle is seen
			origin = Location.newFromLatLon(maxLat, minLon).moveBy(-0.5, 0.5);
			Location bottomRight = Location.newFromLatLon(minLat, maxLon).moveBy(0.5, -0.5);
			height = origin.y-bottomRight.y;
			// BIG ASSUMPTION HERE: map as defined by max/min lat/lon is wide, but canvas is even wider,
			// adjust width so it fits the aspect ratio of the canvas; the inverse needs to happen if the
			// canvas is taller than the map
			width = height / DEFAULT_DRAWING_HEIGHT * DEFAULT_DRAWING_WIDTH;
			scale = GUI.DEFAULT_DRAWING_HEIGHT / height;
			Location centre = new Location(origin.x + width/2,origin.y - height/2);

			qtRoot = new QuadTreeNode(origin, centre);
			for (Stop s : stopsByID.values()) {
				boolean result = qtRoot.insert(s);
				assert result : "Quad tree creation failed for "+s;
			}

			// parse trips
			while ((line = tripReader.readLine()) != null) {
				String[] tripInfo = line.split("\t"); // assume tab-delimited
				String tripID = tripInfo[0];
				List<Stop> tripStops = new ArrayList<>();

				for (int i=1; i<tripInfo.length; i++) {
					tripStops.add(stopsByID.get(tripInfo[i]));
				}

				Trip t = new Trip(tripID, tripStops);
				assert !trips.contains(t) : "Duplicate trip exists: "+tripID;
				trips.add(t);
			}

			// create connections between each stop
			for (Trip trip : trips) {
				List<Stop> stops = trip.getStops();
				for (int i=0; i<stops.size()-1; i++) {
					Stop from = stops.get(i);
					Stop to = stops.get(i+1);
					Connection c = new Connection(from, to, trip);
					from.addOutgoingConnection(c);
					to.addIncomingConnection(c);
				}
			}

			getTextOutputArea().setText("Loaded " + stopsByID.size() + " stops and " + trips.size() + " trips. " +
					"\nUse the buttons or the scroll wheel (Shift+scroll horizontal) to navigate.");
		} catch (IOException e) {
			getTextOutputArea().setText("File handling error.");
		}
	}

	public static void main(String[] args) {
		new JourneyPlanner();
	}
}
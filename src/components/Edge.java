package src.components;

/* AUTHORS
 * Muhammad Sulton Tauhid
 * Email	: msulton55@gmail.com
 * Instagram: msultont
 * Whatsapp	: +6282299024212
 * 
 */
public class Edge {
	String id;
	double weight;

	// constructor
	public Edge(String id, Node o, Node d) {
		this.id = id;
		this.weight = distance(o.lat, o.lon, d.lat, d.lon);
	}

	// find distance between two edges (in meters)
	// the following code for the Haversin Functon is taken from:
	// https://github.com/jasonwinn/haversine/blob/master/Haversine.java
	private static final int EARTH_RADIUS = 6371; // Approx Earth radius in KM

	private static double distance(double startLat, double startLong, double endLat, double endLong) {

		double dLat = Math.toRadians((endLat - startLat));
		double dLong = Math.toRadians((endLong - startLong));

		startLat = Math.toRadians(startLat);
		endLat = Math.toRadians(endLat);

		double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return EARTH_RADIUS * c; // <-- d
		// returns distance in km
	}

	private static double haversin(double val) {
		return Math.pow(Math.sin(val / 2), 2);
	}

}

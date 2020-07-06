package src.components;/* AUTHORS
 * Muhammad Sulton Tauhid
 * Email	: msulton55@gmail.com
 * Instagram: msultont
 * Whatsapp	: +6282299024212
 * 
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.StringTokenizer;

// All nodes and edges will be combined here in src.components.Graph class.
public class Graph {
	// Store list of vertices in a hash set to prevent duplicates
	public HashMap<String, Node> vertices;
	
	// These will be useful for painting the graph
	double maxlat;
	double minlat;
	double maxlon;
	double minlon;

	// construct a graph from a text file
	public Graph(String filename) {
		vertices = new HashMap<>();
		maxlat = maxlon = -1 * Double.MAX_VALUE;
		minlat = minlon = Double.MAX_VALUE;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String line;
			String type;
			StringTokenizer tokenizer;
			while ((line = reader.readLine()) != null) {
				tokenizer = new StringTokenizer(line, "\t");
				type = tokenizer.nextToken().toLowerCase();
				if (type.equals("i")) {
					this.addIntersection(tokenizer.nextToken(), Double.parseDouble(tokenizer.nextToken()),
							Double.parseDouble(tokenizer.nextToken()));
				} else if (type.equals("r")) {
					this.addEdge(tokenizer.nextToken().toLowerCase(), tokenizer.nextToken(), tokenizer.nextToken());
				}
			}
			reader.close();
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
	}

	void addIntersection(String id, double lon, double lat) {
		// create src.components.Node with ID = id; latitude and longitude
		// add src.components.Node to HashSet
		vertices.put(id, new Node(id, lon, lat));
		// keeping track of max and min lat and lon
		if (lat > maxlat) {
			maxlat = lat;
		} else if (lat < minlat) {
			minlat = lat;
		}
		if (lon > maxlon) {
			maxlon = lon;
		} else if (lon < minlon) {
			minlon = lon;
		}
	}

	// add edge between node a and node b
	void addEdge(String id, String a, String b) {
		Node anode = vertices.get(a);
		Node bnode = vertices.get(b);
		// handle cases when nodes aren't on the graph
		if (anode == null) {
			System.out.println(a + " does not exist on the graph");
			return;
		}
		if (bnode == null) {
			System.out.println(b + " does not exist on the graph");
			return;
		}
		// create edges on both nodes and add them to each edgeList
		anode.adjlist.put(bnode, new Edge(id, anode, bnode));
		bnode.adjlist.put(anode, new Edge(id, bnode, anode));
		// test distance calculating method
	}

	// shortest path
	public List<Node> shortestPath(String start, String end) {
		Node startnode = vertices.get(start);
		Node endnode = vertices.get(end);
		PriorityQueue<Node> queue = new PriorityQueue<>();
		LinkedList<Node> returnlist = new LinkedList<>();
		// iterate through vertices looking for corresponding nodes, update info, and
		// add nodes to queue
		for (String s : vertices.keySet()) { //O(|V|)
			Node n = vertices.get(s);
			if (s.equals(start)) {
				n.remember(true);
				queue.add(n); //only adds the starting node to the queue
			} else {
				n.remember(false);
			}
		}
		// handle cases when nodes aren't on the graph
		if (startnode == null) {
			System.out.println("shortestPath: '" + start + "' does not exist on the graph");
			return returnlist;
		}
		if (endnode == null) {
			System.out.println("shortestPath: '"+ end + "' does not exist on the graph");
			return returnlist;
		}
		// iterate through all nodes from closest to farthest, and update info of each
		// adjacent node if necessary
		while (!queue.isEmpty()) { //O(V)
			Node current = queue.poll(); //O(log(V))
			current.info.visited();
			for (Node adj : current.adjlist.keySet()) {//O(V)
				if(vertices.get(end).info.visited) { //breaks out of the loop when the end node has been visited
					break;
				}
				Edge e = current.adjlist.get(adj);
				if (current.info.dist + e.weight < adj.info.dist) {
					adj.info.update(current, e);
					if(!adj.info.visited) {
						if (!adj.info.willvisit) {
							adj.info.willvisit();
							queue.add(adj); // the idea is to keep the queue as small as possible
							//by only adding nodes that are adjacent to the node that is being visited
						}
						else {
							if(queue.remove(adj)) { //this is a node that haven't been visited yet but is on the queue.
								queue.add(adj); //since the distance was changed, it must be removed and added back
							}
						}
					}
				}
			}
		}
		
		if (endnode.info.dist == Double.MAX_VALUE) {
			// clause for the case that b is not connected to a
			System.out.println(start + " is not connected to " + end);
		} else {
			// if connected, iterate through prev starting from b and build the list
			Node ptr = endnode;
			while (ptr != null) {
				returnlist.addFirst(ptr);
				ptr = ptr.info.prev;
			}
		}
		for (String s : vertices.keySet()) {
			// just for formality. Clearing info
			vertices.get(s).forget();
		}
		return returnlist;
	}
   
}

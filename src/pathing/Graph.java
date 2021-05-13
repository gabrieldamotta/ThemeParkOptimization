package pathing;

import java.util.ArrayList;

public class Graph {
	ArrayList<Vertex> vertices;
	ArrayList<Edge> edges;
	
	Graph(ArrayList<Vertex> v, ArrayList<Edge> e) {
		this.vertices = v;
		this.edges = e;
	}
	
	public ArrayList<Vertex> getVertices() {
		return vertices;
	}
	
	public ArrayList<Edge> getEdges() {
		return edges;
	}
}

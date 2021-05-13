package pathing;

public class Edge {
	private Vertex v;
	private Vertex u;
	
	Edge(Vertex v, Vertex u) {
		this.v = v;
		this.u = u;
	}
	
	public Vertex getV() {
		return v;
	}
	
	public Vertex getU() {
		return u;
	}
}

package old;
import java.util.ArrayList;

public class SearchMeta {
	public String current;
	public double satisfaction;
	public double time;
	public ArrayList<String> path;
	
	SearchMeta(String curr, double satis, double t, ArrayList<String> p) {
		current = curr;
		satisfaction = satis;
		time = t;
		path = p;
	}
	
	@Override
	public String toString() {
		return "Satisfaction: " + satisfaction + ", Time: " + time + "\n"
				+ "Path: " + path;
	}
}

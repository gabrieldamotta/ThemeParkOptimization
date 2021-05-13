package old;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Pathfinding {
	
	static HashMap<String,Vertex> buildGraph(String filename) {
		HashMap<String,Vertex> graph = new HashMap<String,Vertex>();
		// read file line by line
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line;
			while ((line = br.readLine()) != null) { // process each line
				// name handling
				String[] splitName = line.split(":");
				String name = splitName[0];
				
				// neighbor handling
				String[] splitNeigh = splitName[1].split(",");
				HashMap<String,Double> neighbors = new HashMap<String,Double>();
				for (int i = 0; i < splitNeigh.length; i++) {
					// get each neighbor into hash map
					String[] neighborInfo = splitNeigh[i].split("\\s");
					neighbors.put(neighborInfo[0], Double.parseDouble(neighborInfo[1]));
				}
				
				// build and insert vertex
				Vertex v = new Vertex(name, neighbors);
				graph.put(name, v);
			}
			
			// close file
			br.close();
		
		} catch (FileNotFoundException e) {
			System.out.println("File not found!");
			System.exit(1);
		} catch (IOException e) {
			System.out.println("IOException!");
			System.exit(1);
		}
		return graph;
	}
	
	static void loadTimes(String filename, HashMap<String,Vertex> graph) {
		// read file line by line
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line = br.readLine(); // ignore first
			while ((line = br.readLine()) != null) { // process each line
				// split raws
				String[] rawTimes = line.split(",");
				Vertex v = graph.get(rawTimes[0]); // get vertex to modify
				
				// build times array
				ArrayList<Double> waitTimes = new ArrayList<Double>();
				for (int i = 1; i < rawTimes.length; i++) {
					try {
						waitTimes.add(Double.parseDouble(rawTimes[i]));
					} catch (NumberFormatException e) {
						waitTimes.add(-1.0);
					}
				}
				
				// add times
				v.addWaitTimes(waitTimes);
			}
			
			// close file
			br.close();
		
		} catch (FileNotFoundException e) {
			System.out.println("File not found!");
			System.exit(1);
		} catch (IOException e) {
			System.out.println("IOException!");
			System.exit(1);
		}
	}
	
	static void loadMeta(String filename, HashMap<String,Vertex> graph) {
		// read file line by line
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line;
			while ((line = br.readLine()) != null) { // process each line
				// split info
				String[] info = line.split(",");
				Vertex v = graph.get(info[0]); // get vertex to modify
				
				// add info
				v.addSatisfaction(Double.parseDouble(info[1]));
				v.addDuration(Integer.parseInt(info[2]));
			}
			
			// close file
			br.close();
		
		} catch (FileNotFoundException e) {
			System.out.println("File not found!");
			System.exit(1);
		} catch (IOException e) {
			System.out.println("IOException!");
			System.exit(1);
		}
	}
	
	static HashMap<String,Vertex> deepCopy(HashMap<String,Vertex> graph) {
		HashMap<String,Vertex> newGraph = new HashMap<String,Vertex>();
		for (String s : graph.keySet()) {
			newGraph.put(s, new Vertex(graph.get(s)));
		}
		return newGraph;
	}
	
	static SearchMeta search(String current, int timeLimit, double time, double satisfaction, ArrayList<String> path, int it, HashMap<String,Vertex> graph) {
		// recursive base case
		if (time > timeLimit || it > 5) {
			return new SearchMeta(current, satisfaction, time, path);
		}
		
		// initialize distances
		HashMap<String,Double> distances = new HashMap<String,Double>();
		Iterator<String> vIt = graph.keySet().iterator();
		while (vIt.hasNext()) {
			String v = vIt.next();
			distances.put(v, 999.9);
		}
		distances.replace(current, 0.0);
		
		// perform |V|-1 times
		for (int i = 0; i < graph.size()-1; i ++) {
			// for each edge
			for (Vertex u : graph.values()) {
				for (String v : u.getNeighbors()) {
//					System.out.println(distances);
//					System.out.println(u.getName() + " and " + v);
					if (distances.get(u.getName()) < distances.get(v)) {
						distances.replace(v,distances.get(u.getName()) + u.getWalkTime(v));
					}
				}
			}
		}
		
		SearchMeta futMeta = new SearchMeta ("", 0, 0, null);
		// for each found distance, perform recursive search
		for (String s : graph.keySet()) {
//			if (!graph.get(s).getName().equals("entrance")) {
//				System.out.println(s + " " + graph.get(s).getWaitTime(time));
//			}
			if (!graph.get(s).getName().equals("entrance") && graph.get(s).isOpen(time)) {
//				System.out.println(graph.keySet());
//				System.out.println(it + " " + graph.get(s));
				HashMap<String,Vertex> newGraph = deepCopy(graph);
				double newTime = time + newGraph.get(s).getWaitTime(time) + newGraph.get(s).getDuration();
				double newSatisfaction = satisfaction + newGraph.get(s).satisfy();
				ArrayList<String> newPath = new ArrayList<String>(path);
				newPath.add(s);
				// System.out.println(time + " < " + newTime);
				SearchMeta posMeta = search(newGraph.get(s).getName(), timeLimit, newTime, newSatisfaction, newPath, it+1, newGraph);
				if (posMeta.satisfaction > futMeta.satisfaction) {
					futMeta.current = posMeta.current;
					futMeta.satisfaction = posMeta.satisfaction;
					futMeta.time = posMeta.time;
					futMeta.path = posMeta.path;
				}
			}
		}
		
		return futMeta;
	}
	
	public static void main(String[] args) {
		
		// initialize graph
		HashMap<String,Vertex> graph = buildGraph("src/graph_meta.txt");
		loadTimes("src/wait_times.csv", graph);
		loadMeta("src/meta_info_family.txt", graph);
		
//		for (Vertex v : graph.values()) {
//			System.out.print(v);
//		}
		
		// set up search meta info
		int timeLimit = 895;
		int time = 0;
		double satisfaction = 0;
		String current = graph.keySet().iterator().next();
		ArrayList<String> path = new ArrayList<String>();
		path.add(current);
		
		SearchMeta finalMeta = new SearchMeta(current, satisfaction, time, path);
		while (finalMeta.time <= timeLimit) {
			finalMeta = search(finalMeta.current, timeLimit, finalMeta.time, finalMeta.satisfaction, finalMeta.path, 0, graph);
		}
		System.out.println(finalMeta);
	}
}

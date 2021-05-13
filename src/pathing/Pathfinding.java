package pathing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class Pathfinding {

	// maybe unused
	HashMap<String,Integer> strToInt;
	HashMap<Integer,String> intToString;
	
	// graph representation
	HashMap<String,Vertex> graph;
	
	// search meta info
	final int timeLimit = 895;
	int time = 0;
	
	/**
	 * Constructor to build and preprocess graph.
	 */
	Pathfinding() {
		// build graph
		buildGraph("src/graph_meta.txt");
		loadTimes("src/wait_times.csv");
		loadMeta("src/meta_info.txt");
		findShortestWalks();
		addBreaks();
		// addMealBreaks();
	}
	
	/**
	 * Auxiliary function to build graph
	 * 
	 * @param filename the graph txt file
	 */
	private void buildGraph(String filename) {
		graph = new HashMap<String,Vertex>();
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
					neighbors.put(neighborInfo[0], Math.ceil(4*Double.parseDouble(neighborInfo[1])/3));
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
	}
	
	/**
	 * Auxiliary function to load graph times
	 * 
	 * @param filename the times csv file
	 */
	private void loadTimes(String filename) {
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
				
				// set times
				v.setWaitTimes(waitTimes);
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
	
	/**
	 * Auxiliary function to load meta information
	 * 
	 * @param filename the meta txt file
	 */
	private void loadMeta(String filename) {
		// read file line by line
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line;
			while ((line = br.readLine()) != null) { // process each line
				// split info
				String[] info = line.split(",");
				Vertex v = graph.get(info[0]); // get vertex to modify
				
				// add info
				v.setSatisfaction(-Double.parseDouble(info[1]));
				v.setDuration(2+Integer.parseInt(info[2]));
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
	
	/**
	 * Performs the Bellman-Ford algorithm
	 * to find the shortest paths between
	 * every ride to every ride. Reformulates
	 * graph into fully connected graph.
	 */
	private void findShortestWalks() {
		// initialize distances
		HashMap<String,Double> distances = new HashMap<String,Double>();
		
		// bellman-ford for each vertex
		for (String current : graph.keySet()) {
			// clear distances
			distances.clear();
			for (String v : graph.keySet()) {
				distances.put(v, 999.9);
			}
			distances.replace(current, 0.0);
			
			// perform |V|-1 times
			for (int i = 0; i < graph.size()-1; i++) {
				// for each edge (consider edge directed)
				for (Vertex u : graph.values()) {
					for (String v : u.getNeighbors()) {
						if (distances.get(u.getName()) + u.getWalkTimeTo(v) < distances.get(v)) {
							distances.replace(v, distances.get(u.getName()) + u.getWalkTimeTo(v));
						}
					}
				}
			}
			
			// reformulate edges of this vertex
			graph.get(current).setWalkTimes(new HashMap<String,Double>(distances));
		}
	}
	
	/**
	 * Adds break vertices to each ride
	 * in the graph.
	 */
	private void addBreaks() {
		// for each vertex
		HashSet<String> nonBreaks = new HashSet<String>(graph.keySet());
		for (String v : nonBreaks) {
			// break vertex name
			String bv = v + "*";
			Vertex originalVertex = graph.get(v);
			
			// create break vertex and add to graph
			Vertex breakVertex = new Vertex(bv, graph.get(v));
			breakVertex.setSatisfaction(-45); // large satisfaction makes it pseudo required
			ArrayList<Double> zeroWeight = new ArrayList<Double>();
			for (int i = 0; i < 180; i++) {
				if (i > 180) {
					zeroWeight.add(-1.0);
				} else {
					zeroWeight.add(0.0);
				}
			}
			breakVertex.setWaitTimes(zeroWeight); // no wait to take break
			breakVertex.setDuration(60); // 60 minute breaks
			graph.put(bv, breakVertex);
			
			// add 0 weight edge from original to break vertex
			HashMap<String,Double> distances = originalVertex.getWalkTimes();
			distances.put(bv, 0.0);
			originalVertex.setWalkTimes(distances);
		}
	}
	
	/**
	 * Adds meal break vertices to 
	 * each ride in the graph.
	 * Unused and incorrect!
	 */
	private void addMealBreaks() {
		// for each vertex
		HashSet<String> nonBreaks = new HashSet<String>(graph.keySet());
		for (String v : nonBreaks) {
			// break vertex name
			String bv = v + "#";
			Vertex originalVertex = graph.get(v);
			
			// create break vertex and add to graph
			Vertex breakVertex = new Vertex(bv, graph.get(v));
			breakVertex.setSatisfaction(-30); // large satisfaction makes it pseudo required
			ArrayList<Double> zeroWeight = new ArrayList<Double>();
			for (int i = 0; i < 180; i++) {
				if ((36 < i && i < 71) || (120 < i && i < 143)) {
					zeroWeight.add(0.0);
				} else {
					zeroWeight.add(-1.0);
				}
			}
			breakVertex.setWaitTimes(zeroWeight); // no wait to take break
			breakVertex.setDuration(45); // thirty minute breaks
			graph.put(bv, breakVertex);
			
			// add 0 weight edge from original to break vertex
			HashMap<String,Double> distances = originalVertex.getWalkTimes();
			distances.put(bv, 0.0);
			originalVertex.setWalkTimes(distances);
		}
	}
	
	/**
	 * Find the optimal path to take.
	 */
	public void findOptimalPath(String current) {
		// initialize distances and predecessors
		HashMap<Pair<String,Integer>,Double> distances = new HashMap<Pair<String,Integer>,Double>();
		HashMap<Pair<String,Integer>,Pair<String,Integer>> predecessors = new HashMap<Pair<String,Integer>,Pair<String,Integer>>();
		HashMap<Pair<String,Integer>,Pair<String,Integer>> successors = new HashMap<Pair<String,Integer>,Pair<String,Integer>>();
		for (String v : graph.keySet()) {
			// for each time
			for (int t = 0; t <= timeLimit + 1; t++) {
				Pair<String,Integer> vertexTime = new Pair<String,Integer>(v + " " + t, t);
				distances.put(vertexTime, 1.0); // <----- IF IT DOES NOT WORK, HELP ME PLS!!!!!!!!!!!!!!!
				predecessors.put(vertexTime, null);
				successors.put(vertexTime, null);
			}
		}
		distances.replace(new Pair<String,Integer>(current + " " + time, time), 0.0);
		
		// perform |V|-1 times (graph.size()*timeLimit)-1
		for (int i = 0; i < 100; i++) {
			System.out.println("Iteration " + i + " of " + ((graph.size()*timeLimit)-1));
			// for each edge (consider edge directed)
			for (Vertex u : graph.values()) {
				for (String v : u.getNeighbors()) {
					// for each possible time stamp of u
					for (int t = 0; t <= timeLimit; t++) {
						
						// find time at end of riding v
						double walkToV = u.getWalkTimeTo(v);
						Vertex ride = graph.get(v);
						if (ride.getWaitTimeAt(t + walkToV) == -1.0) { // handle closed rides
							continue;
						}
						double timeEndOfRide = t + walkToV + ride.getWaitTimeAt(t + walkToV) + ride.getDuration();
						if (timeEndOfRide > timeLimit) {
							timeEndOfRide = timeLimit+1;
						}
						
						// create timed pair u and v
						Pair<String,Integer> timedU = new Pair<String,Integer>(u.getName() + " " + t, t);
						Pair<String,Integer> timedV = new Pair<String,Integer>(v + " " + (int) timeEndOfRide, (int) timeEndOfRide);	
						
						// decipher satisfaction from this ride
						double satisfaction = ride.getSatisfaction();
						Pair<String,Integer> pred = timedU;
						int numBreaks = 0;
						int numMealBreaks = 0;
						while (pred != null) {
							// if ride was already visited, decrease satisfaction
							String check = pred.getX().split("\\s")[0];
							if (check.contains("*")) { // handle breaks
								satisfaction += numBreaks * 15;
								numBreaks++;
							} else if (check.contains("#")) { // handle meal breaks
								satisfaction += numMealBreaks * 15;
								numMealBreaks++;
							} else if (check.equals(v)) {
								satisfaction = decreaseSatisfaction(satisfaction);
							}
							pred = predecessors.get(pred);
						}

						// potentially better - perform the bellman-ford cycle
						if (distances.get(timedU) < 1 && distances.get(timedU) + satisfaction < distances.get(timedV)) {
							
							// build usedCounts
							numBreaks = 0;
							numMealBreaks = 0;
							HashMap<String,Integer> usedCounts = new HashMap<String,Integer>();
							for (String vertex : graph.keySet()) {
								usedCounts.put(vertex, 0);
							}
							
							// check path backwards
							double finalSatisfaction = 0;
							Pair<String,Integer> curr = timedU;
							while (curr != null) {
								String check = curr.getX().split("\\s")[0];
								// add satisfaction
								double checkSatisfaction = graph.get(check).getSatisfaction();
								if (check.contains("*")) { // handle breaks
									checkSatisfaction += numBreaks * 15;
									numBreaks++;
								} else if (check.contains("#")) { // handle meal breaks
									checkSatisfaction += numMealBreaks * 15;
									numMealBreaks++;
								} else {
									for (int j = 0; j < usedCounts.get(check); j++) {
										checkSatisfaction = decreaseSatisfaction(checkSatisfaction);
									}
								}
								finalSatisfaction += checkSatisfaction;
								// System.out.println("	" + finalSatisfaction + " " + check + " " + usedCounts.get(check));
								// add to count
								usedCounts.replace(check,usedCounts.get(check) + 1);
								curr = predecessors.get(curr);
							}
							
							// check path forwards
							ArrayList<Double> satisStorage = new ArrayList<Double>();
							curr = timedV;
							while (curr != null) {
								String check = curr.getX().split("\\s")[0];
								// add satisfaction
								double checkSatisfaction = graph.get(check).getSatisfaction();
								if (check.contains("*")) { // handle breaks
									checkSatisfaction += numBreaks * 15;
									numBreaks++;
								} else if (check.contains("#")) { // handle meal breaks
									checkSatisfaction += numMealBreaks * 15;
									numMealBreaks++;
								} else {
									for (int j = 0; j < usedCounts.get(check); j++) {
										checkSatisfaction = decreaseSatisfaction(checkSatisfaction);
									}
								}
								finalSatisfaction += checkSatisfaction;
								// System.out.println("	" + finalSatisfaction + " " + check + " " + usedCounts.get(check));
								// record satisfaction for update
								satisStorage.add(finalSatisfaction);
								// add to count
								usedCounts.replace(check,usedCounts.get(check) + 1);
								curr = successors.get(curr);
							}
							
//							// find current number of breaks
//							int numBreaks = 0;
//							for (String vertex : usedCounts.keySet()) {
//								if (vertex.contains("*")) {
//									numBreaks += usedCounts.get(vertex);
//								}
//							}
							
							// find end of this path segment
							Pair<String,Integer> end = timedV;
							while (successors.get(end) != null) {
								end = successors.get(end);
							}
							
							// overall better, make changes
							if (finalSatisfaction < distances.get(end)) {
								// System.out.println("Things are changing!");
								
								curr = timedV;
								// update all successors
								int j = 0;
								while (curr != null) {
									// update this vertex
									// System.out.println(satisStorage);
									distances.replace(curr, satisStorage.get(j));
									curr = successors.get(curr);
									j++;
								}
								
								//
								// update timedV
								//
								
								// erase previous predecessor's successor
								successors.replace(predecessors.get(timedV), null);
								
								// update this vertex hierarchy
								predecessors.replace(timedV, timedU);
								successors.replace(timedU, timedV);
							}
						}
					}
				}
			}
		}
		
		// find most satisfactory path
		double bestSatisfaction = 0;
		Pair<String,Integer> bestFinalVertex = null;
		for (Pair<String,Integer> timedVertex : distances.keySet()) {
			// if better (lower), store as best
			if (distances.get(timedVertex) < bestSatisfaction) {
				bestSatisfaction = distances.get(timedVertex);
				bestFinalVertex = timedVertex;
			}
		}
		
		// generate final path
		ArrayList<Pair<String,Integer>> path = new ArrayList<Pair<String,Integer>>();
		path.add(bestFinalVertex);
		Pair<String,Integer> pred = predecessors.get(bestFinalVertex);
		while (pred != null) {
			path.add(pred);
			pred = predecessors.get(pred);
		}
		
		Collections.reverse(path);
		System.out.println("Satisfaction: " + (-bestSatisfaction));
		System.out.println("Number of rides: " + path.size());
		System.out.println("Path: " + path);
		
	}
	
	/**
	 * Function to reduce given satisfaction s:
	 * new s = 10-(3(x+1)^2/2), for x = 10 - s
	 * 
	 * @param s the satisfaction
	 * @return the new reduced satisfaction
	 */
	private double decreaseSatisfaction(double s) {
		return (s + 3);
		// s = 10 - s;
		// return 10-(Math.pow(s+1, 2)/10);
	}
	
	public static void main(String[] args) {
		Pathfinding pathfinder = new Pathfinding();
		pathfinder.findOptimalPath("entrance");
	}
}

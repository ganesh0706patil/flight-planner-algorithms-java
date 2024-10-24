// Dijkstras code
import java.io.*;
import java.util.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

class Flight {
    String source;
    String destination;
    double cost;
    double time;

    public Flight(String source, String destination, double cost, double time) {
        this.source = source;
        this.destination = destination;
        this.cost = cost;
        this.time = time;
    }
}

class FlightPlanner {
    private Map<String, List<Flight>> flightMap = new HashMap<>();
    private PrintWriter outputFile;

    public void readFlightData(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 4) {
                    String source = parts[0];
                    String destination = parts[1];
                    double cost = Double.parseDouble(parts[2]);
                    double time = Double.parseDouble(parts[3]);
                    Flight flight = new Flight(source, destination, cost, time);

                    // Add flight to the adjacency list (flightMap)
                    flightMap.computeIfAbsent(source, k -> new ArrayList<>()).add(flight);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading flight data: " + e.getMessage());
        }
    }

    public void openOutputFile(String filename) {
        try {
            outputFile = new PrintWriter(new FileWriter(filename));
        } catch (IOException e) {
            System.err.println("Error opening output file: " + e.getMessage());
        }
    }

    public void closeFiles() {
        if (outputFile != null) {
            outputFile.close();
        }
    }

    public void findBestPath(String start, String end, String criterion) {
        // Use Dijkstra's algorithm to find the best path
        Map<String, Double> distanceMap = new HashMap<>(); 
        Map<String, Flight> previousFlight = new HashMap<>();
        Set<String> visited = new HashSet<>();
        PriorityQueue<CityNode> pq = new PriorityQueue<>(Comparator.comparingDouble(c -> c.value));

        // Initialize the distance map
        distanceMap.put(start, 0.0);
        pq.offer(new CityNode(start, 0.0));

        while (!pq.isEmpty()) {
            CityNode currentNode = pq.poll();
            String currentCity = currentNode.city;

            if (visited.contains(currentCity)) continue;  // already visited
            visited.add(currentCity);

            if (currentCity.equals(end)) {
                break;
            }

            // Explore the neighboring flights
            List<Flight> neighbors = flightMap.getOrDefault(currentCity, new ArrayList<>());
            for (Flight flight : neighbors) {
                String nextCity = flight.destination;

                double newValue;
                if (criterion.equals("cost")) {
                    newValue = distanceMap.get(currentCity) + flight.cost;
                } else {
                     newValue = distanceMap.get(currentCity) + flight.time;
                }   


                if (newValue < distanceMap.getOrDefault(nextCity, Double.MAX_VALUE)) {
                    distanceMap.put(nextCity, newValue);
                    pq.offer(new CityNode(nextCity, newValue));
                    previousFlight.put(nextCity, flight); // Track the flight leading to this city
                }
            }
        }

        // Reconstruct the best path
        if (distanceMap.containsKey(end)) {
            List<Flight> bestPath = new ArrayList<>();
            String currentCity = end;

            while (previousFlight.containsKey(currentCity)) {
                Flight flight = previousFlight.get(currentCity);
                bestPath.add(0, flight); // Add flight to the beginning of the path
                currentCity = flight.source;
            }

            double totalCost = 0; // Initialize total cost
            double totalTime = 0; // Initialize total time

            outputFile.println("Best path from " + start + " to " + end + ":");
            for (Flight flight : bestPath) {
                outputFile.printf("Flight from %s to %s - Cost: %.2f, Time: %.2f%n",
                        flight.source, flight.destination, flight.cost, flight.time);
                totalCost += flight.cost;
                totalTime += flight.time;
            }

            // Output total cost and time
            outputFile.printf("Total Cost: %.2f, Total Time: %.2f%n", totalCost, totalTime);
        } else {
            outputFile.println("No path found from " + start + " to " + end);
        }
    }

    class CityNode {
        String city;
        double value;

        public CityNode(String city, double value) {
            this.city = city;
            this.value = value;
        }
    }
}

public class FlightPlannerApp {
    public static void main(String[] args) {
        if (args.length < 5) {
            System.err.println("Usage: java FlightPlannerApp <flightDataFile> <startingCity> <destinationCity> <outputFile> <criterion>");
            return;
        }

        long startTime = System.currentTimeMillis();

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryBefore = memoryBean.getHeapMemoryUsage();
        long memoryUsedBefore = heapMemoryBefore.getUsed();

        FlightPlanner plan = new FlightPlanner();
        plan.readFlightData(args[0]);
        plan.openOutputFile(args[3]);
        plan.findBestPath(args[1], args[2], args[4]);
        plan.closeFiles();

        long endTime = System.currentTimeMillis();

        MemoryUsage heapMemoryAfter = memoryBean.getHeapMemoryUsage();
        long memoryUsedAfter = heapMemoryAfter.getUsed();

        long memoryUsed = (memoryUsedAfter - memoryUsedBefore) / (1024 * 1024);
        long executionTime = endTime - startTime;

        System.out.println("Execution Time: " + executionTime + " milliseconds");
        System.out.println("Memory Used: " + memoryUsed + " MB");
    }
}

// Example input ->  FlightPlannerApp largeData.txt "Los Angeles" "D.C." output.txt cost

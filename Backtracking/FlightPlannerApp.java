
// Backtracking code
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
    private List<Flight> flights = new ArrayList<>();
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
                    flights.add(new Flight(source, destination, cost, time));
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
        List<Flight> bestPath = new ArrayList<>();
        double bestValue = Double.MAX_VALUE;

        // Using DFS
        findPath(start, end, new ArrayList<>(), 0, criterion, bestPath, bestValue);
        
        // Output the best path found
        if (!bestPath.isEmpty()) {
            double totalCost = 0; // Initialize total cost
            double totalTime = 0; // Initialize total time

            outputFile.println("Best path from " + start + " to " + end + ":");
            for (Flight flight : bestPath) {
                outputFile.printf("Flight from %s to %s - Cost: %.2f, Time: %.2f%n", flight.source, flight.destination, flight.cost, flight.time);
                // Accumulate total cost and time
                totalCost += flight.cost;
                totalTime += flight.time;
            }
            // Output total cost and time
            outputFile.printf("Total Cost: %.2f, Total Time: %.2f%n", totalCost, totalTime);
        } else {
            outputFile.println("No path found from " + start + " to " + end);
        }
    }

    // DFS code
    private void findPath(String current, String end, List<Flight> currentPath, double currentValue, String criterion, List<Flight> bestPath, double bestValue) {
        if (current.equals(end)) {
            if (currentValue < bestValue) {
                bestValue = currentValue;
                bestPath.clear();
                bestPath.addAll(currentPath);
            }
            return;
        }

        for (Flight flight : flights) {
            if (flight.source.equals(current)) {
                currentPath.add(flight);
                double newValue = criterion.equals("cost") ? currentValue + flight.cost : currentValue + flight.time;
                findPath(flight.destination, end, currentPath, newValue, criterion, bestPath, bestValue);
                currentPath.remove(currentPath.size() - 1);  // Backtrack
            }
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

        // Usage before Execution
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryBefore = memoryBean.getHeapMemoryUsage();
        long memoryUsedBefore = heapMemoryBefore.getUsed();

        FlightPlanner plan = new FlightPlanner();
        plan.readFlightData(args[0]);
        plan.openOutputFile(args[3]);
        plan.findBestPath(args[1], args[2], args[4]);
        plan.closeFiles();

        long endTime = System.currentTimeMillis();

        // Usage after execution
        MemoryUsage heapMemoryAfter = memoryBean.getHeapMemoryUsage();
        long memoryUsedAfter = heapMemoryAfter.getUsed();

        // Calculate memory and execution time
        long memoryUsed = (memoryUsedAfter - memoryUsedBefore) / (1024 * 1024); // Bytes to MB
        long executionTime = endTime - startTime;

        // Output the execution time and memory used
        System.out.println("Execution Time: " + executionTime + " milliseconds");
        System.out.println("Memory Used: " + memoryUsed + " MB");
    }
}

// Example input ->  FlightPlannerApp largeData.txt "Los Angeles" "D.C." output.txt cost

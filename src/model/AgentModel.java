package src.model;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * The {@code AgentModel} class represents the logical model of a grid-based environment.
 * It extends the {@link GridWorldModel} class from Jason and includes methods to handle
 * agents, walls, and scaling of geographic coordinates to fit the grid.
 */
public class AgentModel extends GridWorldModel {

    private static final Logger log = Logger.getLogger(AgentModel.class.getName());

    public static final int BRICKS = 4;
    public static final int AGENT = 16;
    public static final int OBSTACLE = 8;

    public final Set<Location> wallLocation = new HashSet<>();
    private final ArrayList<Location> processedLocations = new ArrayList<>();
    private final int screenWidth;
    private final int screenHeight;
    private final double[] minMax;
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private double lonScale;
    private double latScale;
    private double x0;
    private double y0;
    int agentsAdded = 0;
    //Map<Integer, Integer> AGENTS = new HashMap<>();

    // Additional variables
    List<Location> agentLocations = new ArrayList<>();
    private int totalAgents;

    int getTotalAgents() {
        return totalAgents;
    }

    public void setTotalAgents(int totalAgents) {
        this.totalAgents = totalAgents;
    }

    public AgentModel(int gridWidth, int gridHeight, double[][] coordinates,
                      Map<String, List<Double>> mapCoordinates, double[] minMax, int AGENTS) {
        super(gridWidth, gridHeight, AGENT);
        this.screenWidth = gridWidth;
        this.screenHeight = gridHeight;
        this.minMax = minMax;
        this.totalAgents = AGENTS;

        initializeScaling();
        markCoordinates(mapCoordinates);
        findEmptyCellsUntilWall();
    }

    /*void setAGENTS(){
        AGENTS.clear();
        for(int i=1; i<=10; i++){
            AGENTS.put(i, i);
        }
    }*/

    private double screenWindow = screenSize.getWidth();

    public double getScreenWindow() {
        return screenWindow;
    }

    private void initializeScaling() {
        double minLon = minMax[0], maxLon = minMax[1], minLat = minMax[2], maxLat = minMax[3];
        double deltaLon = maxLon - minLon;
        double deltaLat = maxLat - minLat;

        if (deltaLon != 0.0 && deltaLat != 0.0) {
            lonScale = (screenWidth - 1) / deltaLon;
            latScale = (screenHeight - 1) / deltaLat;
            x0 = -minLon * lonScale;
            y0 = maxLat * latScale;

            if (lonScale > latScale) {
                lonScale = latScale;
                x0 = ((screenWidth - 1) - (minLon + maxLon) * lonScale) / 2.0;
            } else {
                latScale = lonScale;
                y0 = ((screenHeight - 1) + (minLat + maxLat) * latScale) / 2.0;
            }
        } else {
            lonScale = deltaLon != 0.0 ? (screenWidth - 1) / deltaLon : 0;
            latScale = deltaLat != 0.0 ? (screenHeight - 1) / deltaLat : 0;
            x0 = (screenWidth - 1) / 2.0;
            y0 = (screenHeight - 1) / 2.0;
        }
    }

    public void markCoordinates(Map<String, List<Double>> mapCoordinates) {
        List<Location[]> coordinatePairs = new ArrayList<>();

        mapCoordinates.forEach((key, value) -> {
            int rows = value.size() / 3;
            double[][] arrayCoordinates = new double[rows][2];
            for (int i = 0; i < rows; i++) {
                arrayCoordinates[i][0] = value.get(i * 3);
                arrayCoordinates[i][1] = value.get(i * 3 + 1);
            }

            for (int j = 0; j < arrayCoordinates.length - 1; j++) {
                Location start = transformCoordinates(arrayCoordinates[j][0], arrayCoordinates[j][1]);
                Location end = transformCoordinates(arrayCoordinates[j + 1][0], arrayCoordinates[j + 1][1]);
                coordinatePairs.add(new Location[]{start, end});
            }
        });
        double maxY = minMax[3];
        double maxX = minMax[1];
        Location start = transformCoordinates(0, maxY);
        Location end = transformCoordinates(maxX, maxY);
        coordinatePairs.add(new Location[]{start, end});
        addWalls(coordinatePairs);
    }

    private void addWalls(List<Location[]> coordinates) {
        coordinates.forEach(pair -> {
            if (pair.length < 2) return;
            Location start = pair[0], end = pair[1];
            addWallInBetween(start, end);
        });
    }

    public Location transformCoordinates(double x, double y) {
        int gridX = (int) (x0 + x * lonScale);
        int gridY = (int) (y0 - y * latScale);
        processedLocations.add(new Location(gridX, gridY));
        return new Location(gridX, gridY);
    }

    public void addWallInBetween(Location start, Location end) {
        int dx = Math.abs(end.x - start.x), dy = Math.abs(end.y - start.y);
        int sx = Integer.compare(end.x, start.x), sy = Integer.compare(end.y, start.y);
        int err = dx - dy;

        int x = start.x, y = start.y;

        while (true) {
            Location loc = new Location(x, y);
            if (wallLocation.add(loc)) {
                add(BRICKS, loc);
            }
            if (x == end.x && y == end.y) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x += sx; }
            if (e2 < dx) { err += dx; y += sy; }
        }
    }

    private void findEmptyCellsUntilWall() {
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                if (hasObject(BRICKS, x, y)) break;
                paintEmptyCells(x, y);
            }
        }
    }

    private void paintEmptyCells(int startX, int startY) {
        markEmptyInDirection(startX, startY, 1, 0);
        markEmptyInDirection(startX, startY, -1, 0);
        markEmptyInDirection(startX, startY, 0, 1);
        markEmptyInDirection(startX, startY, 0, -1);
    }

    private void markEmptyInDirection(int startX, int startY, int dx, int dy) {
        int x = startX, y = startY;
        while (x >= 0 && x < getWidth() && y >= 0 && y < getHeight() && !hasObject(BRICKS, x, y)) {
            add(OBSTACLE, x, y);
            x += dx;
            y += dy;
        }
    }

    public void initiateAgents(Location loc) {
        if (!agentLocations.contains(loc)) {
            add(AGENT, loc);
            setAgPos(agentsAdded, loc);
            agentsAdded++;
            setTotalAgents(agentsAdded);
            agentLocations.add(loc);
            log.info("Agent added at: " + loc);
        }
    }

    public Location getAgentLocation(int agentID) {
        if (agentID >= 0 && agentID < agentLocations.size()) {
            log.info("Agent location: " + getAgPos(agentID));
            return agentLocations.get(agentID);
        }
        return null; // Return null if the agentID is invalid
    }

    public boolean isNeighbor(Location loc1, Location loc2) {
        return Math.abs(loc1.x - loc2.x) <= 1 && Math.abs(loc1.y - loc2.y) <= 1;
    }

    // Calculates the next step towards the target location, moving only one cell at a time
    public Location getNextStepTowards(Location from, Location to) {
        int newX = from.x;
        int newY = from.y;

        if (from.x < to.x) newX++;
        else if (from.x > to.x) newX--;

        if (from.y < to.y) newY++;
        else if (from.y > to.y) newY--;

        Location nextLocation = new Location(newX, newY);

        // Ensure the next step is valid and not occupied by an obstacle or wall
        if (isLocationFree(nextLocation)) {
            return nextLocation;
        }
        // If the direct path is blocked, fallback to an adjacent open cell
        List<Location> neighbors = getNeighbors(from);
        for (Location neighbor : neighbors) {
            if (isLocationFree(neighbor) && isCloser(neighbor, to, from)) {
                return neighbor;
            }
        }
        // Return the current location if no valid move is found
        return from;
    }

    // Checks if a location is free of obstacles and walls
    private boolean isLocationFree(Location loc) {
        return !hasObject(BRICKS, loc) && !hasObject(OBSTACLE, loc);
    }

    // Returns neighboring locations around a given cell
    public List<Location> getNeighbors(Location loc) {
        List<Location> neighbors = new ArrayList<>();
        int x = loc.x;
        int y = loc.y;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 || dy != 0) {
                    Location neighbor = new Location(x + dx, y + dy);
                    if (isLocationFree(neighbor)) {
                        neighbors.add(neighbor);
                    }
                }
            }
        }
        return neighbors;
    }

    // Helper to check if a move brings the agent closer to the target
    private boolean isCloser(Location candidate, Location target, Location current) {
        return distance(candidate, target) < distance(current, target);
    }

    // Calculates the distance between two locations
    private double distance(Location loc1, Location loc2) {
        int dx = loc1.x - loc2.x;
        int dy = loc1.y - loc2.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Method to move agent to a new location
    public void moveAgent(int agentID, Location newLocation) {
        if (agentID >= 0 && agentID < agentLocations.size()) {
            Location currentLocation = agentLocations.get(agentID);
            remove(AGENT, currentLocation);  // Remove agent from the current location
            add(AGENT, newLocation);         // Place agent at the new location
            agentLocations.set(agentID, newLocation); // Update the agent's location in the list
        }
    }

}

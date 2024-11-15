/*
package src.model;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

*
 * The {@code AgentModel} class represents the logical model of a grid-based environment.
 * It extends the {@link GridWorldModel} class from Jason and includes methods to handle
 * agents, walls, and scaling of geographic coordinates to fit the grid.


public class temp extends GridWorldModel {

    private static final Logger log = Logger.getLogger(AgentModel.class.getName());

    public static final int WALL = 4;
    public static final int AGENTS = 32;
    public static final int OBSTACLE = 2;

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

    public AgentModel(int gridWidth, int gridHeight, double[][] coordinates,
                      Map<String, List<Double>> mapCoordinates, double[] minMax) {
        super(gridWidth, gridHeight, AGENTS);
        this.screenWidth = gridWidth;
        this.screenHeight = gridHeight;
        this.minMax = minMax;

        initializeScaling();
        markCoordinates(mapCoordinates);
        findEmptyCellsUntilWall();
        initiateAgents();
    }

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
                add(WALL, loc);
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
                if (hasObject(WALL, x, y)) break;
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
        while (x >= 0 && x < getWidth() && y >= 0 && y < getHeight() && !hasObject(WALL, x, y)) {
            add(OBSTACLE, x, y);
            x += dx;
            y += dy;
        }
    }

    private void initiateAgents() {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        Location center = new Location(centerX, centerY);

        // Step 1: Find all empty cells
        List<Location> emptyCells = new ArrayList<>();
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                Location loc = new Location(x, y);
                if (isLocationFree(loc)) {
                    emptyCells.add(loc);
                }
            }
        }

        // Step 2: Sort empty cells by distance from the center
        emptyCells.sort(Comparator.comparingDouble(loc -> distance(loc, center)));

        // Step 3: Place agents, ensuring each location is unique
        int agentsAdded = 0;
        int maxAgents = 10; // Set the number of agents you want

        Set<Location> agentLocations = new HashSet<>(); // Track locations where agents are added

        for (Location loc : emptyCells) {
            if (agentsAdded >= maxAgents) {
                break;
            }
            if (!agentLocations.contains(loc)) { // Only add if this location is unique
                add(AGENTS, loc);                // Add agent to this location
                agentLocations.add(loc);          // Mark location as occupied
                agentsAdded++;
                log.info("Agent added at: " + loc); // Log each agent's position for debugging
            }
        }
    }

private void initiateAgents() {
        int maxAgents = 10; // Adjust the number of agents you want

        // Step 1: Find all empty cells
        List<Location> emptyCells = new ArrayList<>();
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                Location loc = new Location(x, y);
                if (isLocationFree(loc)) {
                    emptyCells.add(loc);
                }
            }
        }

        // Step 2: Shuffle the empty cells to randomize agent placement
        Collections.shuffle(emptyCells);

        // Step 3: Place agents in randomly shuffled locations
        int agentsAdded = 0;
        for (Location loc : emptyCells) {
            if (agentsAdded >= maxAgents) {
                break;
            }
            add(AGENTS, loc); // Add agent to this location
            agentsAdded++;
            log.info("Agent added at: " + loc); // Log each agent's position for verification
        }
    }




    // Helper method to calculate the Euclidean distance between two locations
    private double distance(Location loc1, Location loc2) {
        int dx = loc1.x - loc2.x;
        int dy = loc1.y - loc2.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Helper method to check if a given location is free
    private boolean isLocationFree(Location loc) {
        return !hasObject(WALL, loc) && !hasObject(OBSTACLE, loc);
    }

}
*/

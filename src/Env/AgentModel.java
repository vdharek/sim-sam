package src.Env;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * The {@code AgentModel} class represents the logical model of a grid-based environment.
 * It extends the {@link GridWorldModel} class from Jason and includes methods to handle
 * agents, walls, and scaling of geographic coordinates to fit the grid.
 *
 * The environment is structured as a 2D grid where walls and other objects
 * can be placed, and the grid can scale real-world geographical data onto the grid.
 */
public class AgentModel extends GridWorldModel {

    // Logger instance for logging operations within the agent model.
    Logger log = Logger.getLogger(AgentModel.class.getName());

    // Constant representing walls in the grid.
    public static final int WALL = 16;

    // Constant representing agents in the grid.
    public static final int AGENTS = 2;

    // A set to store the locations of walls in the grid.
    private final Set<Location> wallLocation;

    // The width and height of the grid (screen dimensions).
    private final int screenWidth;
    private final int screenHeight;

    // Coordinates representing geographical locations.
    private final double[][] coordinates;

    // The minimum and maximum latitude/longitude values used for scaling.
    private final double[] minMax;

    // Scaling factors and offsets for transforming real-world coordinates into grid coordinates.
    private double lonScale;
    private double latScale;
    private double x0;
    private double y0;

    /**
     * Constructs the agent model with the specified grid dimensions and geographic data.
     *
     * @param gridWidth  the width of the grid (number of columns).
     * @param gridHeight the height of the grid (number of rows).
     * @param coordinates a 2D array of real-world geographic coordinates (longitude, latitude).
     * @param minMax an array containing minimum and maximum longitude and latitude values.
     */
    protected AgentModel(int gridWidth, int gridHeight, double[][] coordinates, double[] minMax) {
        super(gridWidth, gridHeight, WALL);  // Initialize the GridWorldModel with the grid size and WALL object.
        this.coordinates = coordinates;
        this.screenWidth = gridWidth;
        this.screenHeight = gridHeight;
        this.minMax = minMax;
        this.wallLocation = new HashSet<Location>();  // Initialize the set for storing wall locations.

        initializeScaling();  // Scale the real-world coordinates to fit the grid.
        addWall();            // Place walls in the grid based on the scaled coordinates.
        findEmptyCellsUntilWall();  // Mark empty cells in the grid.
    }

    /**
     * Initializes scaling factors and offsets to map real-world geographic coordinates
     * to the grid coordinates.
     */
    private void initializeScaling() {
        double minLon = minMax[0];
        double maxLon = minMax[1];
        double minLat = minMax[2];
        double maxLat = minMax[3];

        /*System.out.println("minLon: " + minLon);
        System.out.println("maxLon: " + maxLon);
        System.out.println("minLat: " + minLat);
        System.out.println("maxLat: " + maxLat);*/

        double deltaLon = maxLon - minLon;
        double deltaLat = maxLat - minLat;

        System.out.println("deltaLon: " + deltaLon);
        System.out.println("deltaLat: " + deltaLat);

        // Calculate scaling factors for longitude and latitude.
        if (deltaLon != 0.0 && deltaLat != 0.0) {
            lonScale = (screenWidth - 1) / deltaLon;
            //System.out.println("lonScale: " + lonScale);
            latScale = (screenHeight - 1) / deltaLat;
            //System.out.println("latScale: " + latScale);
            x0 = -minLon * lonScale;
            y0 = maxLat * latScale;
            //System.out.println("x0: " + x0);
            //System.out.println("y0: " + y0);

            // Adjust scaling factors to maintain aspect ratio.
            if (lonScale > latScale) {
                lonScale = latScale;
                x0 = ((screenWidth - 1) - (minLon + maxLon) * lonScale) / 2.0;
            } else {
                latScale = lonScale;
                y0 = ((screenHeight - 1) + (minLat + maxLat) * latScale) / 2.0;
            }
        } else if (deltaLon != 0.0) {
            lonScale = (screenWidth - 1) / deltaLon;
            latScale = 0;
            x0 = -minLon * lonScale;
            y0 = (screenHeight - 1) / 2.0;
        } else if (deltaLat != 0.0) {
            lonScale = 0;
            latScale = (screenHeight - 1) / deltaLat;
            x0 = (screenWidth - 1) / 2.0;
            y0 = maxLat * latScale;
        } else {
            lonScale = 0;
            latScale = 0;
            x0 = (screenWidth - 1) / 2.0;
            y0 = (screenHeight - 1) / 2.0;
        }
    }

    /**
     * Adds walls to the grid based on real-world geographic coordinates.
     * The method connects pairs of geographic coordinates and places walls in the grid between them.
     */
    private void addWall() {
        double minX = minMax[0];
        double maxX = minMax[1];
        double minY = minMax[2];
        double maxY = minMax[3];

        // Loop through pairs of geographic coordinates to create walls in between.
        for (int i = 0; i < coordinates.length - 1; i++) {
            Location start = transformCoordinates(coordinates[i][0], coordinates[i][1]);
            Location end = transformCoordinates(coordinates[i + 1][0], coordinates[i + 1][1]);
            addWallInBetween(start, end);  // Add walls between the two locations.
        }
    }

    /**
     * Transforms real-world geographic coordinates to grid coordinates.
     *
     * @param x longitude value.
     * @param y latitude value.
     * @return the corresponding grid {@link Location}.
     */
    private Location transformCoordinates(double x, double y) {
        int gridX = (int) (x0 + x * lonScale);
        int gridY = (int) (y0 - y * latScale);
        return new Location(gridX, gridY);
    }

    /**
     * Adds walls between two {@link Location}s in the grid.
     * Uses Bresenham's line algorithm to draw walls between two grid points.
     *
     * @param start the starting location of the wall.
     * @param end the ending location of the wall.
     */
    private void addWallInBetween(Location start, Location end) {
        int dx = Math.abs(end.x - start.x);
        int dy = Math.abs(end.y - start.y);
        int sx = start.x < end.x ? 1 : -1;
        int sy = start.y < end.y ? 1 : -1;
        int err = dx - dy;

        int x = start.x;
        int y = start.y;

        while (true) {
            Location loc = new Location(x, y);
            if (!wallLocation.contains(loc)) {
                wallLocation.add(loc);
                if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()) {
                    add(WALL, loc);  // Add the wall to the grid.
                }
            }
            if (x == end.x && y == end.y) {
                break;  // Stop when the wall reaches the end point.
            }
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }
    }

    /**
     * Finds and marks empty cells in the grid until a wall is encountered.
     * Loops through the grid and invokes {@link #paintEmptyCells(int, int)} to mark empty cells.
     */
    public void findEmptyCellsUntilWall() {
        // Traverse the grid and mark empty cells until a WALL is found.
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                if (hasObject(WALL, x, y)) {  // Stop when a wall is found.
                    break;
                } else {
                    paintEmptyCells(x, y);  // Mark empty cells.
                }
            }
        }
    }

    /**
     * Marks empty cells from the given starting location in all four directions (right, left, up, down)
     * until a wall is encountered.
     *
     * @param startX the starting x-coordinate in the grid.
     * @param startY the starting y-coordinate in the grid.
     */
    private void paintEmptyCells(int startX, int startY) {
        // Mark empty cells in the right direction.
        for (int x = startX; x < getWidth(); x++) {
            if (hasObject(WALL, x, startY)) {
                break;  // Stop when a wall is found.
            } else {
                add(OBSTACLE, x, startY);  // Mark empty cell as an obstacle.
            }
        }

        // Mark empty cells in the left direction.
        for (int x = startX; x >= 0; x--) {
            if (hasObject(WALL, x, startY)) {
                break;
            } else {
                add(OBSTACLE, x, startY);
            }
        }

        // Mark empty cells in the up direction.
        for (int y = startY; y >= 0; y--) {
            if (hasObject(WALL, startX, y)) {
                break;
            } else {
                add(OBSTACLE, startX, y);
            }
        }

        // Mark empty cells in the down direction.
        for (int y = startY; y < getHeight(); y++) {
            if (hasObject(WALL, startX, y)) {
                break;
            } else {
                add(OBSTACLE, startX, y);
            }
        }
    }
}

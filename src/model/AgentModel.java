package src.model;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

import java.util.*;
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

    Logger log = Logger.getLogger(AgentModel.class.getName());

    // Constant representing walls in the grid.
    public static final int WALL = 16;

    // Constant representing agents in the grid.
    public static final int AGENTS = 2;

    // A set to store the locations of walls in the grid.
    private final Set<Location> wallLocation;

    // Set to track processed locations to avoid duplicate walls.
    private final Set<Location> processedLocations = new HashSet<>();
    private final Map<String, List<Double>> mapCoordinates;

    // The width and height of the grid (screen dimensions).
    private final int screenWidth;
    private final int screenHeight;

    // Coordinates representing geographical locations.
    private final double[][] coordinates;
    private final List<List<Double>> listCoordinates;

    // The minimum and maximum latitude/longitude values used for scaling.
    private final double[] minMax;

    // Scaling factors and offsets for transforming real-world coordinates into grid coordinates.
    private double lonScale;
    private double latScale;
    private double x0;
    private double y0;

    protected AgentModel(int gridWidth, int gridHeight, double[][] coordinates, List<List<Double>> listCoordinates, Map<String, List<Double>> mapCoordinates, double[] minMax) {
        super(gridWidth, gridHeight, WALL);  // Initialize the GridWorldModel with the grid size and WALL object.
        this.listCoordinates = listCoordinates;
        this.mapCoordinates = mapCoordinates;
        this.coordinates = coordinates;
        this.screenWidth = gridWidth;
        this.screenHeight = gridHeight;
        this.minMax = minMax;
        this.wallLocation = new HashSet<>();

        initializeScaling();  // Scale the real-world coordinates to fit the grid.
        //convertToArray(listCoordinates);
        addWall(mapCoordinates);
        findEmptyCellsUntilWall();
    }

    private void convertToArray(List<List<Double>> listCoordinates) {
        /*for (List<Double> coordinates : listCoordinates) {
            int row = coordinates.size() / 3;
            double[][] arrayCoordinates = new double[row][2];
            for (int j = 0; j < row; j++) {
                arrayCoordinates[j][0] = coordinates.get(j * 3);
                arrayCoordinates[j][1] = coordinates.get(j * 3 + 1);
            }
            addWall(arrayCoordinates);
        }*/

        for(int i=0; i<=listCoordinates.size(); i++) {
            int arraySize = listCoordinates.size();
            log.info(arraySize + " array size");
            int row = listCoordinates.get(i).size() /3;
            double[][] arrayCoordinates = new double[row][2];
            for(int j=0; j<row; j++) {
                arrayCoordinates[j][0] = listCoordinates.get(i).get(j * 3);
                arrayCoordinates[j][1] = listCoordinates.get(i).get(j * 3 + 1);
            }
            //addWall(arrayCoordinates);
        }
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
    private void addWall(Map<String, List<Double>> mapCoordinates) {
        List<Location[]> coordinatePairs = new ArrayList<>();
        /*for (int i = 0; i < coordinates.length - 1; i++) {*/
        /*for (int i = 0; i <coordinates.length; i++) {
            Location start = transformCoordinates(coordinates[i][0], coordinates[i][1]);
            //Location end = transformCoordinates(coordinates[i + 1][0], coordinates[i + 1][1]);
            Location end = null;
            if(i == coordinates.length-1){
                end = transformCoordinates(coordinates[i][0], coordinates[i][1]);
            }else{
                end = transformCoordinates(coordinates[i + 1][0], coordinates[i + 1][1]);
            }
            coordinatePairs.add(new Location[]{start, end});
        }*/


        for(Map.Entry<String, List<Double>> entry : mapCoordinates.entrySet()){
            int rows = entry.getValue().size() / 3;
            double[][] arrayCoordinates = new double[rows][2];
            for (int i = 0; i <entry.getValue().size(); i++) {
                for (int j = 0; j < rows; j++) {
                    arrayCoordinates[j][0] = entry.getValue().get(j * 3);       // First value (longitude)
                    arrayCoordinates[j][1] = entry.getValue().get(j * 3 + 1);   // Second value (latitude)
                }
            }
            for(int j=0; j<arrayCoordinates.length; j++) {
                Location start = transformCoordinates(arrayCoordinates[j][0], arrayCoordinates[j][1]);
                //Location end = transformCoordinates(coordinates[j + 1][0], coordinates[j + 1][1]);
                Location end = null;
                if(j == arrayCoordinates.length-1){
                    end = transformCoordinates(arrayCoordinates[j][0], arrayCoordinates[j][1]);
                }else{
                    end = transformCoordinates(arrayCoordinates[j + 1][0], arrayCoordinates[j + 1][1]);
                }
                coordinatePairs.add(new Location[]{start, end});
            }
        }
        addWallsBetweenCoordinates(coordinatePairs);
    }

    /**
     * Adds walls between pairs of coordinates while skipping duplicates.
     *
     * @param coordinates List of coordinate pairs to add walls between.
     */
    private void addWallsBetweenCoordinates(List<Location[]> coordinates) {
        for (Location[] pair : coordinates) {
            if (pair.length < 2) continue;  // Skip if there are not enough points in the pair

            Location start = pair[0];
            Location end = pair[1];

            // Skip this line if either start or end location has already been processed
            /*if (processedLocations.contains(start) && processedLocations.contains(end)) {
                continue;
            }*/

            addWallInBetween(start, end);

            // Mark both start and end as processed
            processedLocations.add(start);
            processedLocations.add(end);
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
     * Adds walls between two {@link Location}s in the grid using Bresenham's line algorithm.
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
                break;
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



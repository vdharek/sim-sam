package src.Env;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class AgentModel extends GridWorldModel {

    Logger log = Logger.getLogger(AgentModel.class.getName());

    public static final int WALL = 16;
    public static final int AGENTS =2;
    private final Set<Location> wallLocation;
    private final int screenWidth;
    private final int screenHeight;
    private final double[][] coordinates;
    private final double[] minMax;
    private double lonScale;
    private double latScale;
    private double x0;
    private double y0;

    protected AgentModel(int gridWidth, int gridHeight, double[][] coordinates, double[] minMax) {
        super(gridWidth, gridHeight, WALL);
        this.coordinates = coordinates;
        this.screenWidth = gridWidth;
        this.screenHeight = gridHeight;
        this.minMax = minMax;
        this.wallLocation = new HashSet<Location>();

        initializeScaling();
        addWall();
        findEmptyCellsUntilWall();
    }

    private void initializeScaling() {
        double minLon = minMax[0];
        double maxLon = minMax[1];
        double minLat = minMax[2];
        double maxLat = minMax[3];

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

    private void addWall() {
        double minX = minMax[0];
        double maxX = minMax[1];
        double minY = minMax[2];
        double maxY = minMax[3];

        for (int i = 0; i < coordinates.length - 1; i++) {
            Location start = transformCoordinates(coordinates[i][0], coordinates[i][1], minX, minY, maxX, maxY);
            Location end = transformCoordinates(coordinates[i + 1][0], coordinates[i + 1][1], minX, minY, maxX, maxY);

            addWallInBetween(start, end);
        }
    }

    private Location transformCoordinates(double x, double y, double minX, double minY, double maxX, double maxY) {
        int gridX = (int) (x0 + x * lonScale);
        int gridY = (int) (y0 - y * latScale);
        System.out.println("Real-world coordinates: (" + x + ", " + y + ")");
        System.out.println("Mapped to grid coordinates: (" + gridX + ", " + gridY + ")");
        return new Location(gridX, gridY);
    }

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
                    add(WALL, loc);
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
        // Start from each cell and log empty cells until a WALL is found
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                if (hasObject(WALL,x, y)){  // Empty cell
                    break;
                }else{
                    paintEmptyCells(x,y);
                }
            }
        }
    }

    private void paintEmptyCells(int startX, int startY) {
        // Log empty cells from (startX, startY) until a WALL is encountered
        // Right direction
        for (int x = startX; x < getWidth(); x++) {
            if (hasObject(WALL, x, startY)) {
                break;  // Stop when WALL is encountered
            }
            else{
                add(OBSTACLE, x, startY);
                //log.info("Empty cell found at: (" + x + ", " + startY + ")");
            }
        }

        // Left direction
        for (int x = startX; x >= 0; x--) {
            if (hasObject(WALL, x, startY)) {
                break;  // Stop when WALL is encountered
            } else{
                add(OBSTACLE, x, startY);
                //log.info("Empty cell found at: (" + x + ", " + startY + ")");
            }
        }

        // Up direction
        for (int y = startY; y >= 0; y--) {
            if(hasObject(WALL, startX, y)){
                break;
            }
            else{
                add(OBSTACLE, startX, y);
                //log.info("Empty cell found at: (" + startX + ", " + y + ")");
            }
        }

        // Down direction
        for (int y = startY; y < getHeight(); y++) {
            if (hasObject(WALL, startX, y)) {
                break;  // Stop when WALL is encountered
            }
            else {
                add(OBSTACLE, startX, y);
                //log.info("Empty cell found at: (" + startX + ", " + y + ")");
            }
        }
    }
}
package src.model;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

public class EnvModel extends GridWorldModel {

    Logger log = Logger.getLogger(EnvModel.class.getName());

    static int FOOTPATH = 32;
    static int DRIVING = 16;
    static int PARKING = 8;
   // static int AGENT = 64;
    static int STATIC_AGENT = 64;

    Set<Location> wallLocation = new HashSet<>();
    ArrayList<Location> processedLocation = new ArrayList<>();

    int screenWidth;
    int screenHeight;
    double[] minMax;
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    public double lonScale;
    public double latScale;
    public double x0;
    public double y0;
    int agentAdded = 5;

    List<Location> agentLocations = new ArrayList<>();
    private int totalAgents;

    public EnvModel(int gridWidth, int gridHeight,
                    Map<String, List<Double>> mapCoordinates, double[] minMax, int AGENTS) {
        super(gridWidth,
                gridHeight,
                STATIC_AGENT);
        this.screenWidth = gridWidth;
        this.screenHeight = gridHeight;
        this.minMax = minMax;
        this.totalAgents = AGENTS;
        setTotalAgents(agentAdded);

        initializeScaling();
        markCoordinates(mapCoordinates);
        findEmptyCellsUntilWall();

    }

    int getTotalAgents() {
        return totalAgents;
    }


    void setTotalAgents(int totalAgents) {
        this.totalAgents = totalAgents;
        //this.totalAgents = totalAgents;
    }

    double getScreenSize() {
        return screenSize.getWidth();
    }

    public void initializeScaling() {
        double minLon = minMax[0], maxLon = minMax[1], minLat = minMax[2], maxLat = minMax[3];

        double deltaLon = maxLon - minLon;
        double deltaLat = maxLat - minLat;

        if (deltaLon != 0.0 && deltaLat != 0.0) {
            lonScale = (screenWidth - 1) / deltaLon;
            latScale = (screenHeight - 1) / deltaLat;
            log.info(lonScale + " " + lonScale);
            log.info(latScale + " " + latScale);
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
        //log.info("x0: " + x0 + " y0: " + y0);
    }

    public void markCoordinates(Map<String, List<Double>> mapCoordinates) {
        mapCoordinates.forEach((key, value) -> {
            if (key.startsWith("ID_DRIVINGLANE_")){
                absolutePolygon(value, key);
            } else if (key.startsWith("ID_PARKING_LAY_BY_")) {
                absolutePolygon(value, key);
            }else if(key.startsWith("ID_FOOTPATH_")){
                absolutePolygon(value, key);
            }
        });
    }

    public void absolutePolygon(List<Double> value, String key) {
        int rows = value.size() / 3;
        double[][] arrayCoordinates = new double[rows][2];

        // Extract coordinate pairs
        for (int i = 0; i < rows; i++) {
            arrayCoordinates[i][0] = value.get(i * 3);
            arrayCoordinates[i][1] = value.get(i * 3 + 1);
        }

        for (int j = 0; j < arrayCoordinates.length - 1; j++) {
            Location start = transformCoordinates(arrayCoordinates[j][0], arrayCoordinates[j][1]);
            Location end = transformCoordinates(arrayCoordinates[j + 1][0], arrayCoordinates[j + 1][1]);
            addWallInBetween(start, end, key);
        }
        double maxY = minMax[3];
        double maxX = minMax[1];
        Location start = transformCoordinates(0, maxY);
        Location end = transformCoordinates(maxX, maxY);
        addWallInBetween(start, end, key);
    }

    public Location transformCoordinates(double x, double y) {
        int gridX = (int) (x0 + x * lonScale);
        int gridY = (int) (y0 - y * latScale);
        processedLocation.add(new Location(gridX, gridY));
        return new Location(gridX, gridY);
    }

    public void addWallInBetween(Location start, Location end, String key) {
        int dx = Math.abs(end.x - start.x), dy = Math.abs(end.y - start.y);
        int sx = Integer.compare(end.x, start.x), sy = Integer.compare(end.y, start.y);
        int err = dx - dy;

        int x = start.x, y = start.y;

        while (true) {
            Location loc = new Location(x, y);
            if (wallLocation.add(loc)) { // Ensure location is unique before adding
                if (key.startsWith("ID_DRIVINGLANE_")) {
                    add(DRIVING, loc);
                } else if (key.startsWith("ID_FOOTPATH_")) {
                    add(FOOTPATH, loc);
                } else if (key.startsWith("ID_PARKING_LAY_BY_")) {
                    add(PARKING, loc);
                }
            }

            // Exit the loop if end point is reached
            if (x == end.x && y == end.y) break;

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

    private void findEmptyCellsUntilWall() {
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                //if (hasObject(BRICKS, x, y)) break;
                if (hasObject(FOOTPATH, x, y)) break;
                if (hasObject(DRIVING, x, y)) break;
                if (hasObject(PARKING, x, y)) break;
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
        while (x >= 0 && x < getWidth() && y >= 0 && y < getHeight() && !hasObject(FOOTPATH, x, y) && !hasObject(DRIVING, x, y) && !hasObject(PARKING, x, y)) {
            add(OBSTACLE, x, y);
            x += dx;
            y += dy;
        }
    }
}

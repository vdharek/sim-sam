package src.Env;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

import java.util.HashSet;
import java.util.Set;

public class GeographicalGridWorldModel extends GridWorldModel {

    public static final int WALL = 16;
    private Set<Location> wallLocations;
    private int screenWidth, screenHeight;
    private double[][] coordinates;
    private double R = 6378137;
    private int CELL_SIZE;

    public GeographicalGridWorldModel(int gridWidth, int gridHeight, double[][] coordinates, int cellSize) {
        super(gridWidth, gridHeight, 1); // 1 is the number of agents
        this.screenWidth = gridWidth;
        this.screenHeight = gridHeight;
        this.coordinates = coordinates;
        this.CELL_SIZE = cellSize;
        this.wallLocations = new HashSet<>();
        calculateGridSize();
        addWalls();
    }

    private double toRadians(double degree) {
        return degree * Math.PI / 180.0;
    }

    private double[] latLonToMercator(double lat, double lon) {
        double x = R * toRadians(lon);
        double y = R * Math.log(Math.tan(Math.PI / 4 + toRadians(lat) / 2));
        //System.out.println("X = " + x + ", Y = " + y);
        return new double[]{x, y};
    }

    private void addWalls() {
        double[][] mercatorCoordinates = new double[coordinates.length][2];
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        // Convert lat/lon to Mercator and find the bounding box
        for (int i = 0; i < coordinates.length; i++) {
            double[] mercator = latLonToMercator(coordinates[i][0], coordinates[i][1]);
            mercatorCoordinates[i] = mercator;
            if (mercator[0] < minX) minX = mercator[0];
            if (mercator[0] > maxX) maxX = mercator[0];
            if (mercator[1] < minY) minY = mercator[1];
            if (mercator[1] > maxY) maxY = mercator[1];
        }

        // Loop through pairs of coordinates
        for (int i = 0; i < mercatorCoordinates.length - 1; i++) {
            Location start = mercatorToGrid(mercatorCoordinates[i][0], mercatorCoordinates[i][1], minX, minY, maxX, maxY);
            Location end = mercatorToGrid(mercatorCoordinates[i + 1][0], mercatorCoordinates[i + 1][1], minX, minY, maxX, maxY);

            addWallBetween(start, end);
        }
    }
    private void addWallBetween(Location start, Location end) {
        int dx = Math.abs(end.x - start.x);
        int dy = Math.abs(end.y - start.y);
        int sx = start.x < end.x ? 1 : -1;
        int sy = start.y < end.y ? 1 : -1;
        int err = dx - dy;

        int x = start.x;
        int y = start.y;

        while (true) {
            Location loc = new Location(x, y);
            if (!wallLocations.contains(loc)) {
                wallLocations.add(loc);
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

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6378137; // Earth's radius in meters
        double dLat = toRadians(lat2 - lat1);
        double dLon = toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(toRadians(lat1)) * Math.cos(toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in meters
    }
    private void calculateGridSize() {
        double minLat = Double.POSITIVE_INFINITY;
        double maxLat = Double.NEGATIVE_INFINITY;
        double minLon = Double.POSITIVE_INFINITY;
        double maxLon = Double.NEGATIVE_INFINITY;

        // Find bounding box
        for (double[] coord : coordinates) {
            if (coord[0] < minLat) minLat = coord[0];
            if (coord[0] > maxLat) maxLat = coord[0];
            if (coord[1] < minLon) minLon = coord[1];
            if (coord[1] > maxLon) maxLon = coord[1];
        }

        // Calculate distances
        double latDistance = haversine(minLat, minLon, maxLat, minLon); // North-South distance
        double lonDistance = haversine(minLat, minLon, minLat, maxLon); // East-West distance

        // Cell size in meters
        double cellSizeInMeters = 2.0;

        // Calculate grid dimensions
        int gridRows = (int) Math.ceil(latDistance / cellSizeInMeters);
        int gridColumns = (int) Math.ceil(lonDistance / cellSizeInMeters);

        // Calculate cell dimensions in degrees
        double cellHeightInDegrees = (maxLat - minLat) / gridRows;
        double cellWidthInDegrees = (maxLon - minLon) / gridColumns;

        // Log the results
        System.out.println("Total Grid: " + gridRows + " rows x " + gridColumns + " columns");
        System.out.println("Grid Height (in meters): " + latDistance);
        System.out.println("Grid Width (in meters): " + lonDistance);
        System.out.println("Total Cells: " + (gridRows * gridColumns));
        System.out.println("Cell Height (in degrees): " + cellHeightInDegrees);
        System.out.println("Cell Width (in degrees): " + cellWidthInDegrees);
    }

    private Location mercatorToGrid(double x, double y, double minX, double minY, double maxX, double maxY) {
        int gridX = (int) ((x - minX) / (maxX - minX) * screenWidth);
        int gridY = (int) ((maxY - y) / (maxY - minY) * screenHeight);
        return new Location(gridX, gridY);
    }

}

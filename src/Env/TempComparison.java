package src.Env;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

public class TempComparison extends GridWorldModel {

    public static final int WALL = 16;

    private int screenWidth, screenHeight;
    private double[][] coordinates;
    private double R = 6378137;
    private int CELL_SIZE;

    protected TempComparison(int gridWidth, int gridHeight, double[][] coordinates, int cellSize) {
        super(gridWidth, gridHeight, 1); // 1 is the number of agents
        this.screenWidth = gridWidth;
        this.screenHeight = gridHeight;
        this.coordinates = coordinates;
        this.CELL_SIZE = cellSize;
        addWalls();
    }

    private double toRadians(double degree) {
        return degree * Math.PI / 180.0;
    }

    private double[] latLonToMercator(double lat, double lon) {
        double x = R * toRadians(lon);
        double y = R * Math.log(Math.tan(Math.PI / 4 + toRadians(lat) / 2));
        return new double[]{x, y};
    }

    private Location mercatorToGrid(double x, double y, double minX, double minY, double maxX, double maxY){
        int gridX = (int) ((x - minX) / (maxX - minX) * (screenWidth / CELL_SIZE));
        int gridY = (int) ((maxY - y) / (maxY - minY) * (screenHeight / CELL_SIZE));
        return new Location(gridX, gridY);
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

        // Add walls to the grid
        for (int i = 0; i < mercatorCoordinates.length; i++) {
            Location loc = mercatorToGrid(mercatorCoordinates[i][0], mercatorCoordinates[i][1], minX, minY, maxX, maxY);
            if (loc.x >= 0 && loc.x < getWidth() && loc.y >= 0 && loc.y < getHeight()) {
                add(WALL, loc);
            }
        }
    }
}


package src.model;

import java.util.logging.Logger;

public class EnvValidator {

    private static final java.util.logging.Logger log = Logger.getLogger(EnvValidator.class.getName());

    public EnvValidator(double cellSize, int gridRows, int gridCols, double[][] gridCoordinates) {
        double gridArea = calculateGridArea(cellSize, gridRows, gridCols);
        double[][] closedCoordinates = closePolygon(gridCoordinates);
        log.info("Gird area: " + gridArea);
        double polygonArea = calculatePolygonArea(closedCoordinates);
        log.info("Real world polygon area: " + polygonArea);
        calculateDiscrepancy(polygonArea, gridArea);
    }

    private double calculateGridArea(double cellSize, int gridRows, int gridCols) {
        double cellArea = cellSize * cellSize; // Area of each cell in square meters
        log.info(gridRows + " x " + gridCols + " area is " + cellArea);
        return cellArea * (gridRows * gridCols); // Total grid area
    }

    // Method to calculate the area of a polygon using the Shoelace formula
    private double calculatePolygonArea(double[][] coordinates) {
        int n = coordinates.length;
        double area = 0;

        // Shoelace formula for polygon area calculation
        for (int i = 0; i < n; i++) {
            double x1 = coordinates[i][0];
            double y1 = coordinates[i][1];
            double x2 = coordinates[(i + 1) % n][0];
            double y2 = coordinates[(i + 1) % n][1];

            area += x1 * y2 - y1 * x2;
        }

        return Math.abs(area) / 2.0;
    }

    private double[][] closePolygon(double[][] coordinates) {
        int n = coordinates.length;

        // Check if the first and last points are the same
        if (coordinates[0][0] == coordinates[n - 1][0] && coordinates[0][1] == coordinates[n - 1][1]) {
            log.info("Polygon is already closed");
            return coordinates;
        } else {
            log.info("Polygon is not closed, so add the first point to the end");
            double[][] closedCoordinates = new double[n + 1][2];
            System.arraycopy(coordinates, 0, closedCoordinates, 0, n);
            closedCoordinates[n] = new double[] { coordinates[0][0], coordinates[0][1] };
            return closedCoordinates;
        }
    }

    private static void calculateDiscrepancy(double polygonArea, double gridArea){
        double discrepancy = polygonArea - gridArea;

        if (discrepancy == 0) {
            log.info("The grid area matches the mapped real-world area.");
        } else if (discrepancy > 0) {
            log.info("The mapped real-world area is larger by " + discrepancy + " square meters.");
        } else {
            log.info("The grid area is larger by " + Math.abs(discrepancy) + " square meters.");
        }
    }
}

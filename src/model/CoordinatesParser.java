package src.model;

import java.awt.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

import org.example.*;

/**
 * The {@code CoordinatesParser} class is responsible for parsing geographic coordinates from a GML (Geography Markup Language) file,
 * validating the file, and converting the coordinates into a grid-based representation for further use in a grid environment.
 */
public class CoordinatesParser {

    private static final Logger log = Logger.getLogger(CoordinatesParser.class.getName());
    private static Map<String, List<Double>> mapCoordinates;      // Stores named map coordinates
    private static List<Double> listCoordinates = new ArrayList<>();  // List to store all parsed coordinates
    private static double[][] arrayCoordinates;  // 2D array representation of coordinates
    private static double[][] gridCoordinates;  // Grid coordinates calculated from real-world data
    private static int gridHeight;               // Height of the grid
    private static int gridWidth;                // Width of the grid
    private double[] minMax = new double[4];
    private static double cellSize;
    private static int numRows;

    public int getNumCols() {
        return numCols;
    }

    public static void setNumCols(int numCols) {
        CoordinatesParser.numCols = numCols;
    }

    public int getNumRows() {
        return numRows;
    }

    public static void setNumRows(int numRows) {
        CoordinatesParser.numRows = numRows;
    }

    private static int numCols;

    public double getCellSize() {
        return cellSize;
    }

    public static void setCellSize(double cellSize) {
        CoordinatesParser.cellSize = cellSize;
    }

    public List<List<Double>> getLaneLists() {
        return laneLists;
    }

    public static void setLaneLists(List<List<Double>> laneLists) {
        CoordinatesParser.laneLists = laneLists;
    }

    private static List<List<Double>> laneLists;
    /**
     * Getter for the min/max values of coordinates.
     *
     * @return an array of min/max values for longitude and latitude.
     */
    public double[] getMinMax() {
        return minMax;
    }

    /**
     * Setter for the min/max values of coordinates.
     *
     * @param minMax an array containing the minimum and maximum longitude/latitude values.
     */
    public void setMinMax(double[] minMax) {
        this.minMax = minMax;
    }

    /**
     * Getter for the grid coordinates.
     *
     * @return a 2D array of grid coordinates.
     */
    public double[][] getGridCoordinates() {
        return gridCoordinates;
    }

    /**
     * Setter for the grid coordinates.
     *
     * @param gridCoordinates a 2D array of grid coordinates.
     */
    public static void setGridCoordinates(double[][] gridCoordinates) {
        CoordinatesParser.gridCoordinates = gridCoordinates;
    }

    /**
     * Getter for the grid height.
     *
     * @return the height of the grid.
     */
    public int getGridHeight() {
        return gridHeight;
    }

    /**
     * Setter for the grid height.
     *
     * @param gridHeight the height of the grid.
     */
    public static void setGridHeight(int gridHeight) {
        CoordinatesParser.gridHeight = gridHeight;
    }

    /**
     * Getter for the grid width.
     *
     * @return the width of the grid.
     */
    public int getGridWidth() {
        return gridWidth;
    }

    /**
     * Setter for the grid width.
     *
     * @param gridWidth the width of the grid.
     */
    public static void setGridWidth(int gridWidth) {
        CoordinatesParser.gridWidth = gridWidth;
    }

    /**
     * Getter for the map of coordinates.
     *
     * @return a map of named coordinates.
     */
    public Map<String, List<Double>> getMapCoordinates() {
        return mapCoordinates;
    }

    /**
     * Setter for the map of coordinates.
     *
     * @param mapCoordinates a map of named coordinates.
     */
    public void setMapCoordinates(Map<String, List<Double>> mapCoordinates) {
        CoordinatesParser.mapCoordinates = mapCoordinates;
    }

    /**
     * Getter for the list of coordinates.
     *
     * @return a list of coordinates.
     */
    public List<Double> getListCoordinates() {
        return listCoordinates;
    }

    /**
     * Setter for the list of coordinates.
     *
     * @param listCoordinates a list of coordinates.
     */
    public static void setListCoordinates(List<Double> listCoordinates) {
        CoordinatesParser.listCoordinates = listCoordinates;
    }

    /**
     * Getter for the array of coordinates.
     *
     * @return a 2D array of coordinates.
     */
    public double[][] getArrayCoordinates() {
        return arrayCoordinates;
    }

    /**
     * Converts a list of coordinate values into a 2D array and sets it as the array of coordinates.
     *
     * @param values the list of coordinate values.
     */
    public static void setArrayCoordinates(List<Double> values) {
        int rows = values.size() / 3;
        arrayCoordinates = new double[rows][2];

        for (int i = 0; i < rows; i++) {
            arrayCoordinates[i][0] = values.get(i * 3);       // First value (longitude)
            arrayCoordinates[i][1] = values.get(i * 3 + 1);   // Second value (latitude)
        }
    }

    /**
     * Constructor that parses and validates a GML file located at the specified path, extracts the coordinates, and calculates the grid representation.
     *
     * @param path the path to the GML file.
     */
    public CoordinatesParser(String path) {
        GMLValidator gmlValidator = new GMLValidator(Paths.get(path));
        try {
            boolean isValid = gmlValidator.validate();
            if (isValid) {
                log.info("GML file is valid, proceeding with parsing.");
                GMLParser parser = new GMLParser(path);
                GMLParser.main(new String[]{});  // Invoke main parser
                Map<String, List<Double>> mapCoordinates = parser.getMapCoordinates();
                List<Double> coordinates = parser.getListCoordinates();
                setMapCoordinates(mapCoordinates);
                setListCoordinates(coordinates);
                setArrayCoordinates(coordinates);
                calculateGrid(arrayCoordinates);  // Compute grid from coordinates
                log.info("Coordinates parsed and stored.");
            } else {
                log.warning("GML file is not valid.");
            }
        } catch (Exception e) {
            log.severe("An error occurred during validation: " + e.getMessage());
        }
    }

    public void calculateGrid(double[][] values) {
        double[][] gridCoordinates = new double[values.length][2];
        double[] minMax = new double[4];

        // Initialize min/max values.
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        // Determine min and max longitude/latitude values.
        for (double[] coord : values) {
            double x = coord[0];
            double y = coord[1];
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
        }

        // Set the min/max values.
        minMax[0] = minX;
        minMax[1] = maxX;
        minMax[2] = minY;
        minMax[3] = maxY;
        setMinMax(minMax);

        // Define grid cell size and calculate grid dimensions.
        double cellSize = 0.1;
        int numRows = (int) Math.ceil((maxX - minX) / cellSize);
        int numCols = (int) Math.ceil((maxY - minY) / cellSize);
        log.info("GridRows: " + numRows);
        log.info("GridCols: " + numCols);

        // Set the grid height and width.
        setGridHeight(numRows);
        setGridWidth(numCols);

        // Normalize the geographic coordinates into grid coordinates.
        for (int i = 0; i < values.length; i++) {
            gridCoordinates[i][0] = (values[i][0] - minX) / (maxX - minX);
            gridCoordinates[i][1] = (values[i][1] - minY) / (maxY - minY);
        }
        setGridCoordinates(gridCoordinates);
    }









}
package src.Env;

import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

import org.example.*;

public class CoordinatesParser {

    private static final java.util.logging.Logger log = Logger.getLogger(CoordinatesParser.class.getName());
    //private static final String path = "./gmlFiles/Frankfurt_Street_Setting_LOD3.gml";

    private static Map<String, List<Double>> mapCoordinates;
    private static List<Double> listCoordinates = new ArrayList<>();
    private static double[][] arrayCoordinates;
    private static int gridHeight;
    private static int gridWidth;
    private final double cellSize = 0.1;

    public int getGridHeight() {
        return gridHeight;
    }

    public static void setGridHeight(int gridHeight) {
        CoordinatesParser.gridHeight = gridHeight;
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public static void setGridWidth(int gridWidth) {
        CoordinatesParser.gridWidth = gridWidth;
    }

    public static Map<String, List<Double>> getMapCoordinates() {
        return mapCoordinates;
    }

    public void setMapCoordinates(Map<String, List<Double>> mapCoordinates) {
        CoordinatesParser.mapCoordinates = mapCoordinates;
    }

    public List<Double> getListCoordinates() {
        return listCoordinates;
    }

    public static void setListCoordinates(List<Double> listCoordinates) {
        CoordinatesParser.listCoordinates = listCoordinates;
    }

    public double[][] getArrayCoordinates() {
        return arrayCoordinates;
    }

    public static void setArrayCoordinates(List<Double> values) {
        int rows = values.size() / 3;
        arrayCoordinates = new double[rows][2];

        for (int i = 0; i < rows; i++) {
            arrayCoordinates[i][0] = values.get(i * 3);       // First value
            arrayCoordinates[i][1] = values.get(i * 3 + 1);   // Second value
        }
    }

    public CoordinatesParser(String path){
        GMLValidator gmlValidator = new GMLValidator(Paths.get(path));
        try {
            boolean isValid = gmlValidator.validate();
            if (isValid) {
                log.info("GML file is valid, Proceeding with parsing.");
                GMLParser parser = new GMLParser(path);
                GMLParser.main(new String[]{});
                Map<String, List<Double>> mapCoordinates = parser.getMapCoordinates();
                List<Double> coordinates = parser.getListCoordinates();
                setMapCoordinates(mapCoordinates);
                setListCoordinates(coordinates);
                setArrayCoordinates(coordinates);
                calculateGrid(arrayCoordinates);
                if (!coordinates.isEmpty()) {
                    log.info("List coordinates stored:");
                    log.info("Coordinate stored in a List.");
                }if(mapCoordinates != null && !mapCoordinates.isEmpty()) {
                    log.info("Map coordinates stored in a Map.");
                }
                else {
                    log.info("No coordinates found");
                }
            } else {
                log.warning("GML file is not valid.");
            }
        }catch (Exception e){
            log.severe("An error occurred during validation: " + e.getMessage());
        }
    }

    public void calculateGrid(double[][] values){
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for(double[] coord : values){
            double x = coord[0];
            double y = coord[1];
            if(x < minX) minX = x;
            if(x > maxX) maxX = x;
            if(y < minY) minY = y;
            if(y > maxY) maxY = y;
        }

        int numRows = (int) Math.ceil((maxX - minX) / cellSize);
        int numCols = (int) Math.ceil((maxY - minY) / cellSize);
        log.info("GridRows: "+numRows);
        log.info("GridCols: "+numCols);

        setGridHeight(numRows);
        setGridWidth(numCols);
    }

}

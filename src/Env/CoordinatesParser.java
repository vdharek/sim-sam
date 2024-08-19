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
}

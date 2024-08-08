package src.Env;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class CoordinateCalc {
    Point point;
    public CoordinateCalc(double[][] coordinates){

        List<double[]> cartesianCoords = new ArrayList<>();
        for (double[] coord : coordinates) {
            double x = lonToX(coord[1]);
            double y = latToY(coord[0]);
            cartesianCoords.add(new double[]{x, y});
        }

        //double area = calculatePolygonArea(cartesianCoords);
        //System.out.println("Total area: " + area + " square meters");
    }

    private static double lonToX(double lon) {
        return lon * 20037508.34 / 180;
    }

    private static double latToY(double lat) {
        double rad = Math.toRadians(lat);
        return Math.log(Math.tan(Math.PI / 4 + rad / 2)) * 20037508.34 / Math.PI;
    }

    public static void calculatePolygonArea(double[][] coordinates) {
        double area = 0;

        if (coordinates.length > 2) {
            for(double[] coord : coordinates){
                Double p1 = coord[0];
                Double p2 = coord[1];
                System.out.println("p1" + p1 + " p2" + p2);
            }

            // Earth radius in meters (assuming WGS84 ellipsoid radius)
            //double earthRadius = 6378137;
            //area = area * earthRadius * earthRadius / 2;
        }

        //return Math.abs(area);
    }

    // Method to convert degrees to radians
    private static double convertToRadian(double input) {
        return input * Math.PI / 180;
    }

}

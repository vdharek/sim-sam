package src.test;

import org.junit.jupiter.api.Test;
import src.model.CoordinatesParser;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoordinatesParserTest {

    Logger logger = Logger.getLogger("CoordinatesParserTest");

    @Test
    void calculateGrid_ValidCoordinates() {
        // Provide a valid path for testing
        String path = "./gmlFiles/PockelStrasse.gml";

        // Create an instance of the parser
        CoordinatesParser parser = new CoordinatesParser(path);

        // Retrieve the parsed coordinates
        double[][] arrayCoordinates = parser.getArrayCoordinates();
        assertNotNull(arrayCoordinates);
        logger.info("ArrayCoordinate stored: PASSES");
        assertNotNull(parser.getMapCoordinates());
        logger.info("MapCoordinate stored: PASSES");
        assertNotNull(parser.getMinMax());
        logger.info("MinMax stored: PASSES");

        // Ensure that the array is not null or empty
        assertNotNull(arrayCoordinates, "Parsed coordinates array should not be null");
        assertTrue(arrayCoordinates.length > 0, "Parsed coordinates array should not be empty");

        // Validate each coordinate for proper DD (Decimal Degree) format
        for (double[] coordinate : arrayCoordinates) {
            String coordinateString = coordinate[0] + "," + coordinate[1];
            assertTrue(isValidDDFormat(coordinateString), "Coordinate should be in valid DD format: " + coordinateString);
            logger.info("Test Passed for: " + coordinateString);
        }
    }

    /**
     * Helper method to validate if a coordinate is in valid DD (Decimal Degree) format.
     *
     * @param coordinateString The coordinate string (e.g., "latitude,longitude")
     * @return true if the coordinate is valid, false otherwise.
     */
    private boolean isValidDDFormat(String coordinateString) {
        try {
            String[] parts = coordinateString.split(",");
            if (parts.length != 2) {
                return false;
            }

            double latitude = Double.parseDouble(parts[0].trim());
            double longitude = Double.parseDouble(parts[1].trim());

            // Validate latitude and longitude ranges
            return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
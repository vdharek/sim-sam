/*
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GridCellTest {

    @Test
    public void testVerifyCellSize() {
        // Test parameters
        double maxX = 10.0;
        double minX = 0.0;
        double maxY = 10.0;
        double minY = 0.0;
        double cellSize = 0.1;

        // Expected values
        int expectedNumRows = 10; // Expected number of rows
        int expectedNumCols = 10; // Expected number of columns
        double expectedCellSizeX = 2.0; // Expected cell size in X direction
        double expectedCellSizeY = 2.0; // Expected cell size in Y direction

        // Calculate the number of rows and columns
        int numRows = (int) Math.ceil((maxX - minX) / cellSize);
        int numCols = (int) Math.ceil((maxY - minY) / cellSize);

        // Verify the number of rows and columns
        assertEquals(expectedNumRows, numRows, "Number of rows is incorrect.");
        assertEquals(expectedNumCols, numCols, "Number of columns is incorrect.");

        // Calculate the actual cell size
        double calculatedCellSizeX = (maxX - minX) / numRows;
        double calculatedCellSizeY = (maxY - minY) / numCols;

        // Verify the calculated cell sizes
        assertTrue(Math.abs(calculatedCellSizeX - expectedCellSizeX) < 0.01,
                "Calculated cell size (X) is not approximately 2 meters.");
        assertTrue(Math.abs(calculatedCellSizeY - expectedCellSizeY) < 0.01,
                "Calculated cell size (Y) is not approximately 2 meters.");

        // Debugging information (optional)
        System.out.println("Number of Rows: " + numRows);
        System.out.println("Number of Columns: " + numCols);
        System.out.println("Calculated Cell Size (X): " + calculatedCellSizeX);
        System.out.println("Calculated Cell Size (Y): " + calculatedCellSizeY);

        // Success message
        System.out.println("Test passed: Rows and columns calculated correctly, and cell size is 2 meters.");
    }
}
*/

/*

import jason.environment.grid.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import src.model.EnvModel;

import java.util.*;

import static org.mockito.Mockito.*;

public class EnvModelTest {

    private EnvModel envModel;

    @BeforeEach
    public void setUp() {
        double[][] coordinates = {};
        Map<String, List<Double>> mapCoordinates = new HashMap<>();
        double[] minMax = {-180, 180, -90, 90};
        envModel = new EnvModel(100, 100, coordinates, mapCoordinates, minMax,10);
    }

    @Test
    public void testInitializeScaling() {
        assertEquals(50, agentModel.getWidth());
        assertEquals(50, agentModel.getHeight());
    }

    @Test
    public void testTransformCoordinates() {
        Location loc = agentModel.transformCoordinates(0, 0);
        assertNotNull(loc);
    }

    @Test
    public void testAddWallInBetween() {
        Location start = new Location(0, 0);
        Location end = new Location(10, 10);
        //agentModel.addWallInBetween(start, end);
        assertTrue(agentModel.wallLocation.contains(start));
        assertTrue(agentModel.wallLocation.contains(end));
    }

    @Test
    public void testMarkCoordinates() {
        Map<String, List<Double>> mapCoordinates = new HashMap<>();
        mapCoordinates.put("Line1", Arrays.asList(0.0, 0.0, 0.0, 10.0, 10.0, 10.0));
        agentModel.markCoordinates(mapCoordinates);
        assertFalse(agentModel.wallLocation.isEmpty());
    }
}
*/

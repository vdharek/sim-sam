package src.model;

import jason.environment.Environment;

import java.util.List;
import java.util.logging.Logger;

/**
 * This class represents a custom environment for a Jason agent system.
 * It extends the Jason {@link Environment} class and provides methods
 * for initializing the environment with specific parameters and models.

 * The environment is designed to handle a graphical representation of
 * agents within a grid, using data parsed from a GML (Geography Markup Language) file.
 */
public class Env extends Environment {

	// Logger instance for logging messages in the environment.
	private static final java.util.logging.Logger log = Logger.getLogger(Env.class.getName());

	// Path to the GML file that contains the coordinate information.
	private static final String path = "./gmlFiles/Frankfurt_Street_Setting_LOD3.gml";

	// AgentModel represents the logical model of agents in the environment.
	private AgentModel agentModel;

	// AgentView is responsible for the graphical display of the agent model.
	private AgentView agentView;

	/**
	 * Initializes the environment with the specified arguments.
	 * This method sets up the agent model and view by parsing coordinates from a GML file.
	 *
	 * @param args Arguments passed during environment initialization, which can be configuration parameters.
	 */
	@Override
	public void init(String[] args) {
		super.init(args);  // Calls the superclass's init method to perform any standard initialization.

		// Parses the coordinates and other necessary data from the specified GML file.
		CoordinatesParser coordinatesParser = new CoordinatesParser(path);

		// Initializes the agent model with the parsed data (grid dimensions and coordinates).
		agentModel = new AgentModel(
				coordinatesParser.getGridWidth(),
				coordinatesParser.getGridHeight(),
				coordinatesParser.getArrayCoordinates(),
				coordinatesParser.getLaneLists(),
				coordinatesParser.getMapCoordinates(),
				coordinatesParser.getMinMax()
		);

		// Initializes the agent view to visually represent the agent model.
		agentView = new AgentView(agentModel);

		// Links the agent view to the agent model for updating the view based on model changes.
		agentModel.setView(agentView);
	}

}

package src.Env;

import jason.environment.Environment;
import java.util.logging.Logger;

public class Env extends Environment {

	private static final java.util.logging.Logger log = Logger.getLogger(Env.class.getName());
	private static final String path = "./gmlFiles/PockelStrasse.gml";

	private AgentModel agentModel;
	private AgentView agentView;

	public void init(String[] args) {
		super.init(args);
		CoordinatesParser coordinatesParser = new CoordinatesParser(path);

		agentModel = new AgentModel(coordinatesParser.getGridWidth(), coordinatesParser.getGridHeight(), coordinatesParser.getArrayCoordinates(), coordinatesParser.getMinMax());
		agentView = new AgentView(agentModel);
		agentModel.setView(agentView);

	}
}

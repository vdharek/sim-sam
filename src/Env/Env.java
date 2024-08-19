package src.Env;

import jason.environment.Environment;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

public class Env extends Environment {

	private static final java.util.logging.Logger log = Logger.getLogger(Env.class.getName());
	private static final String path = "./gmlFiles/Frankfurt_Street_Setting_LOD3.gml";

	private AgentModel agentModel;
	private AgentView agentView;

	private static Map<String, List<Double>> mapCoordinates = new HashMap<>();

	public static void setMapCoordinates(Map<String, List<Double>> mapCoordinates) {
		Env.mapCoordinates = mapCoordinates;
	}

	public void init(String[] args) {
		super.init(args);
		CoordinatesParser coordinatesParser = new CoordinatesParser(path);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int) screenSize.getWidth();
		int height = (int) screenSize.getHeight();

		agentModel = new AgentModel(300, 380, coordinatesParser.getArrayCoordinates());
		agentView = new AgentView(agentModel);
		agentModel.setView(agentView);
	}
}

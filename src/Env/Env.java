package src.Env;

import jason.environment.Environment;

import java.awt.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.example.*;

public class Env extends Environment {

	private static final java.util.logging.Logger log = Logger.getLogger(Env.class.getName());
	private static final String path = "./gmlFiles/PockelStrasse.gml";

	private AgentModel agentModel;
	private AgentView agentView;

	public void init(String[] args) {
		super.init(args);
		List<Double> coordinateList = getCoordinates(path);
		double[][] coordinateArray = convertListToArray(coordinateList);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int) screenSize.getWidth();
		int height = (int) screenSize.getHeight();

		agentModel = new AgentModel(300, 380, coordinateArray);
		agentView = new AgentView(agentModel);
		agentModel.setView(agentView);
	}

	public static List<Double> getCoordinates(String path){
		List<Double> coordinateList = new ArrayList<>();

		GMLValidator gmlValidator = new GMLValidator(Paths.get(path));

		try {
			boolean isValid = gmlValidator.validate();

			if (isValid) {
				log.info("GML file is valid, Proceeding with parsing.");
				GMLParser parser = new GMLParser(path);
				GMLParser.main(new String[]{});
				List<Double> coordinates = parser.getListCoordinates();
				if (coordinates != null && !coordinates.isEmpty()) {
					log.info("Retrieved coordinates:");
					//System.out.println(coordinate);
					coordinateList.addAll(coordinates);
					log.info("Coordinate stored in a List.");
				} else {
					log.info("No coordinates found");
				}
			} else {
				log.warning("GML file is not valid.");
			}
		}catch (Exception e){
			log.severe("An error occurred during validation: " + e.getMessage());
		}
		return coordinateList;
	}

	public static double[][] convertListToArray(List<Double> list) {
		// Calculate the number of rows (each row has 2 elements)
		int rows = list.size() / 3;
		double[][] array = new double[rows][2];

		for (int i = 0; i < rows; i++) {
			array[i][0] = list.get(i * 3);       // First value
			array[i][1] = list.get(i * 3 + 1);   // Second value
		}
		System.out.println("Array length:  "+array.length);
		return array;
	}

}

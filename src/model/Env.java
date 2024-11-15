package src.model;

import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.Location;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Env extends Environment {

	private static final Logger log = Logger.getLogger(Env.class.getName());
	private static final String path = "./gmlFiles/PockelStrasse.gml";
	private AgentModel agentModel;
	private AgentView agentView;

	@Override
	public void init(String[] args) {
		super.init(args);

		try {
			// Parse coordinates and initialize agent model and view
			CoordinatesParser coordinatesParser = new CoordinatesParser(path);

			agentModel = new AgentModel(
					coordinatesParser.getGridWidth(),
					coordinatesParser.getGridHeight(),
					coordinatesParser.getGridCoordinates(),
					coordinatesParser.getMapCoordinates(),
					coordinatesParser.getMinMax(),
					Integer.parseInt(args[0])
			);

			agentView = new AgentView(agentModel);
			agentModel.setView(agentView);

			log.info("Environment initialized with parameters: " + Arrays.toString(args));
		} catch (Exception e) {
			log.log(Level.SEVERE, "Failed to initialize environment: " + e.getMessage(), e);
		}
	}

	@Override
	public boolean executeAction(String agentName, Structure action) {
		log.info("Executing action for agent: " + agentName + " Action: " + action.getFunctor());

		try {
			int agentID = 0; // Assuming agent0 is moving towards agent1
			int targetAgentID = 1; // Assuming agent1 is the target

			Location currentLoc = agentModel.getAgentLocation(agentID);
			Location targetLoc = agentModel.getAgentLocation(targetAgentID);

			if (action.getFunctor().equals("move")) {
				return handleMoveAction(agentID, targetAgentID, currentLoc, targetLoc);
			} else if (action.getFunctor().equals("greet")) {
				return handleGreetAction(action);
			} else {
				log.warning("Unknown action: " + action.getFunctor());
				return false;
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "Error executing action: " + e.getMessage(), e);
			return false;
		}
	}

	private boolean handleMoveAction(int agentID, int targetAgentID, Location currentLoc, Location targetLoc) {
		log.info("Move action received");
		if (currentLoc == null || targetLoc == null) {
			log.warning("Current or target location is null.");
			return false;
		}

		if (agentModel.isNeighbor(currentLoc, targetLoc)) {
			addPercept("agent1", Literal.parseLiteral("neighbor(agent1, agent2)"));
			addPercept("agent2", Literal.parseLiteral("neighbor(agent1, agent2)"));
			log.info("Agent0 is adjacent to Agent1: Ready to greet.");
		} else {
			Location nextStep = agentModel.getNextStepTowards(currentLoc, targetLoc);
			if (nextStep != null) {
				agentModel.moveAgent(agentID, nextStep);
				log.info("Agent0 moved to: " + nextStep);

				clearPercepts("agent1");
				addPercept("agent1", Literal.parseLiteral("position(agent1, " + nextStep.x + ", " + nextStep.y + ")"));
			} else {
				log.warning("No valid next step found for agent0.");
				return false;
			}
		}
		return true;
	}

	private boolean handleGreetAction(Structure action) {
		log.info("Greet action received");
		if (action.getTerms().isEmpty()) {
			log.warning("Greet action has no message.");
			return false;
		}

		String message = ((Atom) action.getTerm(0)).toString();
		log.info("Agent0 greets Agent1 with message: " + message);

		addPercept("agent2", Literal.parseLiteral("greeted(\"" + message + "\")"));
		return true;
	}

	/*@Override
	public boolean executeAction(String agentName, Structure action) {
		log.info("Agent " + agentName + " is executing action: " + action.getFunctor());

		if (action.getFunctor().equals("greet")) {
			// Safely handle both Atom and StringTerm
			String message;
			if (action.getTerm(0) instanceof Atom) {
				message = ((Atom) action.getTerm(0)).toString();
			} else if (action.getTerm(0) instanceof StringTerm) {
				message = ((StringTerm) action.getTerm(0)).getString();
			} else {
				log.warning("Invalid term type for greet action: " + action.getTerm(0));
				return false;
			}

			log.info(agentName + " sends a greeting: " + message);

			// Optionally add a percept for feedback
			addPercept(agentName, Literal.parseLiteral("greeted(\"" + message + "\")"));
			return true;
		} else if (action.getFunctor().equals("move")) {
			log.info(agentName + " is attempting to move.");
			// Implement movement logic here
			return true;
		}

		log.warning("Unknown action: " + action.getFunctor());
		return false;
	}


	*//** Called before the end of MAS execution *//*
	@Override
	public void stop() {
		super.stop();
	}*/

}

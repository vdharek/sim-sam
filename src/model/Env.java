package src.model;

import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.Location;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Env extends Environment implements I_agtClickListener{

	private static final Logger log = Logger.getLogger(Env.class.getName());
	private static final String path = "./gmlFiles/PockelStrasse.gml";
	private AgentModel agentModel;
	private AgentView agentView;
	String[] args;
	Thread simulationThread;

	@Override
	public void init(String[] args) {
		super.init(args);
		this.args = args;
		initAgents(args);
	}

	void initAgents(String[] args){
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

			agentView = new AgentView(this, agentModel);
			agentModel.setView(agentView);

			log.info("Environment initialized with parameters: " + Arrays.toString(args));
			//startSimulationLoop(); // Start the simulation loop
		} catch (Exception e) {
			log.log(Level.SEVERE, "Failed to initialize environment: " + e.getMessage(), e);
		}
	}

	@Override
	public boolean executeAction(String agentName, Structure action) {
		log.info("Executing action for agent: " + agentName + " Action: " + action.getFunctor());

		try {
			for(int i=0; i<agentModel.getTotalAgents(); i++){
				int agentID = i;
				int targetAgentID = (i +1) % agentModel.getTotalAgents();

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
			}
			//int agentID = 0;
			//int targetAgentID = 1;

		} catch (Exception e) {
			log.log(Level.SEVERE, "Error executing action: " + e.getMessage(), e);
		}
		return false;
	}

	void startSimulationLoop() {
		simulationThread = new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(500); //movement speed

					// Periodically update agents' positions and interactions
					for (int agentID = 0; agentID < agentModel.getTotalAgents(); agentID++) {
						int targetAgentID = (agentID + 1) % agentModel.getTotalAgents();
						Location currentLoc = agentModel.getAgentLocation(agentID);
						Location targetLoc = agentModel.getAgentLocation(targetAgentID);

						if (currentLoc != null && targetLoc != null) {
							handleMoveAction(agentID, targetAgentID, currentLoc, targetLoc);
						}
					}

					//updatePercepts();
				} catch (InterruptedException e) {
					log.severe("Simulation loop interrupted: " + e.getMessage());
					break;
				}
			}
		});
		simulationThread.start();
	}

	void updatePercepts() {
		clearAllPercepts();
		for (int agentID = 0; agentID < agentModel.getTotalAgents(); agentID++) {
			Location loc = agentModel.getAgentLocation(agentID);
			if (loc != null) {
				addPercept("agent" + (agentID + 1),
						Literal.parseLiteral("position(agent" + (agentID + 1) + ", " + loc.x + ", " + loc.y + ")"));
			}
		}
	}

	private boolean handleMoveAction(int agentID, int targetAgentID, Location currentLoc, Location targetLoc) {
		log.info("Move action received");

		if (currentLoc == null || targetLoc == null) {
			log.warning("Current or target location is null.");
			return false;
		}

		if (agentModel.isNeighbor(currentLoc, targetLoc)) {
			// Agents are neighbors, add percept and stop movement
			addPercept("agent" + (agentID + 1), Literal.parseLiteral("neighbor(agent" + (agentID + 1) + ", agent" + (targetAgentID + 1) + ")"));
			log.info("Agent" + agentID + " and Agent" + targetAgentID + " are neighbors.");
		} else {
			// Move agent towards the target
			Location nextStep = agentModel.getNextStepTowards(currentLoc, targetLoc);
			if (nextStep != null) {
				agentModel.moveAgent(agentID, nextStep);
				log.info("Agent " + agentID + " moved to: " + nextStep);

				// Update percepts
				clearPercepts("agent" + (agentID + 1));
				addPercept("agent" + (agentID + 1), Literal.parseLiteral("position(agent" + (agentID + 1) + ", " + nextStep.x + ", " + nextStep.y + ")"));
			} else {
				log.warning("No valid next step found for agent " + agentID);
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
		log.info("Agent greets another agent with message: " + message);

		addPercept("agent2", Literal.parseLiteral("greeted(\"" + message + "\")"));
		return true;
	}

	void stopSimulationLoop() {

		// Interrupt the thread to ensure it stops if sleeping
		if (simulationThread != null && simulationThread.isAlive()) {
			simulationThread.interrupt();
		}

		log.info("Simulation stopped and reset.");
	}

	@Override
	public void onStartClick() {
		startSimulationLoop();
	}

	@Override
	public void onStopClick() {
		stopSimulationLoop();
	}
}

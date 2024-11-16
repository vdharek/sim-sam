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
    private volatile boolean isRunning = false;
    private volatile boolean isPaused = false;
    private Thread simulationThread;
    private boolean isInitialized = false;

    @Override
    public void init(String[] args) {
        super.init(args);

        try {
            initializeEnvironment(args);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to initialize environment: " + e.getMessage(), e);
        }
    }

    private void initializeEnvironment(String[] args) throws Exception {
        if (isInitialized) {
            log.info("Environment is already initialized.");
            return;
        }

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

        isInitialized = true; // Mark environment as initialized
        log.info("Environment initialized with parameters: " + Arrays.toString(args));
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

    public void startSimulation() {
        if (isRunning) {
            log.info("Simulation is already running.");
            return;
        }

        if (!isInitialized) {
            log.log(Level.SEVERE, "Environment must be initialized before starting the simulation.");
            return;
        }

        isRunning = true;
        isPaused = false;

        simulationThread = new Thread(() -> {
            log.info("Simulation thread entered...");
            try {
                while (isRunning) {
                    log.info("Simulation thread running...");

                    synchronized (this) {
                        while (isPaused) {
                            log.info("Simulation thread paused...");
                            wait(); // Pause the thread
                        }
                    }

                    // Simulation logic
                    for (int agentID = 0; agentID < agentModel.getMaxAgents(); agentID++) {
                        int targetAgentID = (agentID + 1) % agentModel.getMaxAgents();
                        Location currentLoc = agentModel.getAgentLocation(agentID);
                        Location targetLoc = agentModel.getAgentLocation(targetAgentID);

                        if (currentLoc != null && targetLoc != null) {
                            handleMoveAction(agentID, targetAgentID, currentLoc, targetLoc);
                            log.info("Agent " + agentID + " moved towards Agent " + targetAgentID);
                        }
                    }

                    // Update the environment percepts
                    updatePercepts();

                    Thread.sleep(200); // Adjust simulation speed
                }
            } catch (InterruptedException e) {
                log.info("Simulation loop interrupted.");
            } catch (Exception e) {
                log.log(Level.SEVERE, "Error in simulation thread: " + e.getMessage(), e);
            }
            log.info("Simulation thread exiting...");
        });

        simulationThread.start();
        log.info("Simulation thread started.");
    }

    public void stopSimulation() {
        if (!isRunning) {
            log.warning("Simulation is not running. Cannot stop.");
            return;
        }

        isRunning = false;

        // Interrupt the thread to ensure it stops if sleeping
        if (simulationThread != null && simulationThread.isAlive()) {
            simulationThread.interrupt();
        }

        log.info("Simulation stopped and reset.");
    }

    private void updatePercepts() {
        clearAllPercepts();
        for (int agentID = 0; agentModel != null && agentID < agentModel.getMaxAgents(); agentID++) {
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
}
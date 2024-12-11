package src.model;

import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.Location;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Env extends Environment implements I_agtClickListener {

    private static final Logger log = Logger.getLogger(Env.class.getName());
    private static final String path = "./gmlFiles/PockelStrasse.gml";
    private EnvModel envModel;
    private AgentModel agentModel;
    private Thread simulationThread;

    private Location agent5StartLocation; // Agent 5's starting position
    private int currentTargetAgent = 0; // Tracks the current agent being approached

    @Override
    public void init(String[] args) {
        super.init(args);
        initAgents(args);
    }

    void initAgents(String[] args) {
        try {
            // Initialize environment model
            CoordinatesParser coordinatesParser = new CoordinatesParser(path);

            envModel = new EnvModel(
                    coordinatesParser.getGridWidth(),
                    coordinatesParser.getGridHeight(),
                    coordinatesParser.getMapCoordinates(),
                    coordinatesParser.getMinMax(),
                    Integer.parseInt(args[0])
            );
            this.agentModel = new AgentModel(envModel);
            AgentView agentView = new AgentView(this, envModel);
            envModel.setView(agentView);

            log.info("Environment initialized with parameters: " + Arrays.toString(args));

            // Store Agent 5's starting location
            agent5StartLocation = agentModel.getAgentLocation(4);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to initialize environment: " + e.getMessage(), e);
        }
    }

    void startSimulationLoop() {
        log.info("Simulation started.");
        simulationThread = new Thread(() -> {
            int maxRetries = 5; // Maximum retries for finding a valid path
            int retryCount = 0;

            while (true) {
                try {
                    Thread.sleep(80); // Adjust as needed

                    Location agent5Loc = agentModel.getAgentLocation(4); // Get Agent 5's location
                    if (agent5Loc == null) {
                        log.severe("Agent 5's location is null. Cannot proceed.");
                        stopSimulationLoop();
                        break;
                    }

                    Location targetLoc;
                    if (currentTargetAgent < 4) {
                        targetLoc = agentModel.getAgentLocation(currentTargetAgent);
                    } else {
                        targetLoc = agent5StartLocation;
                    }

                    if (targetLoc == null) {
                        log.severe("Target location for Agent 5 is null. Skipping to the next agent.");
                        currentTargetAgent++;
                        continue; // Skip this target
                    }

                    if (agent5Loc.equals(agent5StartLocation) && currentTargetAgent >= 4) {
                        log.info("Agent 5 has returned to its starting position. Simulation ends.");
                        stopSimulationLoop();
                        break;
                    }

                    if (agent5Loc.equals(targetLoc)) {
                        if (currentTargetAgent < 4) {
                            log.info("Agent 5 greeted Agent " + (currentTargetAgent + 1) + ".");
                            handleGreetAction("hello from Agent 5");
                        }
                        currentTargetAgent++;
                        retryCount = 0; // Reset retry count for the next target
                        continue;
                    }

                    Location nextStep = agentModel.getNextStepTowards(agent5Loc, targetLoc);

                    if (nextStep.equals(agent5Loc)) { // No progress made
                        retryCount++;
                        log.warning("Agent 5 could not find a valid path. Retry count: " + retryCount);

                        if (retryCount >= maxRetries) {
                            if (currentTargetAgent < 4) {
                                log.warning("Agent 5 is stuck. Skipping to the next target.");
                                currentTargetAgent++;
                            } else {
                                log.warning("Agent 5 is stuck. Returning to starting position.");
                            }
                            retryCount = 0; // Reset retry count
                        }
                        continue; // Retry or move to the next target
                    } else {
                        retryCount = 0; // Reset retries after successful move
                        agentModel.moveAgent(4, nextStep);
                        log.info("Agent 5 moved to " + nextStep);
                    }

                    updatePercepts();

                } catch (InterruptedException e) {
                    log.severe("Simulation loop interrupted: " + e.getMessage());
                    break;
                } catch (Exception e) {
                    log.severe("Unexpected error in simulation loop: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        simulationThread.start();
    }

    private void handleGreetAction(String message) {
        if (currentTargetAgent < 4) {
            log.info("Agent 5 greets Agent " + (currentTargetAgent + 1) + " with message: " + message);
            addPercept("agent" + (currentTargetAgent + 1),
                    Literal.parseLiteral("greeted(\"" + message + "\")"));
        }
    }

    void updatePercepts() {
        clearAllPercepts();
        for (int agentID = 0; agentID < envModel.getTotalAgents(); agentID++) {
            Location loc = agentModel.getAgentLocation(agentID);
            if (loc != null) {
                addPercept("agent" + (agentID + 1),
                        Literal.parseLiteral("position(agent" + (agentID + 1) + ", " + loc.x + ", " + loc.y + ")"));
            }
        }
    }

    void stopSimulationLoop() {
        if (simulationThread != null && simulationThread.isAlive()) {
            simulationThread.interrupt();
        }
        log.info("Simulation stopped.");
    }

    @Override
    public void onStartClick() {
        startSimulationLoop();
    }

    @Override
    public void onStopClick() {
        stopSimulationLoop();
    }

    @Override
    public void onClickReset() {
        clearAllPercepts();
    }
}

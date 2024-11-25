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
            while (true) {
                try {
                    Thread.sleep(200); // Adjust as needed

                    Location agent5Loc = agentModel.getAgentLocation(4); // Get Agent 5's location
                    Location targetLoc;
                    if (currentTargetAgent < 4) {
                        targetLoc = agentModel.getAgentLocation(currentTargetAgent);
                    } else {
                        targetLoc = agent5StartLocation;
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
                        continue; // Move to the next target in the next iteration
                    }

                    // Handle movement
                    Location nextStep = agentModel.getNextStepTowards(agent5Loc, targetLoc);
                    //currentTargetAgent++;
                    if (nextStep == null) {
                        log.info("Footpath encountered by Agent 5. Redirecting to next agent.");
                        currentTargetAgent++;
                        continue; // Redirect to next agent
                    } else {
                        agentModel.moveAgent(4, nextStep); // Move Agent 5
                        //log.info("Agent 5 moved to " + nextStep);
                    }

                    updatePercepts();
                } catch (InterruptedException e) {
                    log.severe("Simulation loop interrupted: " + e.getMessage());
                    break;
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

        // Detect if Agent 5 encounters FOOTPATH
        Location agent5Loc = agentModel.getAgentLocation(4);
        if (agent5Loc != null || envModel.hasObject(EnvModel.FOOTPATH, agent5Loc)) {
            //addPercept("agent5", Literal.parseLiteral("encountered_footpath(" + agent5Loc.x + ", " + agent5Loc.y + ")"));
            addPercept("agent5", Literal.parseLiteral("ENV Class: FOOTPATH encountered by Agent 5"));
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
}

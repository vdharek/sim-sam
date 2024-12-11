package src.model;

import jason.asSemantics.DefaultInternalAction;
import jason.environment.grid.Location;

import java.util.*;
import java.util.logging.Logger;

import static src.model.EnvModel.*;

public class AgentModel extends DefaultInternalAction {

    Logger log = Logger.getLogger(AgentModel.class.getName());

    EnvModel envModel;

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations() {
        List<Location> staticLocation  = new ArrayList<>();
        staticLocation.add(new Location(200, 204));
        staticLocation.add(new Location(210, 68));
        staticLocation.add(new Location(345, 126));
        this.locations = staticLocation;
    }

    List<Location> locations = new ArrayList<>();

    public AgentModel(EnvModel envModel){
        this.envModel = envModel;
        //initiateStaticAgents();
        setLocations();
    }

    void initiateStaticAgents(Location loc){
        Random rand = new Random();

// Randomly place agents
        for (int i = 0; i < envModel.getTotalAgents(); i++) {
            boolean validPosition = false; // Flag to ensure valid placement
            Location randomLocation = null;

            while (!validPosition) {
                // Generate random coordinates
                int randX = rand.nextInt(loc.x);
                int randY = rand.nextInt(loc.y);
                randomLocation = new Location(randX, randY);

                // Check if the location is free of obstacles
                if (!envModel.hasObject(OBSTACLE, randomLocation)) {
                    validPosition = true; // Found a valid position
                }
            }

            // Place the agent at the valid location
            envModel.setAgPos(i, randomLocation.x, randomLocation.y);
            envModel.agentLocations.add(randomLocation);
            envModel.add(STATIC_AGENT, randomLocation.x, randomLocation.y);

            // Debugging output
            System.out.println("Agent " + i + " placed at: (" + randomLocation.x + ", " + randomLocation.y + ")");
        }

        /*envModel.setAgPos(0, 324, 64);
        envModel.setAgPos(1, 210, 68);
        envModel.setAgPos(2, 345, 126);
        envModel.setAgPos(3, 200, 204);
        envModel.setAgPos(4, 266, 145);

        envModel.agentLocations.add(new Location(324, 64));
        envModel.agentLocations.add(new Location(210, 68));
        envModel.agentLocations.add(new Location(345, 126));
        envModel.agentLocations.add(new Location(200, 204));
        envModel.agentLocations.add(new Location(266, 145));

        envModel.add(STATIC_AGENT, 324, 64);
        envModel.add(STATIC_AGENT, 210, 68);
        envModel.add(STATIC_AGENT, 345, 126);
        envModel.add(STATIC_AGENT, 200, 204);
        envModel.add(STATIC_AGENT, 266, 145);*/
    }

    /*public void initiateAgents(Location loc) {
        if (!(envModel.agentLocations.contains(loc))) {
            envModel.setAgPos(envModel.agentAdded, loc);
            envModel.add(AGENT, loc);
            log.info("Agent added at: " + loc + " Agent ID: " + envModel.agentAdded);
            int agent = envModel.getTotalAgents();
            agent++;
            envModel.setTotalAgents(agent);
            envModel.agentLocations.add(loc);
            //log.info("Agent added at: " + loc);
        }
    }*/

    public Location getAgentLocation(int agentID) {
        if (agentID >= 0 && agentID < envModel.agentLocations.size()) {
            return envModel.agentLocations.get(agentID);
        }
        //return envModel.getAgPos(agentID);
        return null;
    }

    public Location getNextStepTowards(Location from, Location to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Both 'from' and 'to' locations must be non-null.");
        }

        int newX = from.x;
        int newY = from.y;

        // Determine the primary next step
        if (from.x < to.x) newX++;
        else if (from.x > to.x) newX--;

        if (from.y < to.y) newY++;
        else if (from.y > to.y) newY--;

        Location nextLocation = new Location(newX, newY);

        try {
            boolean hasDriving = envModel.hasObject(DRIVING, nextLocation);
            boolean hasParking = envModel.hasObject(PARKING, nextLocation);
            boolean hasFootpath = envModel.hasObject(FOOTPATH, nextLocation);

            if (hasFootpath) {
                log.warning("Footpath detected at location: " + nextLocation + ". Attempting alternative paths.");

                List<Location> alternatives = Arrays.asList(
                        new Location(from.x + 1, from.y), // Move right
                        new Location(from.x - 1, from.y), // Move left
                        new Location(from.x, from.y + 1), // Move down
                        new Location(from.x, from.y - 1)  // Move up
                );

                for (Location alternative : alternatives) {
                    boolean isValid = !envModel.hasObject(FOOTPATH, alternative) &&
                            (envModel.hasObject(DRIVING, alternative) || envModel.hasObject(PARKING, alternative));
                    if (isValid) {
                        log.info("Alternative path found: " + alternative);
                        return alternative;
                    }
                }
                // If no valid alternatives, stay in place
                log.warning("No alternative path available. Staying in the current position.");
                return from; // Stay in the current location
            }
            if(hasParking){
                log.info("Parking found: Entry allowed");
                return nextLocation;
            }
            if(hasDriving){
                log.info("Driving found: Entry allowed.");
                return nextLocation;
            }

            return nextLocation;

        } catch (Exception e) {
            log.severe("Unexpected error in getNextStepTowards: " + e.getMessage());
            throw new RuntimeException("Error in pathfinding logic.", e);
        }
    }

    public void moveAgent(int agentID, Location goalLocation) {
        if (agentID < 0 || agentID >= envModel.agentLocations.size()) {
            System.out.println("Invalid agent ID: " + agentID);
            return;
        }

        Location currentLocation = envModel.agentLocations.get(agentID);
        int currentX = currentLocation.x;
        int currentY = currentLocation.y;

        Random rand = new Random();

        // Generate random steps
        int stepX = Integer.compare(goalLocation.x, currentX);
        int stepY = (goalLocation.y > currentY) ? 1 : ((goalLocation.y < currentY) ? -1 : 0);

        // Introduce randomness to steps
        if (rand.nextBoolean()) {
            stepX += rand.nextInt(3) - 1; // Adds -1, 0, or 1
        }
        if (rand.nextBoolean()) {
            stepY += rand.nextInt(3) - 1; // Adds -1, 0, or 1
        }

        // Calculate the new position
        int newX = Math.max(0, Math.min(envModel.getScreenWidth(), currentX + stepX)); // Ensure within bounds
        int newY = Math.max(0, Math.min(envModel.getScreenHeight(), currentY + stepY)); // Ensure within bounds

        Location newLocation = new Location(newX, newY);

        // Check if the new location is valid (e.g., no obstacles)
        if (!envModel.hasObject(OBSTACLE, newLocation)) {
            // Update agent's position
            envModel.remove(STATIC_AGENT, currentLocation);
            envModel.add(STATIC_AGENT, newLocation);
            envModel.agentLocations.set(agentID, newLocation);
            envModel.setAgPos(agentID, newLocation);

        } else {
            // If new location has an obstacle, retry the movement next time
            log.info("Agent " + agentID + " encountered an obstacle at: (" + newX + ", " + newY + ")");
        }
    }

}
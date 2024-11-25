package src.model;

import jason.asSemantics.DefaultInternalAction;
import jason.environment.grid.Location;
import java.util.logging.Logger;

import static src.model.EnvModel.*;

public class AgentModel extends DefaultInternalAction {

    Logger log = Logger.getLogger(AgentModel.class.getName());

    EnvModel envModel;

    public AgentModel(EnvModel envModel){
        this.envModel = envModel;
        initiateStaticAgents();
    }

    void initiateStaticAgents(){
        envModel.setAgPos(0, 324, 64);
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
        envModel.add(STATIC_AGENT, 266, 145);
        //envModel.updatePercepts();
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
        int newX = from.x;
        int newY = from.y;

        if (from.x < to.x) newX++;
        else if (from.x > to.x) newX--;

        if (from.y < to.y) newY++;
        else if (from.y > to.y) newY--;

        Location nextLocation = new Location(newX, newY);

        boolean hasDriving = envModel.hasObject(DRIVING, nextLocation);
        boolean hasParking = envModel.hasObject(PARKING, nextLocation);
        boolean hasFootpath = envModel.hasObject(FOOTPATH, nextLocation);

        if(hasFootpath){
            return null;
        }
        return nextLocation; // Default case: Move if no specific terrain
    }

    public void moveAgent(int agentID, Location newLocation) {
        if (agentID >= 0 && agentID < envModel.agentLocations.size()) {
            Location currentLocation = envModel.agentLocations.get(agentID);
            envModel.remove(STATIC_AGENT, currentLocation);
            envModel.add(STATIC_AGENT, newLocation);
            envModel.agentLocations.set(agentID, newLocation);
            envModel.setAgPos(agentID, newLocation);
        }
    }
}
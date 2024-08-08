package src.Env;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;

import java.awt.*;
import java.awt.Point;
import java.util.ArrayList;
import java.util.logging.Logger;

public class AgentModel extends GridWorldModel {

    Logger log = Logger.getLogger(AgentModel.class.getName());

    int gridWidth = 1;
    int gridHeight = 1;

    public AgentModel(int h, int w) {
        super(h,w,0);
    }

}

package src.Env;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;

import java.awt.*;

public class AgentView extends GridWorldView {

    public AgentView(AgentModel model) {
        super(model, "Environment", model.getWidth());
        setResizable(true);
        setVisible(true);
    }

    @Override
    public void draw(Graphics g, int x, int y, int object) {
        super.draw(g, x, y, object);

        // Drawing priority: WALL > OBSTACLE > empty
        if ((object & AgentModel.WALL) != 0) {
            g.setColor(Color.BLACK);  // Color for WALL
            g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
        } else if ((object & AgentModel.OBSTACLE) != 0) {
            g.setColor(Color.GRAY);  // Color for OBSTACLE
            g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
        }else if(((object & AgentModel.AGENTS) != 0)){
            g.setColor(Color.red);
            g.fillOval(x , y * cellSizeH, cellSizeW, cellSizeH);
        }
    }

    @Override
    public void drawEmpty(Graphics g, int x, int y) {
        super.drawEmpty(g, x, y);
        g.setColor(Color.WHITE);
        g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
    }
}

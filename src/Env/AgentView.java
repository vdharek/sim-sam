package src.Env;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;

import java.awt.*;

public class AgentView extends GridWorldView {

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int sWidth = (int) screenSize.getWidth();
    int height = (int) screenSize.getHeight();

    private AgentModel agentModel;
    public AgentView(AgentModel model) {
        super(model, "Environment", model.getWidth()/2); // Set initial window size
        setVisible(true);
        this.agentModel = model;
        //setSize(sWidth, height);
        //repaint();
    }

    /*@Override
    public void draw(Graphics g, int x, int y, int object) {
        super.draw(g, x, y, object);
        if (object == AgentModel.WALL) { // Use AGENT identifier
            g.setColor(Color.BLACK);
            g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
            //System.out.println("cellSizeW = " + cellSizeW);
            //System.out.println("cellSizeH = " + cellSizeH);
            //super.drawAgent(g, x, y, Color.RED, -1); // Drawing agent as a red cell
        }
        if(object == AgentModel.OBSTACLE){
            g.setColor(Color.GRAY);
            g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
        }
    }*/

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
        }
    }

    @Override
    public void drawEmpty(Graphics g, int x, int y) {
        super.drawEmpty(g, x, y);
        g.setColor(Color.WHITE);
        g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
    }
}

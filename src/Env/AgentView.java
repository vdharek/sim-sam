package src.Env;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;

import java.awt.*;

public class AgentView extends GridWorldView {
    public AgentView(AgentModel model) {
        super(model, "Mercator Projection Grid", 800); // Set initial window size
        setVisible(true);
        repaint();
    }

    @Override
    public void draw(Graphics g, int x, int y, int object) {
        if (object == AgentModel.AGENT) { // Use AGENT identifier
            g.setColor(Color.RED);
            super.drawAgent(g, x, y, Color.RED, -1); // Drawing agent as a red cell
        }
    }

    /*@Override
    public void drawBackground(Graphics g, int width, int height) {
        super.setBackground(g, width, height);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14)); // Adjust font as needed
        g.drawString("Mercator Projection Grid", 10, 20);
    }*/
}

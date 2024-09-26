package src.Env;

import jason.environment.grid.GridWorldView;

import java.awt.*;

/**
 * The {@code AgentView} class provides a graphical interface for visualizing the grid-based environment.
 * It extends {@link GridWorldView} from the Jason framework and allows rendering of objects like walls, obstacles, and agents.
 */
public class AgentView extends GridWorldView {

    /**
     * Constructs an {@code AgentView} instance that displays the grid environment.
     *
     * @param model the {@link AgentModel} representing the underlying grid model of the environment.
     */
    public AgentView(AgentModel model) {
        // Call the superclass constructor to initialize the view with the model and window title.
        super(model, "Environment", model.getWidth());
        setResizable(true);  // Allow the window to be resized.
        setVisible(true);    // Make the window visible.
    }

    /**
     * Overrides the {@code draw} method to render specific objects in the grid, including walls, obstacles, and agents.
     *
     * @param g      the {@link Graphics} object used for drawing.
     * @param x      the x-coordinate in the grid.
     * @param y      the y-coordinate in the grid.
     * @param object the object type to be drawn (e.g., wall, obstacle, or agent).
     */
    @Override
    public void draw(Graphics g, int x, int y, int object) {
        super.draw(g, x, y, object);  // Call the superclass method for default drawing behavior.

        // Check and draw objects in the order of priority: WALL > OBSTACLE > AGENTS.
        if ((object & AgentModel.WALL) != 0) {
            // If the object is a WALL, set color to black and fill the corresponding grid cell.
            g.setColor(Color.BLACK);
            g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
        } else if ((object & AgentModel.OBSTACLE) != 0) {
            // If the object is an OBSTACLE, set color to gray and fill the corresponding grid cell.
            g.setColor(Color.GRAY);
            g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
        } else if ((object & AgentModel.AGENTS) != 0) {
            // If the object is an AGENT, set color to red and draw a filled oval in the grid cell.
            g.setColor(Color.RED);
            g.fillOval(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
        }
    }

    /**
     * Overrides the {@code drawEmpty} method to render empty cells in the grid.
     *
     * @param g the {@link Graphics} object used for drawing.
     * @param x the x-coordinate of the empty cell.
     * @param y the y-coordinate of the empty cell.
     */
    @Override
    public void drawEmpty(Graphics g, int x, int y) {
        super.drawEmpty(g, x, y);  // Call the superclass method to clear the grid cell.

        // Set the color to white and fill the empty grid cell with white color.
        g.setColor(Color.WHITE);
        g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
    }
}

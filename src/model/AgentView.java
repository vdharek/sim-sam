package src.model;

import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;

/**
 * The {@code AgentView} class provides a graphical interface for visualizing the grid-based environment.
 * It extends {@link GridWorldView} from the Jason framework and allows rendering of objects like walls, obstacles, and agents.
 */
public class AgentView extends GridWorldView {
    private static final Logger log = Logger.getLogger(AgentView.class.getName());

    /**
     * Constructs an {@code AgentView} instance that displays the grid environment.
     *
     * @param model the {@link AgentModel} representing the underlying grid model of the environment.
     */
    //AgentModel agentModel;
    AgentModel agentModel;
    I_agtClickListener iAgtClickListener;
    public AgentView(I_agtClickListener iAgtClickListener, EnvModel model) {
        super(model, "Environment", (int)model.getScreenSize());
        initializeButtons();
        setVisible(true);    // Make the window visible.
        this.agentModel = new AgentModel(model);
        this.iAgtClickListener = iAgtClickListener;
    }

    private JButton startButton;  // Button to start the simulation
    private JButton stopButton;   // Button to stop the simulation

    private void initializeButtons() {
        // Create buttons
        startButton = new JButton("Start");
        stopButton = new JButton("Stop");

        // Add action listeners to buttons
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                iAgtClickListener.onStartClick();
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                iAgtClickListener.onStopClick();
            }
        });

        // Add buttons to the view
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);


        this.add(buttonPanel, BorderLayout.SOUTH);
        getCanvas().addMouseListener(new MouseListener() {
            /*public void mouseClicked(MouseEvent e) {
                int col = e.getX() / cellSizeW;
                int lin = e.getY() / cellSizeH;
                if (col >= 0 && lin >= 0 && col < getModel().getWidth() && lin < getModel().getHeight()) {

                    Location loc = new Location(col, lin);
                    //agentModel.initiateAgents(loc);

                    update(col, lin);
                    //env.updatePercepts();
                }
            }*/
            public void mouseClicked(MouseEvent e) {
                int col = e.getX() / cellSizeW;
                int lin = e.getY() / cellSizeH;
                if (col >= 0 && lin >= 0 && col < getModel().getWidth() && lin < getModel().getHeight()) {

                    Location loc = new Location(col, lin);
                    //floodFillRecursive(loc, new HashSet<>());
                    fillPolygon(loc);

                    // Refresh the view to reflect changes
                    repaint();
                }
            }
            public void mouseExited(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
        });
    }

    private void fillPolygon(Location startLoc) {
        Stack<Location> stack = new Stack<>();
        Set<Location> visited = new HashSet<>();

        // Push the starting location onto the stack
        stack.push(startLoc);
        visited.add(startLoc);

        while (!stack.isEmpty()) {
            Location loc = stack.pop();

            // Mark the current location as FOOTPATH
            agentModel.envModel.add(EnvModel.FOOTPATH, loc);

            // Explore neighbors in four directions
            for (int[] dir : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                int newX = loc.x + dir[0];
                int newY = loc.y + dir[1];
                Location neighbor = new Location(newX, newY);

                // Skip if already visited
                if (visited.contains(neighbor)) continue;

                // Check bounds
                if (newX >= 0 && newY >= 0 && newX < getModel().getWidth() && newY < getModel().getHeight()) {
                    // Check if the cell is a boundary
                    if (!agentModel.envModel.hasObject(EnvModel.FOOTPATH, neighbor) &&
                            !agentModel.envModel.hasObject(EnvModel.DRIVING, neighbor) &&
                            !agentModel.envModel.hasObject(EnvModel.PARKING, neighbor) &&
                            !agentModel.envModel.hasObject(EnvModel.OBSTACLE, neighbor)) {
                        // Push neighbor to stack
                        stack.push(neighbor);
                        visited.add(neighbor);
                    }
                }
            }
        }
    }

    @Override
    public void draw(Graphics g, int x, int y, int object) {
        super.draw(g, x, y, object);  // Call the superclass method for default drawing behavior.

        //Polygon polygon = agentModel.envModel.getPolygon();

        if ((object & EnvModel.FOOTPATH) != 0) {
            g.setColor(new Color(184, 184, 184)); // Footpath color
            g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
        }
        if ((object & EnvModel.DRIVING) != 0) {
            g.setColor(new Color(135, 135, 135)); // Driving lane color
            g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
        }
        if ((object & EnvModel.PARKING) != 0) {
            g.setColor(new Color(172, 172, 172)); // Parking color
            g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
        }
        if ((object & EnvModel.AGENT) != 0) {
            g.setColor(new Color(5, 5, 5)); // Agent color
            g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
        }
        if ((object & EnvModel.STATIC_AGENT) != 0) {
            /*g.setColor(new Color(255, 210, 0)); // Static agent color
            g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);*/
            g.setColor(new Color(255, 255, 255)); // Static agent color

            // Increase the rectangle size slightly
            int padding = 2; // Padding to expand the size
            int adjustedWidth = cellSizeW + padding;
            int adjustedHeight = cellSizeH + padding;

            // Offset the position to center the larger rectangle
            int adjustedX = x * cellSizeW - padding / 2;
            int adjustedY = y * cellSizeH - padding / 2;

            g.fillRect(adjustedX, adjustedY, adjustedWidth, adjustedHeight);
        }
    }


    @Override
    public void drawObstacle(Graphics g, int x, int y) {
        super.drawObstacle(g, x, y);
        Color c = new Color(255, 255, 255);
        g.setColor(c);
        g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
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
        super.drawEmpty(g, x, y);
        Color c = new Color(255, 255, 255);
        g.setColor(c);
        g.fillRect(x * this.cellSizeW + 1, y * this.cellSizeH + 1, this.cellSizeW - 2, this.cellSizeH - 2);
        Color c2 = new Color(255, 93, 93);
        g.setColor(c2);
        g.drawRect(x * this.cellSizeW, y * this.cellSizeH, this.cellSizeW, this.cellSizeH);
    }
}

package src.model;

import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
    AgentModel agentModel;
    Env env;
    I_agtClickListener iAgtClickListener;
    int click = 0;
    public AgentView(I_agtClickListener iAgtClickListener, AgentModel model) {
        // Call the superclass constructor to initialize the view with the model and window title.
        super(model, "Environment", (int)model.getScreenWindow());
        initializeButtons();
        setVisible(true);    // Make the window visible.
        this.agentModel = model;
        this.iAgtClickListener = iAgtClickListener;
        this.env = new Env();
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
            public void mouseClicked(MouseEvent e) {
                int col = e.getX() / cellSizeW;
                int lin = e.getY() / cellSizeH;
                if (col >= 0 && lin >= 0 && col < getModel().getWidth() && lin < getModel().getHeight()) {

                    Location loc = new Location(col, lin);
                    agentModel.initiateAgents(loc);
                    update(col, lin);
                    env.updatePercepts();
                }
            }
            public void mouseExited(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
        });

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
        if ((object & AgentModel.BRICKS) != 0) {
            // If the object is a WALL, set color to black and fill the corresponding grid cell.
            g.setColor(Color.BLACK);
            g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
        } else if ((object & AgentModel.OBSTACLE) != 0) {
            // If the object is an OBSTACLE, set color to gray and fill the corresponding grid cell.
            g.setColor(Color.GRAY);
            g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
        } else if ((object & AgentModel.AGENT) != 0) {
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

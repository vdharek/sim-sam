package src.model;

import jason.environment.grid.GridWorldView;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;

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
        super(model, "Environment", (int)model.getScreenWindow());
        setVisible(true);    // Make the window visible.
    }

    Env env = new Env();

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
            g.setColor(Color.lightGray);
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

    JLabel jlMouseLoc;
    JComboBox scenarios;
    JSlider   jSpeed;
    JLabel    jGoldsC;

    @Override
    public void initComponents(int width) {
        super.initComponents(width);

        // Dropdown for Scenarios
        scenarios = new JComboBox();
        for (int i = 1; i <= 3; i++) {
            scenarios.addItem(i);
        }
        JPanel args = new JPanel();
        args.setLayout(new BoxLayout(args, BoxLayout.Y_AXIS));

        JPanel sp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sp.setBorder(BorderFactory.createEtchedBorder());
        sp.add(new JLabel("Scenario:"));
        sp.add(scenarios);

        // Speed slider
        jSpeed = new JSlider();
        jSpeed.setMinimum(0);
        jSpeed.setMaximum(400);
        jSpeed.setValue(50);
        jSpeed.setPaintTicks(true);
        jSpeed.setPaintLabels(true);
        jSpeed.setMajorTickSpacing(100);
        jSpeed.setMinorTickSpacing(20);
        jSpeed.setInverted(true);
        Hashtable<Integer, Component> labelTable = new Hashtable<>();
        labelTable.put(0, new JLabel("max"));
        labelTable.put(200, new JLabel("speed"));
        labelTable.put(400, new JLabel("min"));
        jSpeed.setLabelTable(labelTable);
        JPanel p = new JPanel(new FlowLayout());
        p.setBorder(BorderFactory.createEtchedBorder());
        p.add(jSpeed);

        args.add(sp);
        args.add(p);

        // Status message panel
        JPanel msg = new JPanel();
        msg.setLayout(new BoxLayout(msg, BoxLayout.Y_AXIS));
        msg.setBorder(BorderFactory.createEtchedBorder());

        p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.add(new JLabel("Click on the cells to add new agent."));
        msg.add(p);
        p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.add(new JLabel("(mouse at:"));
        jlMouseLoc = new JLabel("0,0)");
        p.add(jlMouseLoc);
        msg.add(p);
        p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.add(new JLabel("Collected golds:"));
        jGoldsC = new JLabel("0");
        p.add(jGoldsC);
        msg.add(p);

        // Buttons for controlling the simulation
        JPanel controls = new JPanel(new FlowLayout());
        JButton startButton = new JButton("Start");
        JButton stopButton = new JButton("Stop");
        JButton pauseButton = new JButton("Pause");
        JButton continueButton = new JButton("Continue");

        controls.add(startButton);
        controls.add(stopButton);
        controls.add(pauseButton);
        controls.add(continueButton);

        // Add event listeners for buttons
        startButton.addActionListener(e -> {
            env.startSimulation();
            System.out.println("Simulation started.");
        });

        stopButton.addActionListener(e -> {
            env.stopSimulation();
            System.out.println("Simulation stopped and reset.");
        });

        pauseButton.addActionListener(e -> {
            //env.pauseSimulation();
            System.out.println("Simulation paused.");
        });

        continueButton.addActionListener(e -> {
            //env.continueSimulation();
            System.out.println("Simulation resumed.");
        });

        // Combine panels into the UI
        JPanel s = new JPanel(new BorderLayout());
        s.add(BorderLayout.WEST, args);
        s.add(BorderLayout.CENTER, msg);
        s.add(BorderLayout.SOUTH, controls); // Add controls to the bottom
        getContentPane().add(BorderLayout.SOUTH, s);

        // Mouse events
        getCanvas().addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                int col = e.getX() / cellSizeW;
                int lin = e.getY() / cellSizeH;
                if (col >= 0 && lin >= 0 && col < getModel().getWidth() && lin < getModel().getHeight()) {
                    AgentModel wm = (AgentModel) model;
                    wm.add(AgentModel.AGENT, col, lin);
                    wm.initiateAgents();
                    update(col, lin);
                }
            }
            public void mouseExited(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
        });

        getCanvas().addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) { }
            public void mouseMoved(MouseEvent e) {
                int col = e.getX() / cellSizeW;
                int lin = e.getY() / cellSizeH;
                if (col >= 0 && lin >= 0 && col < getModel().getWidth() && lin < getModel().getHeight()) {
                    jlMouseLoc.setText(col + "," + lin + ")");
                }
            }
        });
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

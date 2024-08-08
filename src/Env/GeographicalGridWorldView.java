package src.Env;

import jason.environment.grid.GridWorldView;

import java.awt.*;

public class GeographicalGridWorldView extends GridWorldView {

    private GeographicalGridWorldModel model;

    public GeographicalGridWorldView(GeographicalGridWorldModel model) {
        super(model, "Geographical Grid World", 800 );
        System.out.println("Model.getWidth: " + model.getWidth());
        this.model = model;
        //setSize(800, 800);
        setVisible(true);
    }

    @Override
    public void draw(Graphics g, int x, int y, int object) {
        super.draw(g, x, y, object);
        if (object == GeographicalGridWorldModel.WALL) {
            g.setColor(Color.BLACK);
            g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);

        }
    }
}

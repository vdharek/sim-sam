package src.Env;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

public class TempModel extends GridWorldModel {

    Logger log = Logger.getLogger(TempModel.class.getName());

    public static final int WALL = 16;
    private final Set<Location> wallLocation;
    private final int screenWidth;
    private final int screenHeight;
    private final double[][] coordinates;
    private final double[] minMax;

    protected TempModel(int gridWidth, int gridHeight, double[][] coordinates, double[] minMax) {
        super(gridWidth, gridHeight, WALL);
        this.coordinates = coordinates;
        this.screenWidth = gridWidth;
        this.screenHeight = gridHeight;
        this.minMax = minMax;
        this.wallLocation  = new HashSet<Location>();
        addWall();
    }

    private void addWall(){
        double minX = minMax[0];
        double maxX = minMax[1];
        double minY = minMax[2];
        double maxY = minMax[3];

        for(int i=0; i<coordinates.length-1; i++){
            Location start = setCoordinates(coordinates[i][0], coordinates[i][1], minX, minY, maxX, maxY);
            Location end = setCoordinates(coordinates[i+1][0], coordinates[i+1][1], minX, minY, maxX, maxY);

            addWallInBetween(start, end);
        }
    }

    private Location setCoordinates(double x, double y, double minX, double minY, double maxX, double maxY){
        int gridX = (int) ((maxX - x) / (maxX - minX) * screenWidth);
        int gridY = (int) ((maxY - y) / (maxY - minY) * screenHeight);
        System.out.println("Real-world coordinates: (" + x + ", " + y + ")");
        System.out.println("Mapped to grid coordinates: (" + gridX + ", " + gridY + ")");
        return new Location(gridX, gridY);
    }

    private void addWallInBetween(Location start, Location end){
        int dx = Math.abs(end.x - start.x);
        int dy = Math.abs(end.y - start.y);
        int sx = start.x < end.x ? 1 : -1;
        int sy = start.y < end.y ? 1 : -1;
        int err = dx - dy;

        int x = start.x;
        int y = start.y;

        while(true){
            Location loc =  new Location(x, y);
            if(!wallLocation.contains(loc)) {
                wallLocation.add(loc);
                if(x >= 0 && x < getWidth() && y >=0 && y < getHeight()){
                    add(WALL, loc);
                }
            }
            if(x == end.x && y == end.y){
                break;
            }
            int e2 = 2 * err;
            if(e2 > -dy){
                err -= dy;
                x += sx;
            }
            if(e2 < dy){
                err += dx;
                y += sy;
            }
        }
    }

    /*private void setCoordinates(double[][] gridCoordinates){
        Location[] gridLocation = new Location[gridCoordinates.length];
        for(int i=0; i<gridCoordinates.length; i++){
            gridLocation[i] = new Location((int) Math.round(gridCoordinates[i][0]),
                    (int) Math.round(gridCoordinates[i][1]));

            addWall(gridLocation[i]);
        }
        //return gridLocation;
    }

    public void addWall(Location location) {
        if(inGrid(location)){
            if(!hasObject(WALL, location)){
                add(WALL, location);
            }
        }
    }*/

}

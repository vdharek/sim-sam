package src.Env;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class AgentModel extends GridWorldModel {

    Logger log = Logger.getLogger(AgentModel.class.getName());

    public static final int WALL = 16;
    public static final int OBSTACLE = 32;
    public static final int AGENTS = 64;
    private Set<Location> wallLocation;
    private int screenWidth, screenHeight;
    private double[][] coordinates;
    private double R = 6378137;
    private double CELL_SIZE;

    public AgentModel(int gridWidth, int gridHeight, double[][] coordinates) {
        super(gridHeight, gridWidth, WALL);
        this.coordinates = coordinates;
        this.screenWidth = gridWidth;
        this.screenHeight = gridHeight;
        this.wallLocation = new HashSet<>();
        addWall();
        findEmptyCellsUntilWall();
        //fillOuterCellsWithObstacles();
    }

    public int[][] getGridSize(double rangeX, double rangeY) {

        CELL_SIZE = 0.1;
        int gridWidth = (int) Math.ceil(rangeX / CELL_SIZE);
        int gridHeight = (int) Math.ceil(rangeY / CELL_SIZE);
        return new int[gridHeight][gridWidth];
    }

    public void findEmptyCellsUntilWall() {
        // Start from each cell and log empty cells until a WALL is found
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                if (hasObject(WALL,x, y)){  // Empty cell
                    break;
                }else{
                    paintEmptyCells(x,y);
                }
            }
        }
    }

    private void paintEmptyCells(int startX, int startY) {
        // Log empty cells from (startX, startY) until a WALL is encountered
        // Right direction
        for (int x = startX; x < getWidth(); x++) {
            if (hasObject(WALL, x, startY)) {
                break;  // Stop when WALL is encountered
            }
            else{
                add(OBSTACLE, x, startY);
                //log.info("Empty cell found at: (" + x + ", " + startY + ")");
            }
        }

        // Left direction
        for (int x = startX; x >= 0; x--) {
            if (hasObject(WALL, x, startY)) {
                break;  // Stop when WALL is encountered
            } else{
                add(OBSTACLE, x, startY);
                //log.info("Empty cell found at: (" + x + ", " + startY + ")");
            }
        }

        // Up direction
        for (int y = startY; y >= 0; y--) {
            if(hasObject(WALL, startX, y)){
                break;
            }
            else{
                add(OBSTACLE, startX, y);
                //log.info("Empty cell found at: (" + startX + ", " + y + ")");
            }
        }

        // Down direction
        for (int y = startY; y < getHeight(); y++) {
            if (hasObject(WALL, startX, y)) {
                break;  // Stop when WALL is encountered
            }
            else {
                add(OBSTACLE, startX, y);
                //log.info("Empty cell found at: (" + startX + ", " + y + ")");
            }
        }
    }

    private void addWall(){
        double minLat = Double.POSITIVE_INFINITY;
        double maxLat = Double.NEGATIVE_INFINITY;
        double minLon = Double.POSITIVE_INFINITY;
        double maxLon = Double.NEGATIVE_INFINITY;

        for(double[] coord : coordinates){
            if(coord[0] < minLat) minLat = coord[0];
            if(coord[0] > maxLat) maxLat = coord[0];
            if(coord[1] < minLon) minLon = coord[1];
            if(coord[1] > maxLon) maxLon = coord[1];
        }

        double rangeX = maxLat - minLat;
        double rangeY = maxLon - minLon;

        getGridSize(rangeX, rangeY);

        for(int i=0; i<coordinates.length-1; i++){
            Location start = setCoordinates(coordinates[i][0], coordinates[i][1], minLat, minLon, maxLat, maxLon);
            Location end = setCoordinates(coordinates[i+1][0], coordinates[i+1][1], minLat, minLon, maxLat, maxLon);

            //double distanceX = haversine(minLat, minLon, maxLat, maxLon);
            //double distanceY = haversine(minLat, minLon, maxLat, maxLon);
            //System.out.println("DistanceX: " + distanceX);
            //System.out.println("DistanceY: " + distanceY);
            addWallInBetween(start, end);
        }
    }

    private Location setCoordinates(double x, double y, double minX, double minY, double maxX, double maxY){
        int gridX = (int) ((maxX - x) / (maxX - minX) * screenWidth);
        int gridY = (int) ((maxY - y) / (maxY - minY) * screenHeight);
        //System.out.println("Real-world coordinates: (" + x + ", " + y + ")");
        //System.out.println("Mapped to grid coordinates: (" + gridX + ", " + gridY + ")");
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

}
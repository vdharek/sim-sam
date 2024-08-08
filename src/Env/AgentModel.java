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

        CELL_SIZE = 0.2;

        int gridWidth = (int) Math.ceil(rangeX / CELL_SIZE);
        int gridHeight = (int) Math.ceil(rangeY / CELL_SIZE);

        System.out.println(gridWidth + "GridWidth");
        System.out.println(gridHeight + "GridHeight");


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

    private void floodFill(int x, int y, boolean[][] visited) {
        if (x < 0 || x >= screenWidth || y < 0 || y >= screenHeight || visited[x][y]) {
            return;
        }

        if (hasObject(WALL, x, y)) {
            return;
        }

        visited[x][y] = true;
        add(OBSTACLE, new Location(x, y));

        floodFill(x + 1, y, visited);
        floodFill(x - 1, y, visited);
        floodFill(x, y + 1, visited);
        floodFill(x, y - 1, visited);
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        //final double R = 6378137; // Earth's radius in meters
        double dLat = toRadians(lat2 - lat1);
        double dLon = toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(toRadians(lat1)) * Math.cos(toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in meters
    }

    private double toRadians(double degree) {
        return degree * Math.PI / 180.0;
    }

    private Location setCoordinates(double x, double y, double minX, double minY, double maxX, double maxY){
        int gridX = (int) ((x - minX) / (maxX - minX) * screenWidth);
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
}

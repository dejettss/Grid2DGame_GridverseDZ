package model.arena;

import model.objects.GameObject;
import model.objects.staticobj.*;
import util.Position;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for all arena types.
 * Manages the 40x40 grid, static object placement, and cell verification.
 * Implements Single Responsibility Principle - only handles arena structure and validation.
 */
public abstract class Arena {
    public static final int GRID_WIDTH = 40;
    public static final int GRID_HEIGHT = 40;

    protected GameObject[][] grid;
    protected boolean isOpenArena;
    protected String arenaName;

    /**
     * Constructor initializes the grid with empty cells.
     */
    public Arena(String arenaName, boolean isOpenArena) {
        this.arenaName = arenaName;
        this.isOpenArena = isOpenArena;
        this.grid = new GameObject[GRID_WIDTH][GRID_HEIGHT];
        initializeEmptyGrid();
    }

    /**
     * Initializes all cells with Empty objects or Derez for open arenas.
     */
    private void initializeEmptyGrid() {
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                Position pos = new Position(x, y);
                grid[x][y] = new Empty(pos);
            }
        }
    }

    /**
     * Generate static objects specific to this arena type.
     * This method must be implemented by each concrete arena class.
     */
    protected abstract void generateStaticObjects();

    /**
     * Verifies whether a cell at the given position is empty and traversable.
     * @return true if the cell is Empty, false otherwise
     */
    public boolean isCellEmpty(Position position) {
        if (!isValidPosition(position)) {
            return false;
        }
        GameObject obj = grid[position.getX()][position.getY()];
        return obj instanceof Empty && obj.isTraversable();
    }

    /**
     * Checks if a position is within grid bounds.
     */
    public boolean isValidPosition(Position position) {
        return position.getX() >= 0 && position.getX() < GRID_WIDTH &&
               position.getY() >= 0 && position.getY() < GRID_HEIGHT;
    }

    /**
     * Checks if a position is outside grid bounds (for open arenas).
     * Used to detect when a player/enemy falls into the derez.
     */
    public boolean isFallingIntoDerez(Position position) {
        if (!isOpenArena) {
            return false;
        }
        return !isValidPosition(position);
    }

    /**
     * Gets the object at a specific position.
     */
    public GameObject getObjectAt(Position position) {
        if (!isValidPosition(position)) {
            if (isOpenArena) {
                return new Derez(position);
            }
            return null;
        }
        return grid[position.getX()][position.getY()];
    }

    /**
     * Places a static object at the given position.
     * Static objects cannot be replaced once placed.
     */
    protected void placeStaticObject(GameObject obj) {
        Position pos = obj.getPosition();
        if (isValidPosition(pos) && obj.isStatic()) {
            grid[pos.getX()][pos.getY()] = obj;
        }
    }

    /**
     * Places a wall at the given position.
     */
    protected void placeWall(int x, int y) {
        placeStaticObject(new Wall(new Position(x, y)));
    }

    /**
     * Places an obstacle at the given position.
     */
    protected void placeObstacle(int x, int y) {
        placeStaticObject(new Obstacle(new Position(x, y)));
    }

    /**
     * Places a JetWall (light trail) at the given position.
     * Players and enemies leave JetWalls at their previous positions.
     * 
     * @param position Previous position where JetWall should be placed
     * @param color Color of the entity leaving the trail
     */
    public void placeJetWall(Position position, java.awt.Color color) {
        placeJetWall(position, color, null);
    }
    
    /**
     * Places a JetWall (light trail) at the given position with owner tracking.
     * 
     * @param position Previous position where JetWall should be placed
     * @param color Color of the entity leaving the trail
     * @param ownerId ID of the entity creating this jetwall
     */
    public void placeJetWall(Position position, java.awt.Color color, String ownerId) {
        if (isCellEmpty(position)) {
            grid[position.getX()][position.getY()] = new JetWall(position, color, ownerId);
        }
    }
    
    /**
     * Gets the JetWall at a specific position.
     * 
     * @param position Position to check
     * @return JetWall object if present, null otherwise
     */
    public JetWall getJetWallAt(Position position) {
        if (!isValidPosition(position)) {
            return null;
        }
        GameObject obj = grid[position.getX()][position.getY()];
        return obj instanceof JetWall ? (JetWall) obj : null;
    }
    
    /**
     * Checks if a position contains a JetWall.
     * Used for collision detection.
     * 
     * @param position Position to check
     * @return true if position contains a JetWall
     */
    public boolean isJetWall(Position position) {
        if (!isValidPosition(position)) {
            return false;
        }
        GameObject obj = grid[position.getX()][position.getY()];
        return obj instanceof JetWall;
    }
    
    /**
     * Checks if a position contains any obstacle that causes derez.
     * Includes JetWalls, Walls, and Obstacles.
     * 
     * @param position Position to check
     * @return true if position causes derez
     */
    public boolean causesDerez(Position position) {
        if (!isValidPosition(position)) {
            return isOpenArena; // Out of bounds causes derez in open arenas
        }
        GameObject obj = grid[position.getX()][position.getY()];
        return obj.causesDerez();
    }
    
    /**
     * Clears all JetWalls from the arena.
     * Called when any player or enemy hits a JetWall (loses life).
     * All JetWalls are replaced with Empty objects.
     */
    public void clearAllJetWalls() {
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                GameObject obj = grid[x][y];
                if (obj instanceof JetWall) {
                    grid[x][y] = new Empty(new Position(x, y));
                }
            }
        }
    }
    
    /**
     * Clears JetWalls belonging to a specific entity.
     * Called when an entity is eliminated or falls into derez.
     * 
     * @param ownerId ID of the entity whose jetwalls should be cleared
     */
    public void clearEntityJetWalls(String ownerId) {
        if (ownerId == null) {
            return;
        }
        
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                GameObject obj = grid[x][y];
                if (obj instanceof JetWall) {
                    JetWall jetWall = (JetWall) obj;
                    if (ownerId.equals(jetWall.getOwnerId())) {
                        grid[x][y] = new Empty(new Position(x, y));
                    }
                }
            }
        }
    }
    
    /**
     * Gets all JetWall positions in the arena.
     * Useful for AI pathfinding and collision detection.
     * 
     * @return List of positions containing JetWalls
     */
    public List<Position> getAllJetWallPositions() {
        List<Position> jetWalls = new ArrayList<>();
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                if (grid[x][y] instanceof JetWall) {
                    jetWalls.add(new Position(x, y));
                }
            }
        }
        return jetWalls;
    }
    
    /**
     * Gets all JetWalls in the arena.
     * Returns actual JetWall objects for detailed information.
     * 
     * @return List of JetWall objects
     */
    public List<JetWall> getAllJetWalls() {
        List<JetWall> jetWalls = new ArrayList<>();
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                GameObject obj = grid[x][y];
                if (obj instanceof JetWall) {
                    jetWalls.add((JetWall) obj);
                }
            }
        }
        return jetWalls;
    }
    
    /**
     * Counts the number of JetWalls in the arena.
     * 
     * @return Number of JetWall objects
     */
    public int getJetWallCount() {
        int count = 0;
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                if (grid[x][y] instanceof JetWall) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Gets all static objects in the arena.
     */
    public List<GameObject> getStaticObjects() {
        List<GameObject> staticObjects = new ArrayList<>();
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                GameObject obj = grid[x][y];
                if (obj.isStatic() && !(obj instanceof Empty)) {
                    staticObjects.add(obj);
                }
            }
        }
        return staticObjects;
    }

    /**
     * Gets the arena name.
     */
    public String getArenaName() {
        return arenaName;
    }

    /**
     * Checks if this is an open arena (no walls at boundaries).
     */
    public boolean isOpenArena() {
        return isOpenArena;
    }

    /**
     * Gets the grid dimensions.
     */
    public int getWidth() {
        return GRID_WIDTH;
    }

    public int getHeight() {
        return GRID_HEIGHT;
    }

    /**
     * Returns a string representation of the arena for console display.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(arenaName).append(" ===\n");
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                sb.append(grid[x][y].getSymbol());
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}

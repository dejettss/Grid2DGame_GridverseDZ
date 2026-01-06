package model.objects;

import model.arena.Arena;
import util.Position;
import java.awt.Color;
import java.util.*;

/**
 * Manages all discs in the game.
 * Handles throwing, recapture, and disc ownership rules.
 */
public class DiscManager {
    
    private Map<String, List<Disc>> entityDiscs;  // Maps entity ID to their discs
    private Arena arena;                           // Arena reference for obstacle checking
    
    /**
     * Constructor for DiscManager.
     */
    public DiscManager() {
        this.entityDiscs = new HashMap<>();
        this.arena = null;
    }
    
    /**
     * Sets the arena reference for obstacle checking.
     */
    public void setArena(Arena arena) {
        this.arena = arena;
    }
    
    /**
     * Registers a disc for an entity.
     * 
     * @param entityId ID of the entity (player/enemy)
     * @param disc The disc to register
     */
    public void registerDisc(String entityId, Disc disc) {
        entityDiscs.putIfAbsent(entityId, new ArrayList<>());
        entityDiscs.get(entityId).add(disc);
    }
    
    /**
     * Creates and registers discs for an entity.
     * 
     * @param entityId ID of the entity
     * @param ownerColor Color of the owner
     * @param startPosition Starting position
     * @param count Number of discs to create
     */
    public void createDiscsForEntity(String entityId, Color ownerColor, 
                                    Position startPosition, int count) {
        for (int i = 0; i < count; i++) {
            Disc disc = new Disc(entityId, ownerColor, startPosition);
            registerDisc(entityId, disc);
        }
    }
    
    /**
     * Throws a disc for an entity.
     * Checks for walls, obstacles, and JetWalls along the path.
     * 
     * @param entityId ID of the entity throwing
     * @param direction Direction to throw
     * @param distance Distance to throw (1-3)
     * @return Position where disc landed, or null if throw failed
     */
    public Position throwDisc(String entityId, util.Direction direction, int distance) {
        List<Disc> discs = entityDiscs.get(entityId);
        
        if (discs == null || discs.isEmpty()) {
            return null;  // No discs available
        }
        
        // Build obstacle set from arena
        Set<Position> obstacles = buildObstacleSet();
        
        // Find first held disc
        for (Disc disc : discs) {
            if (disc.isHeld()) {
                return disc.throwDisc(direction, distance, obstacles);
            }
        }
        
        return null;  // No held discs
    }
    
    /**
     * Builds a set of all positions that block disc travel.
     * Includes: walls, obstacles, and JetWalls.
     */
    private Set<Position> buildObstacleSet() {
        Set<Position> obstacles = new HashSet<>();
        
        if (arena == null) {
            return obstacles; // No arena, return empty set
        }
        
        // Check all grid positions for blocking objects
        for (int x = 0; x < Arena.GRID_WIDTH; x++) {
            for (int y = 0; y < Arena.GRID_HEIGHT; y++) {
                Position pos = new Position(x, y);
                GameObject obj = arena.getObjectAt(pos);
                
                // Add position if it has a wall, obstacle, or JetWall
                if (obj != null && !obj.isTraversable()) {
                    obstacles.add(pos);
                }
                
                // Also check for JetWalls specifically
                if (arena.isJetWall(pos)) {
                    obstacles.add(pos);
                }
            }
        }
        
        return obstacles;
    }
    
    /**
     * Attempts to recapture a disc.
     * 
     * @param entityId ID of the entity attempting recapture
     * @param entityPosition Position of the entity
     * @return true if a disc was recaptured
     */
    public boolean recaptureDisc(String entityId, Position entityPosition) {
        List<Disc> discs = entityDiscs.get(entityId);
        
        if (discs == null) {
            return false;
        }
        
        // Try to recapture any thrown disc
        for (Disc disc : discs) {
            if (disc.recapture(entityId, entityPosition)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Updates entity position for all their held discs.
     */
    public void updateEntityPosition(String entityId, Position newPosition) {
        List<Disc> discs = entityDiscs.get(entityId);
        
        if (discs != null) {
            for (Disc disc : discs) {
                if (disc.isHeld()) {
                    disc.updatePosition(newPosition);
                }
            }
        }
    }
    
    /**
     * Gets all discs on the grid (not held).
     */
    public List<Disc> getDiscsOnGrid() {
        List<Disc> result = new ArrayList<>();
        
        for (List<Disc> discs : entityDiscs.values()) {
            for (Disc disc : discs) {
                if (!disc.isHeld()) {
                    result.add(disc);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Gets all discs belonging to an entity.
     */
    public List<Disc> getEntityDiscs(String entityId) {
        return entityDiscs.getOrDefault(entityId, new ArrayList<>());
    }
    
    /**
     * Gets number of held discs for an entity.
     */
    public int getHeldDiscCount(String entityId) {
        List<Disc> discs = entityDiscs.get(entityId);
        if (discs == null) return 0;
        
        int count = 0;
        for (Disc disc : discs) {
            if (disc.isHeld()) count++;
        }
        return count;
    }
    
    /**
     * Gets number of thrown discs for an entity.
     */
    public int getThrownDiscCount(String entityId) {
        List<Disc> discs = entityDiscs.get(entityId);
        if (discs == null) return 0;
        
        int count = 0;
        for (Disc disc : discs) {
            if (!disc.isHeld()) count++;
        }
        return count;
    }
    
    /**
     * Gets disc at a specific position (if any).
     */
    public Disc getDiscAtPosition(Position position) {
        for (List<Disc> discs : entityDiscs.values()) {
            for (Disc disc : discs) {
                if (!disc.isHeld() && disc.getPosition().equals(position)) {
                    return disc;
                }
            }
        }
        return null;
    }
    
    /**
     * Checks if entity can throw a disc.
     */
    public boolean canThrow(String entityId) {
        List<Disc> discs = entityDiscs.get(entityId);
        if (discs == null) return false;
        
        for (Disc disc : discs) {
            if (disc.canThrow()) return true;
        }
        return false;
    }
    
    /**
     * Removes all discs for an entity (when entity is eliminated).
     */
    public void removeEntityDiscs(String entityId) {
        entityDiscs.remove(entityId);
    }
    
    /**
     * Gets all disc positions on grid (for rendering/collision).
     */
    public Set<Position> getAllDiscPositions() {
        Set<Position> positions = new HashSet<>();
        
        for (List<Disc> discs : entityDiscs.values()) {
            for (Disc disc : discs) {
                if (!disc.isHeld()) {
                    positions.add(disc.getPosition());
                }
            }
        }
        
        return positions;
    }
    
    /**
     * Gets statistics for debugging.
     */
    public String getStatistics() {
        int totalDiscs = 0;
        int heldDiscs = 0;
        int thrownDiscs = 0;
        
        for (List<Disc> discs : entityDiscs.values()) {
            totalDiscs += discs.size();
            for (Disc disc : discs) {
                if (disc.isHeld()) {
                    heldDiscs++;
                } else {
                    thrownDiscs++;
                }
            }
        }
        
        return String.format("Total: %d discs (%d held, %d thrown) across %d entities",
                totalDiscs, heldDiscs, thrownDiscs, entityDiscs.size());
    }
}

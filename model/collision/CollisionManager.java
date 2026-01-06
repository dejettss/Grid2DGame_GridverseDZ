package model.collision;

import model.arena.Arena;
import model.arena.JetWallManager;
import model.objects.Disc;
import util.Position;
import java.util.*;

/**
 * Comprehensive collision management system.
 * Integrates JetWall collisions, disc collisions, and arena boundary checks.
 * 
 * Collision Rules:
 * 1. Disc hit: -1 life
 * 2. JetWall collision: -0.5 lives  
 * 3. Fall off open arena: All lives lost instantly
 */
public class CollisionManager {
    
    private CollisionDetector detector;
    private JetWallManager jetWallManager;
    private Map<String, List<Disc>> entityDiscs;  // Track discs per entity
    
    /**
     * Constructor for CollisionManager.
     * 
     * @param arena The game arena
     * @param jetWallManager JetWall manager for trail collisions
     */
    public CollisionManager(Arena arena, JetWallManager jetWallManager) {
        this.detector = new CollisionDetector(arena);
        this.jetWallManager = jetWallManager;
        this.entityDiscs = new HashMap<>();
    }
    
    /**
     * Registers an entity for collision tracking.
     * 
     * @param entityId Entity identifier
     * @param startingLives Starting number of lives
     */
    public void registerEntity(String entityId, int startingLives) {
        detector.registerEntity(entityId, startingLives);
        entityDiscs.put(entityId, new ArrayList<>());
    }
    
    /**
     * Processes movement collision for an entity.
     * Checks for JetWall and boundary collisions.
     * 
     * @param entityId Entity moving
     * @param newPosition New position to move to
     * @return CollisionResult
     */
    public CollisionResult processMovementCollision(String entityId, Position newPosition) {
        // Check for position-based collisions
        CollisionResult result = detector.checkCollision(entityId, newPosition);
        
        // If JetWall collision, trigger JetWall auto-clear
        if (result.getType() == CollisionType.JETWALL) {
            jetWallManager.clearAllJetWalls();
        }
        
        return result;
    }
    
    /**
     * Checks for disc collisions at a position.
     * Returns the first disc that would hit an entity at this position.
     * 
     * @param entityId Entity to check
     * @param position Position to check
     * @return CollisionResult, or null if no disc collision
     */
    public CollisionResult checkDiscCollisionAtPosition(String entityId, Position position) {
        // Check all discs from all other entities
        for (Map.Entry<String, List<Disc>> entry : entityDiscs.entrySet()) {
            String discOwnerId = entry.getKey();
            
            // Don't check own discs (can't be hit by own disc)
            if (discOwnerId.equals(entityId)) {
                continue;
            }
            
            // Check each disc from this owner
            for (Disc disc : entry.getValue()) {
                if (!disc.isHeld() && disc.getPosition().equals(position)) {
                    // Entity hit by this disc!
                    return detector.checkDiscCollision(entityId, discOwnerId);
                }
            }
        }
        
        return null;  // No disc collision
    }
    
    /**
     * Processes a complete move including all collision types.
     * Checks movement collision AND disc collision.
     * 
     * @param entityId Entity moving
     * @param newPosition New position
     * @return List of all collisions that occurred
     */
    public List<CollisionResult> processFullCollision(String entityId, Position newPosition) {
        List<CollisionResult> results = new ArrayList<>();
        
        // Check movement-based collisions
        CollisionResult movementResult = processMovementCollision(entityId, newPosition);
        if (movementResult.isCollision()) {
            results.add(movementResult);
        }
        
        // Check disc collisions
        CollisionResult discResult = checkDiscCollisionAtPosition(entityId, newPosition);
        if (discResult != null) {
            results.add(discResult);
        }
        
        return results;
    }
    
    /**
     * Registers a disc for collision tracking.
     * 
     * @param entityId Owner of the disc
     * @param disc The disc to track
     */
    public void registerDisc(String entityId, Disc disc) {
        entityDiscs.putIfAbsent(entityId, new ArrayList<>());
        entityDiscs.get(entityId).add(disc);
    }
    
    /**
     * Gets the current lives of an entity.
     * 
     * @param entityId Entity identifier
     * @return Current lives (fractional)
     */
    public double getEntityLives(String entityId) {
        return detector.getEntityLives(entityId);
    }
    
    /**
     * Gets integer lives (for display).
     * 
     * @param entityId Entity identifier
     * @return Lives rounded up
     */
    public int getEntityLivesInt(String entityId) {
        return detector.getEntityLivesInt(entityId);
    }
    
    /**
     * Checks if an entity is alive.
     * 
     * @param entityId Entity identifier
     * @return true if alive
     */
    public boolean isAlive(String entityId) {
        return detector.isAlive(entityId);
    }
    
    /**
     * Sets an entity's lives.
     * 
     * @param entityId Entity identifier
     * @param lives New life count
     */
    public void setEntityLives(String entityId, double lives) {
        detector.setEntityLives(entityId, lives);
    }
    
    /**
     * Adds lives to an entity.
     * 
     * @param entityId Entity identifier
     * @param lives Lives to add
     */
    public void addLives(String entityId, double lives) {
        detector.addLives(entityId, lives);
    }
    
    /**
     * Removes an entity from tracking.
     * 
     * @param entityId Entity to remove
     */
    public void unregisterEntity(String entityId) {
        detector.unregisterEntity(entityId);
        entityDiscs.remove(entityId);
    }
    
    /**
     * Gets statistics.
     * 
     * @return Statistics string
     */
    public String getStatistics() {
        return detector.getStatistics();
    }
    
    /**
     * Gets all tracked entities.
     * 
     * @return Set of entity IDs
     */
    public Set<String> getTrackedEntities() {
        return detector.getTrackedEntities();
    }
    
    /**
     * Resets collision tracking.
     * 
     * @param startingLives Map of entity IDs to starting lives
     */
    public void reset(Map<String, Integer> startingLives) {
        detector.reset(startingLives);
        entityDiscs.clear();
    }
    
    /**
     * Clears all tracking.
     */
    public void clear() {
        detector.clear();
        entityDiscs.clear();
    }
}

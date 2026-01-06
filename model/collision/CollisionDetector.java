package model.collision;

import model.arena.Arena;
import util.Position;
import java.util.*;

/**
 * Handles collision detection and consequences for players and enemies.
 * 
 * Collision Rules:
 * 1. Being struck by a disc: -1 life
 * 2. Colliding with a JetWall: -0.5 lives
 * 3. Falling off an open arena: All lives lost instantly
 * 
 * Note: Wall and obstacle collisions are handled by movement validation,
 * not by the collision system.
 */
public class CollisionDetector {
    
    private Arena arena;
    private Map<String, Double> entityLives;  // Track fractional lives
    
    /**
     * Constructor for CollisionDetector.
     * 
     * @param arena The game arena
     */
    public CollisionDetector(Arena arena) {
        this.arena = arena;
        this.entityLives = new HashMap<>();
    }
    
    /**
     * Registers an entity with their starting lives.
     * 
     * @param entityId Entity identifier
     * @param lives Starting number of lives
     */
    public void registerEntity(String entityId, int lives) {
        entityLives.put(entityId, (double) lives);
    }
    
    /**
     * Checks for collision at a position and applies consequences.
     * 
     * @param entityId Entity identifier
     * @param position Position to check
     * @return CollisionResult containing collision type and lives lost
     */
    public CollisionResult checkCollision(String entityId, Position position) {
        // Check for falling off open arena (instant death)
        if (arena.isFallingIntoDerez(position)) {
            double currentLives = entityLives.getOrDefault(entityId, 0.0);
            entityLives.put(entityId, 0.0);
            return new CollisionResult(
                CollisionType.FALL_OFF_ARENA,
                currentLives,
                "Fell off the arena edge!"
            );
        }
        
        // Check for JetWall collision (-0.5 lives)
        if (arena.isJetWall(position)) {
            double livesLost = 0.5;
            applyDamage(entityId, livesLost);
            return new CollisionResult(
                CollisionType.JETWALL,
                livesLost,
                "Hit a JetWall!"
            );
        }
        
        // No collision
        return new CollisionResult(CollisionType.NONE, 0.0, "Safe");
    }
    
    /**
     * Checks for disc collision.
     * 
     * @param entityId Entity that was hit
     * @param discOwnerId Owner of the disc
     * @return CollisionResult for disc hit
     */
    public CollisionResult checkDiscCollision(String entityId, String discOwnerId) {
        // Being struck by a disc: -1 life
        double livesLost = 1.0;
        applyDamage(entityId, livesLost);
        
        return new CollisionResult(
            CollisionType.DISC_HIT,
            livesLost,
            "Hit by " + discOwnerId + "'s disc!"
        );
    }
    
    /**
     * Applies damage to an entity.
     * 
     * @param entityId Entity to damage
     * @param damage Amount of damage (lives to lose)
     */
    private void applyDamage(String entityId, double damage) {
        double currentLives = entityLives.getOrDefault(entityId, 0.0);
        double newLives = Math.max(0.0, currentLives - damage);
        entityLives.put(entityId, newLives);
    }
    
    /**
     * Gets the current lives of an entity (fractional).
     * 
     * @param entityId Entity identifier
     * @return Current lives (can be fractional)
     */
    public double getEntityLives(String entityId) {
        return entityLives.getOrDefault(entityId, 0.0);
    }
    
    /**
     * Gets the integer lives count (for display).
     * 
     * @param entityId Entity identifier
     * @return Lives rounded up to nearest integer
     */
    public int getEntityLivesInt(String entityId) {
        double lives = entityLives.getOrDefault(entityId, 0.0);
        return (int) Math.ceil(lives);
    }
    
    /**
     * Checks if an entity is still alive.
     * 
     * @param entityId Entity identifier
     * @return true if entity has more than 0 lives
     */
    public boolean isAlive(String entityId) {
        return entityLives.getOrDefault(entityId, 0.0) > 0.0;
    }
    
    /**
     * Sets an entity's lives (for power-ups, healing, etc.).
     * 
     * @param entityId Entity identifier
     * @param lives New life count
     */
    public void setEntityLives(String entityId, double lives) {
        entityLives.put(entityId, Math.max(0.0, lives));
    }
    
    /**
     * Adds lives to an entity (healing).
     * 
     * @param entityId Entity identifier
     * @param lives Lives to add
     */
    public void addLives(String entityId, double lives) {
        double currentLives = entityLives.getOrDefault(entityId, 0.0);
        entityLives.put(entityId, currentLives + lives);
    }
    
    /**
     * Removes an entity from tracking.
     * 
     * @param entityId Entity to remove
     */
    public void unregisterEntity(String entityId) {
        entityLives.remove(entityId);
    }
    
    /**
     * Gets all tracked entity IDs.
     * 
     * @return Set of entity IDs
     */
    public Set<String> getTrackedEntities() {
        return new HashSet<>(entityLives.keySet());
    }
    
    /**
     * Gets statistics about entity lives.
     * 
     * @return Statistics string
     */
    public String getStatistics() {
        int aliveCount = 0;
        int deadCount = 0;
        
        for (Double lives : entityLives.values()) {
            if (lives > 0.0) {
                aliveCount++;
            } else {
                deadCount++;
            }
        }
        
        return String.format("Alive: %d | Dead: %d | Total: %d",
                aliveCount, deadCount, entityLives.size());
    }
    
    /**
     * Resets all entity lives to their starting values.
     * 
     * @param startingLives Map of entity IDs to starting lives
     */
    public void reset(Map<String, Integer> startingLives) {
        entityLives.clear();
        for (Map.Entry<String, Integer> entry : startingLives.entrySet()) {
            entityLives.put(entry.getKey(), (double) entry.getValue());
        }
    }
    
    /**
     * Clears all tracking.
     */
    public void clear() {
        entityLives.clear();
    }
}

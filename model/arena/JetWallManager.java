package model.arena;

import util.Position;
import java.awt.Color;
import java.util.*;

/**
 * Manages JetWall generation and lifecycle for players and enemies.
 * Tracks entity movements and handles collision consequences.
 * 
 * JetWall Rules:
 * 1. Players/enemies leave JetWalls at their previous positions
 * 2. Hitting any JetWall (own or others) causes life loss
 * 3. When any entity hits a JetWall, ALL JetWalls are cleared
 */
public class JetWallManager {
    
    private Arena arena;
    private Map<String, Position> entityLastPositions;  // Track previous positions
    private Map<String, Color> entityColors;            // Track entity colors
    private boolean jetwallsEnabled;                    // Enable/disable JetWall generation
    
    /**
     * Constructor for JetWallManager.
     * 
     * @param arena The arena where JetWalls are managed
     */
    public JetWallManager(Arena arena) {
        this.arena = arena;
        this.entityLastPositions = new HashMap<>();
        this.entityColors = new HashMap<>();
        this.jetwallsEnabled = true;
    }
    
    /**
     * Registers an entity for JetWall tracking.
     * Must be called before entity starts moving.
     * 
     * @param entityId Unique identifier for the entity
     * @param color Color of the entity's JetWalls
     * @param startPosition Initial position (no JetWall here)
     */
    public void registerEntity(String entityId, Color color, Position startPosition) {
        entityColors.put(entityId, color);
        entityLastPositions.put(entityId, startPosition);
    }
    
    /**
     * Updates an entity's position and creates JetWall at previous position.
     * This is the main method called when entities move.
     * 
     * @param entityId Entity identifier
     * @param newPosition New position after movement
     */
    public void moveEntity(String entityId, Position newPosition) {
        if (!jetwallsEnabled) {
            entityLastPositions.put(entityId, newPosition);
            return;
        }
        
        // Get entity's previous position
        Position previousPos = entityLastPositions.get(entityId);
        
        if (previousPos != null && !previousPos.equals(newPosition)) {
            // Place JetWall at previous position with owner tracking
            Color entityColor = entityColors.get(entityId);
            if (entityColor != null) {
                arena.placeJetWall(previousPos, entityColor, entityId);
            }
        }
        
        // Update entity's last position
        entityLastPositions.put(entityId, newPosition);
    }
    
    /**
     * Manually places a JetWall at a specific position.
     * Useful for testing or special game mechanics.
     * 
     * @param position Position to place JetWall
     * @param color Color of the JetWall
     */
    public void placeJetWall(Position position, Color color) {
        if (jetwallsEnabled) {
            arena.placeJetWall(position, color);
        }
    }
    
    /**
     * Checks if an entity would collide with a JetWall at a position.
     * Useful for AI pathfinding and move validation.
     * 
     * @param position Position to check
     * @return true if position contains a JetWall
     */
    public boolean wouldHitJetWall(Position position) {
        return arena.isJetWall(position);
    }
    
    /**
     * Checks if a position would cause derez (any obstacle).
     * Includes JetWalls, Walls, Obstacles, and out-of-bounds.
     * 
     * @param position Position to check
     * @return true if position causes derez
     */
    public boolean wouldCauseDerez(Position position) {
        return arena.causesDerez(position);
    }
    
    /**
     * Clears all JetWalls from the arena.
     * Called externally when needed (e.g., round reset).
     */
    public void clearAllJetWalls() {
        arena.clearAllJetWalls();
    }
    
    /**
     * Clears all JetWalls belonging to a specific entity.
     * Called when an entity is eliminated or falls into derez.
     * 
     * @param entityId Entity whose jetwalls should be cleared
     */
    public void clearEntityJetWalls(String entityId) {
        arena.clearEntityJetWalls(entityId);
    }
    
    /**
     * Removes an entity from tracking.
     * Called when entity is eliminated or leaves game.
     * 
     * @param entityId Entity to remove
     */
    public void unregisterEntity(String entityId) {
        entityLastPositions.remove(entityId);
        entityColors.remove(entityId);
    }
    
    /**
     * Gets all current JetWall positions.
     * Useful for rendering and AI pathfinding.
     * 
     * @return Set of all JetWall positions
     */
    public Set<Position> getAllJetWallPositions() {
        return new HashSet<>(arena.getAllJetWallPositions());
    }
    
    /**
     * Gets the count of JetWalls currently in the arena.
     * 
     * @return Number of JetWalls
     */
    public int getJetWallCount() {
        return arena.getJetWallCount();
    }
    
    /**
     * Enables or disables JetWall generation.
     * Useful for practice mode or specific game phases.
     * 
     * @param enabled true to enable JetWalls, false to disable
     */
    public void setJetWallsEnabled(boolean enabled) {
        this.jetwallsEnabled = enabled;
        if (!enabled) {
            clearAllJetWalls();
        }
    }
    
    /**
     * Checks if JetWalls are currently enabled.
     * 
     * @return true if JetWalls are enabled
     */
    public boolean areJetWallsEnabled() {
        return jetwallsEnabled;
    }
    
    /**
     * Gets the last known position of an entity.
     * 
     * @param entityId Entity identifier
     * @return Last position, or null if not tracked
     */
    public Position getEntityLastPosition(String entityId) {
        return entityLastPositions.get(entityId);
    }
    
    /**
     * Gets the color of an entity's JetWalls.
     * 
     * @param entityId Entity identifier
     * @return Entity's JetWall color, or null if not registered
     */
    public Color getEntityColor(String entityId) {
        return entityColors.get(entityId);
    }
    
    /**
     * Checks if an entity is registered with the manager.
     * 
     * @param entityId Entity identifier
     * @return true if entity is registered
     */
    public boolean isEntityRegistered(String entityId) {
        return entityColors.containsKey(entityId);
    }
    
    /**
     * Gets all registered entity IDs.
     * 
     * @return Set of entity IDs
     */
    public Set<String> getRegisteredEntities() {
        return new HashSet<>(entityColors.keySet());
    }
    
    /**
     * Resets the manager, clearing all tracking and JetWalls.
     * Useful for starting a new round.
     */
    public void reset() {
        clearAllJetWalls();
        entityLastPositions.clear();
        // Note: entityColors are kept so entities remain registered
    }
    
    /**
     * Completely clears the manager, including entity registrations.
     * Use this for starting a completely new game.
     */
    public void clear() {
        clearAllJetWalls();
        entityLastPositions.clear();
        entityColors.clear();
    }
    
    /**
     * Gets statistics about JetWalls for debugging.
     * 
     * @return Statistics string
     */
    public String getStatistics() {
        int wallCount = getJetWallCount();
        int entityCount = entityColors.size();
        String status = jetwallsEnabled ? "Enabled" : "Disabled";
        
        return String.format("JetWalls: %d | Entities: %d | Status: %s",
                wallCount, entityCount, status);
    }
    
    /**
     * Gets detailed information about a specific position.
     * 
     * @param position Position to check
     * @return Description of what's at that position
     */
    public String getPositionInfo(Position position) {
        if (!arena.isValidPosition(position)) {
            return "Out of bounds";
        }
        
        if (arena.isJetWall(position)) {
            return "JetWall";
        } else if (arena.causesDerez(position)) {
            return "Obstacle (causes derez)";
        } else if (arena.isCellEmpty(position)) {
            return "Empty";
        } else {
            return "Unknown object";
        }
    }
}
